package com.dbp.democarpultec.service.impl;

import com.dbp.democarpultec.dto.RequestPublicationRequestDto;
import com.dbp.democarpultec.dto.RequestPublicationResponseDto;
import com.dbp.democarpultec.event.RequestStatusChangedEvent;
import com.dbp.democarpultec.exception.BusinessRuleException;
import com.dbp.democarpultec.exception.DuplicateResourceException;
import com.dbp.democarpultec.exception.ForbiddenException;
import com.dbp.democarpultec.model.Publication;
import com.dbp.democarpultec.model.RequestPublication;
import com.dbp.democarpultec.model.Ride;
import com.dbp.democarpultec.model.RidePassenger;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.Vehicle;
import com.dbp.democarpultec.model.enums.Status;
import com.dbp.democarpultec.repository.RidePassengerRepository;
import com.dbp.democarpultec.repository.RideRepository;
import com.dbp.democarpultec.repository.RequestPublicationRepository;
import com.dbp.democarpultec.service.GeoService;
import com.dbp.democarpultec.service.GoogleMapsService;
import com.dbp.democarpultec.service.PublicationService;
import com.dbp.democarpultec.service.RequestPublicationService;
import com.dbp.democarpultec.service.UserService;
import com.dbp.democarpultec.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestPublicationServiceImpl implements RequestPublicationService {
    private static final EnumSet<Status> ACTIVE_REQUEST_STATUSES = EnumSet.of(Status.PENDING, Status.ACCEPTED);

    private final RequestPublicationRepository requestPublicationRepository;
    private final RideRepository rideRepository;
    private final RidePassengerRepository ridePassengerRepository;
    private final PublicationService publicationService;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final GeoService geoService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public List<RequestPublicationResponseDto> findAll() {
        return requestPublicationRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    public RequestPublicationResponseDto findById(Long id) {
        return toResponseDto(findEntityById(id));
    }

    public RequestPublicationResponseDto create(RequestPublicationRequestDto dto) {
        RequestPublication request = new RequestPublication();
        updateEntity(request, dto);
        return toResponseDto(requestPublicationRepository.save(request));
    }

    public RequestPublicationResponseDto createAuthenticated(Long authenticatedUserId, RequestPublicationRequestDto dto) {
        Long publicationId = requirePublicationId(dto);
        Publication publication = publicationService.findEntityById(publicationId);
        validateCreateBusinessRules(publication, authenticatedUserId, dto);
        validateDriverHasVehicle(authenticatedUserId, dto.getRequesterIsDriver());

        RequestPublication request = new RequestPublication();
        request.setRequester(userService.findEntityById(authenticatedUserId));
        updateEntityData(request, publication, dto);
        return toResponseDto(requestPublicationRepository.save(request));
    }

    public RequestPublicationResponseDto update(Long id, RequestPublicationRequestDto dto) {
        RequestPublication request = findEntityById(id);
        updateEntity(request, dto);
        return toResponseDto(requestPublicationRepository.save(request));
    }

    public RequestPublicationResponseDto updateAuthenticated(Long id, Long authenticatedUserId, RequestPublicationRequestDto dto) {
        RequestPublication request = findEntityById(id);
        validateRequesterOwnership(request, authenticatedUserId);
        ensurePendingStatus(request);
        validateImmutableRequestData(request, dto);
        validateDriverHasVehicle(authenticatedUserId, dto.getRequesterIsDriver());
        updateEditableData(request, dto);
        return toResponseDto(requestPublicationRepository.save(request));
    }

    public void delete(Long id) {
        if (!requestPublicationRepository.existsById(id)) {
            throw new EntityNotFoundException("RequestPublication not found with id " + id);
        }
        requestPublicationRepository.deleteById(id);
    }

    public void deleteAuthenticated(Long id, Long authenticatedUserId) {
        RequestPublication request = findEntityById(id);
        validateRequesterOwnership(request, authenticatedUserId);
        requestPublicationRepository.deleteById(id);
    }

    public RequestPublication findEntityById(Long id) {
        return requestPublicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RequestPublication not found with id " + id));
    }

    public List<RequestPublicationResponseDto> findByPublication(Long publicationId, Long authenticatedUserId) {
        Publication publication = publicationService.findEntityById(publicationId);
        validateAuthorOwnership(publication, authenticatedUserId);

        return requestPublicationRepository.findByPublication_Id(publicationId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public RequestPublicationResponseDto createForPublication(
            Long publicationId,
            Long authenticatedUserId,
            RequestPublicationRequestDto dto
    ) {
        dto.setPublicationId(publicationId);
        return createAuthenticated(authenticatedUserId, dto);
    }

    public RequestPublicationResponseDto reject(Long requestId, Long authenticatedUserId) {
        RequestPublication request = findEntityById(requestId);
        validateAuthorOwnership(request.getPublication(), authenticatedUserId);
        ensurePendingStatus(request);

        request.setStatus(Status.REJECTED);
        RequestPublication saved = requestPublicationRepository.save(request);
        publishStatusChanged(saved, saved.getRequester());
        return toResponseDto(saved);
    }

    public RequestPublicationResponseDto cancel(Long requestId, Long authenticatedUserId) {
        RequestPublication request = findEntityById(requestId);
        validateRequesterOwnership(request, authenticatedUserId);
        ensurePendingStatus(request);

        request.setStatus(Status.CANCELLED);
        RequestPublication saved = requestPublicationRepository.save(request);
        publishStatusChanged(saved, saved.getPublication().getAuthor());
        return toResponseDto(saved);
    }

    @Transactional
    public RequestPublicationResponseDto accept(Long requestId, Long authenticatedUserId, Long vehicleId) {
        RequestPublication request = findEntityById(requestId);
        validateAuthorOwnership(request.getPublication(), authenticatedUserId);
        ensurePendingStatus(request);

        Publication publication = request.getPublication();
        User driver = resolveDriver(publication, request);
        User passenger = resolvePassenger(publication, request);

        Vehicle vehicle = resolveVehicleForAcceptedRequest(publication, vehicleId);
        validateVehicleOwnership(vehicle, driver);

        Ride existingRide = rideRepository.findByPublication_Id(publication.getId()).orElse(null);

        if (publication.getDriverToPassenger()) {
            validateDriverPublicationCapacity(publication, vehicle);
            Ride ride = existingRide != null
                    ? existingRide
                    : rideRepository.save(createRide(publication, driver, vehicle));

            validateAvailableSeats(ride, publication, request.getSeats());
            addPassengerToRide(ride, passenger, request.getSeats(), request.getPickupPointOrDestine());
            ride.setVehicle(vehicle);
            rideRepository.save(ride);
        } else {
            if (existingRide != null) {
                throw new BusinessRuleException("A driver has already been accepted for this passenger publication");
            }

            validatePassengerPublicationCapacity(publication, request, vehicle);
            Ride ride = rideRepository.save(createRide(publication, driver, vehicle));
            addPassengerToRide(ride, passenger, publication.getSeats(), request.getPickupPointOrDestine());
            rideRepository.save(ride);
            rejectOtherPendingRequests(publication.getId(), request.getId());
        }

        request.setStatus(Status.ACCEPTED);
        RequestPublication saved = requestPublicationRepository.save(request);
        publishStatusChanged(saved, saved.getRequester());
        return toResponseDto(saved);
    }

    private void updateEntity(RequestPublication request, RequestPublicationRequestDto dto) {
        Long publicationId = requirePublicationId(dto);
        updateEntityData(request, publicationService.findEntityById(publicationId), dto);
        request.setRequester(userService.findEntityById(dto.getRequesterId()));
    }

    private void updateEntityData(RequestPublication request, Publication publication, RequestPublicationRequestDto dto) {
        Double latitude = dto.getExternalLatitude();
        Double longitude = dto.getExternalLongitude();
        validateCoordinatePair(latitude, longitude, "request publication");
        if (latitude == null && longitude == null) {
            GoogleMapsService.Coordinates coordinates = geoService.geocode(dto.getPickupPointOrDestine());
            if (coordinates != null) {
                latitude = coordinates.latitude();
                longitude = coordinates.longitude();
            }
        }

        request.setPublication(publication);
        request.setRequesterIsDriver(dto.getRequesterIsDriver());
        request.setSeats(dto.getSeats());
        request.setMessage(dto.getMessage());
        request.setPickupPointOrDestine(dto.getPickupPointOrDestine());
        request.setExternalLatitude(latitude);
        request.setExternalLongitude(longitude);
    }

    private void updateEditableData(RequestPublication request, RequestPublicationRequestDto dto) {
        Double latitude = dto.getExternalLatitude();
        Double longitude = dto.getExternalLongitude();
        validateCoordinatePair(latitude, longitude, "request publication");
        if (latitude == null && longitude == null) {
            GoogleMapsService.Coordinates coordinates = geoService.geocode(dto.getPickupPointOrDestine());
            if (coordinates != null) {
                latitude = coordinates.latitude();
                longitude = coordinates.longitude();
            }
        }

        request.setSeats(dto.getSeats());
        request.setMessage(dto.getMessage());
        request.setPickupPointOrDestine(dto.getPickupPointOrDestine());
        request.setExternalLatitude(latitude);
        request.setExternalLongitude(longitude);
    }

    private void validateRequesterOwnership(RequestPublication request, Long authenticatedUserId) {
        if (!request.getRequester().getId().equals(authenticatedUserId)) {
            throw new ForbiddenException("You are not the owner of this request publication");
        }
    }

    private void validateAuthorOwnership(Publication publication, Long authenticatedUserId) {
        if (!publication.getAuthor().getId().equals(authenticatedUserId)) {
            throw new ForbiddenException("You are not the owner of this publication");
        }
    }

    private void validateCreateBusinessRules(Publication publication, Long requesterId, RequestPublicationRequestDto dto) {
        if (publication.getAuthor().getId().equals(requesterId)) {
            throw new BusinessRuleException("Author cannot create request for own publication");
        }

        if (dto.getPickupPointOrDestine() == null || dto.getPickupPointOrDestine().isBlank()) {
            throw new BusinessRuleException("pickupPointOrDestine is required");
        }

        if (dto.getRequesterIsDriver().equals(publication.getDriverToPassenger())) {
            throw new BusinessRuleException("Requester role must be opposite to publication role");
        }

        boolean hasActiveRequest = requestPublicationRepository.existsByPublication_IdAndRequester_IdAndStatusIn(
                publication.getId(),
                requesterId,
                ACTIVE_REQUEST_STATUSES
        );
        if (hasActiveRequest) {
            throw new DuplicateResourceException("Requester already has an active request for this publication");
        }
    }

    private void validateImmutableRequestData(RequestPublication request, RequestPublicationRequestDto dto) {
        Long publicationId = requirePublicationId(dto);
        if (!request.getPublication().getId().equals(publicationId)) {
            throw new BusinessRuleException("A request cannot change publication");
        }
        if (!request.getRequesterIsDriver().equals(dto.getRequesterIsDriver())) {
            throw new BusinessRuleException("A request cannot change requester role");
        }
    }

    private void validateDriverHasVehicle(Long requesterId, Boolean requesterIsDriver) {
        if (Boolean.TRUE.equals(requesterIsDriver) && !vehicleService.userHasVehicle(requesterId)) {
            throw new BusinessRuleException("A driver must register a vehicle before requesting a ride");
        }
    }

    private void ensurePendingStatus(RequestPublication request) {
        if (request.getStatus() != Status.PENDING) {
            throw new BusinessRuleException("Only PENDING requests can change state");
        }
    }

    private Long requirePublicationId(RequestPublicationRequestDto dto) {
        if (dto.getPublicationId() == null) {
            throw new BusinessRuleException("publicationId is required");
        }
        return dto.getPublicationId();
    }

    private RequestPublicationResponseDto toResponseDto(RequestPublication request) {
        return RequestPublicationResponseDto.builder()
                .id(request.getId())
                .publicationId(request.getPublication().getId())
                .requesterId(request.getRequester().getId())
                .requesterIsDriver(request.getRequesterIsDriver())
                .seats(request.getSeats())
                .message(request.getMessage())
                .pickupPointOrDestine(request.getPickupPointOrDestine())
                .externalLatitude(request.getExternalLatitude())
                .externalLongitude(request.getExternalLongitude())
                .distanceToUtecKm(geoService.distanceToUtecKm(
                        request.getExternalLatitude(),
                        request.getExternalLongitude()
                ))
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }

    private User resolveDriver(Publication publication, RequestPublication request) {
        return publication.getDriverToPassenger()
                ? publication.getAuthor()
                : request.getRequester();
    }

    private User resolvePassenger(Publication publication, RequestPublication request) {
        return publication.getDriverToPassenger()
                ? request.getRequester()
                : publication.getAuthor();
    }

    private void validateVehicleOwnership(Vehicle vehicle, User driver) {
        if (!vehicle.getOwner().getId().equals(driver.getId())) {
            throw new BusinessRuleException("Vehicle must belong to the real driver");
        }
    }

    private Vehicle resolveVehicleForAcceptedRequest(Publication publication, Long vehicleId) {
        if (Boolean.TRUE.equals(publication.getDriverToPassenger())) {
            if (publication.getVehicle() == null) {
                throw new BusinessRuleException("Driver publication has no selected vehicle");
            }
            if (!publication.getVehicle().getId().equals(vehicleId)) {
                throw new BusinessRuleException("Acceptance must use the vehicle selected in the publication");
            }
            return publication.getVehicle();
        }
        return vehicleService.findEntityById(vehicleId);
    }

    private void validateDriverPublicationCapacity(Publication publication, Vehicle vehicle) {
        if (publication.getSeats() > vehicle.getSeats()) {
            throw new BusinessRuleException("Offered seats cannot exceed vehicle capacity");
        }
    }

    private void validatePassengerPublicationCapacity(
            Publication publication,
            RequestPublication request,
            Vehicle vehicle
    ) {
        if (request.getSeats() < publication.getSeats()) {
            throw new BusinessRuleException("Driver does not offer enough seats for this passenger request");
        }
        if (vehicle.getSeats() < publication.getSeats()) {
            throw new BusinessRuleException("Vehicle does not have enough seats for this passenger request");
        }
    }

    private Ride createRide(Publication publication, User driver, Vehicle vehicle) {
        return Ride.builder()
                .publication(publication)
                .driver(driver)
                .vehicle(vehicle)
                .fromUTEC(publication.getFromUTEC())
                .destinationOrOrigin(publication.getDestinationOrOrigin())
                .departureTime(publication.getDepartureTime())
                .build();
    }

    private void validateAvailableSeats(Ride ride, Publication publication, Integer seatsToAdd) {
        int occupied = ride.getId() == null ? 0 : ridePassengerRepository.sumSeatsReservedByRide_Id(ride.getId());
        int requested = seatsToAdd == null ? 0 : seatsToAdd;
        if (occupied + requested > publication.getSeats()) {
            throw new BusinessRuleException("Not enough available seats");
        }
    }

    private void addPassengerToRide(Ride ride, User passenger, Integer seatsReserved, String pickupPoint) {
        boolean alreadyInRide = ride.getId() != null
                && ridePassengerRepository.existsByRide_IdAndPassenger_Id(ride.getId(), passenger.getId());
        if (alreadyInRide) {
            throw new DuplicateResourceException("Passenger is already part of this ride");
        }

        RidePassenger ridePassenger = RidePassenger.builder()
                .ride(ride)
                .passenger(passenger)
                .seatsReserved(seatsReserved)
                .pickupPoint(pickupPoint)
                .build();
        ridePassengerRepository.save(ridePassenger);
    }

    private void rejectOtherPendingRequests(Long publicationId, Long acceptedRequestId) {
        List<RequestPublication> pendingRequests = requestPublicationRepository.findByPublication_IdAndStatus(publicationId, Status.PENDING);
        pendingRequests.stream()
                .filter(item -> !item.getId().equals(acceptedRequestId))
                .forEach(item -> {
                    item.setStatus(Status.REJECTED);
                    publishStatusChanged(item, item.getRequester());
                });
        requestPublicationRepository.saveAll(pendingRequests);
    }

    private void publishStatusChanged(RequestPublication request, User recipient) {
        applicationEventPublisher.publishEvent(new RequestStatusChangedEvent(
                this,
                request.getId(),
                recipient == null ? null : recipient.getEmail(),
                recipient == null ? null : recipient.getName(),
                request.getPublication().getTitulo(),
                request.getStatus()
        ));
    }

    private void validateCoordinatePair(Double latitude, Double longitude, String resourceName) {
        boolean hasOnlyOneCoordinate = (latitude == null && longitude != null)
                || (latitude != null && longitude == null);
        if (hasOnlyOneCoordinate) {
            throw new BusinessRuleException("Both coordinates are required for " + resourceName + " location");
        }
    }
}
