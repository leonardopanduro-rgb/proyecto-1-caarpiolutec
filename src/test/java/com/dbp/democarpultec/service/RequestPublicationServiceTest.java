package com.dbp.democarpultec.service;

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
import com.dbp.democarpultec.repository.RequestPublicationRepository;
import com.dbp.democarpultec.repository.RidePassengerRepository;
import com.dbp.democarpultec.repository.RideRepository;
import com.dbp.democarpultec.service.impl.RequestPublicationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RequestPublicationServiceTest {
    @Mock
    private RequestPublicationRepository requestPublicationRepository;

    @Mock
    private PublicationService publicationService;

    @Mock
    private UserService userService;

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RidePassengerRepository ridePassengerRepository;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private GeoService geoService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private RequestPublicationServiceImpl requestPublicationService;

    @Test
    void shouldCreateRequestPublicationWhenValidData() {
        RequestPublicationRequestDto dto = RequestPublicationRequestDto.builder()
                .publicationId(1L)
                .requesterId(2L)
                .requesterIsDriver(false)
                .seats(2)
                .message("Puedo unirme?")
                .pickupPointOrDestine("San Miguel")
                .build();

        Publication publication = new Publication();
        publication.setId(1L);

        User requester = new User();
        requester.setId(2L);
        requester.setName("Carlos");

        RequestPublication savedRequest = new RequestPublication();
        savedRequest.setId(1L);
        savedRequest.setPublication(publication);
        savedRequest.setRequester(requester);
        savedRequest.setRequesterIsDriver(false);
        savedRequest.setSeats(2);
        savedRequest.setMessage("Puedo unirme?");
        savedRequest.setPickupPointOrDestine("San Miguel");
        savedRequest.setStatus(Status.PENDING);
        savedRequest.setCreatedAt(LocalDateTime.now());

        when(publicationService.findEntityById(1L)).thenReturn(publication);
        when(userService.findEntityById(2L)).thenReturn(requester);
        when(requestPublicationRepository.save(any(RequestPublication.class))).thenReturn(savedRequest);

        RequestPublicationResponseDto result = requestPublicationService.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getPublicationId());
        assertEquals(2L, result.getRequesterId());
        assertEquals(2, result.getSeats());
        assertEquals(Status.PENDING, result.getStatus());

        verify(publicationService).findEntityById(1L);
        verify(userService).findEntityById(2L);
        verify(requestPublicationRepository).save(any(RequestPublication.class));
    }

    @Test
    void shouldReturnRequestPublicationWhenIdExists() {
        Publication publication = new Publication();
        publication.setId(1L);

        User requester = new User();
        requester.setId(2L);
        requester.setName("Carlos");

        RequestPublication request = new RequestPublication();
        request.setId(1L);
        request.setPublication(publication);
        request.setRequester(requester);
        request.setRequesterIsDriver(false);
        request.setSeats(2);
        request.setMessage("Puedo unirme?");
        request.setPickupPointOrDestine("San Miguel");
        request.setStatus(Status.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        when(requestPublicationRepository.findById(1L)).thenReturn(Optional.of(request));

        RequestPublicationResponseDto result = requestPublicationService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getPublicationId());
        assertEquals(2L, result.getRequesterId());
        assertEquals(Status.PENDING, result.getStatus());

        verify(requestPublicationRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenRequestPublicationNotFound() {
        when(requestPublicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            requestPublicationService.findById(99L);
        });

        verify(requestPublicationRepository).findById(99L);
    }

    @Test
    void shouldUpdateRequestPublicationWhenValidData() {
        RequestPublicationRequestDto dto = RequestPublicationRequestDto.builder()
                .publicationId(1L)
                .requesterId(2L)
                .requesterIsDriver(true)
                .seats(3)
                .message("Yo conduzco")
                .pickupPointOrDestine("Miraflores")
                .build();

        Publication publication = new Publication();
        publication.setId(1L);

        User requester = new User();
        requester.setId(2L);
        requester.setName("Carlos");

        RequestPublication existingRequest = new RequestPublication();
        existingRequest.setId(1L);
        existingRequest.setStatus(Status.PENDING);

        RequestPublication updatedRequest = new RequestPublication();
        updatedRequest.setId(1L);
        updatedRequest.setPublication(publication);
        updatedRequest.setRequester(requester);
        updatedRequest.setRequesterIsDriver(true);
        updatedRequest.setSeats(3);
        updatedRequest.setMessage("Yo conduzco");
        updatedRequest.setPickupPointOrDestine("Miraflores");
        updatedRequest.setStatus(Status.PENDING);
        updatedRequest.setCreatedAt(LocalDateTime.now());

        when(requestPublicationRepository.findById(1L)).thenReturn(Optional.of(existingRequest));
        when(publicationService.findEntityById(1L)).thenReturn(publication);
        when(userService.findEntityById(2L)).thenReturn(requester);
        when(requestPublicationRepository.save(any(RequestPublication.class))).thenReturn(updatedRequest);

        RequestPublicationResponseDto result = requestPublicationService.update(1L, dto);

        assertNotNull(result);
        assertEquals(Status.PENDING, result.getStatus());
        assertEquals(3, result.getSeats());
        assertEquals(2L, result.getRequesterId());

        verify(requestPublicationRepository).findById(1L);
        verify(publicationService).findEntityById(1L);
        verify(userService).findEntityById(2L);
        verify(requestPublicationRepository).save(any(RequestPublication.class));
    }

    @Test
    void shouldDeleteRequestPublicationWhenRequestPublicationExists() {
        when(requestPublicationRepository.existsById(1L)).thenReturn(true);
        requestPublicationService.delete(1L);
        verify(requestPublicationRepository).existsById(1L);
        verify(requestPublicationRepository).deleteById(1L);
    }

    @Test
    void shouldCreateRequestUsingAuthenticatedUserWhenJwtFlowIsUsed() {
        RequestPublicationRequestDto dto = RequestPublicationRequestDto.builder()
                .publicationId(1L)
                .requesterId(999L)
                .requesterIsDriver(false)
                .seats(1)
                .pickupPointOrDestine("San Miguel")
                .build();

        User author = new User();
        author.setId(1L);

        Publication publication = new Publication();
        publication.setId(1L);
        publication.setAuthor(author);
        publication.setDriverToPassenger(true);

        User authenticatedRequester = new User();
        authenticatedRequester.setId(2L);

        RequestPublication savedRequest = new RequestPublication();
        savedRequest.setId(8L);
        savedRequest.setPublication(publication);
        savedRequest.setRequester(authenticatedRequester);
        savedRequest.setRequesterIsDriver(false);
        savedRequest.setSeats(1);
        savedRequest.setStatus(Status.PENDING);
        savedRequest.setCreatedAt(LocalDateTime.now());

        when(publicationService.findEntityById(1L)).thenReturn(publication);
        when(requestPublicationRepository.existsByPublication_IdAndRequester_IdAndStatusIn(eq(1L), eq(2L), any())).thenReturn(false);
        when(userService.findEntityById(2L)).thenReturn(authenticatedRequester);
        when(requestPublicationRepository.save(any(RequestPublication.class))).thenReturn(savedRequest);

        RequestPublicationResponseDto result = requestPublicationService.createAuthenticated(2L, dto);

        assertEquals(2L, result.getRequesterId());
        verify(userService).findEntityById(2L);
        verify(userService, never()).findEntityById(999L);
    }

    @Test
    void shouldUseGeocodedCoordinatesWhenCreatingRequestWithAddressOnly() {
        RequestPublicationRequestDto dto = RequestPublicationRequestDto.builder()
                .publicationId(1L)
                .requesterId(2L)
                .requesterIsDriver(false)
                .seats(1)
                .pickupPointOrDestine("San Miguel")
                .build();

        Publication publication = new Publication();
        publication.setId(1L);

        User requester = new User();
        requester.setId(2L);

        when(publicationService.findEntityById(1L)).thenReturn(publication);
        when(userService.findEntityById(2L)).thenReturn(requester);
        when(geoService.geocode("San Miguel"))
                .thenReturn(new GoogleMapsService.Coordinates(-12.078, -77.09));
        when(requestPublicationRepository.save(any(RequestPublication.class))).thenAnswer(invocation -> {
            RequestPublication request = invocation.getArgument(0);
            request.setId(1L);
            request.setStatus(Status.PENDING);
            request.setCreatedAt(LocalDateTime.now());
            return request;
        });

        RequestPublicationResponseDto result = requestPublicationService.create(dto);

        assertEquals(-12.078, result.getExternalLatitude());
        assertEquals(-77.09, result.getExternalLongitude());
        verify(geoService).geocode("San Miguel");
    }

    @Test
    void shouldThrowBusinessRuleWhenAuthorCreatesRequestForOwnPublication() {
        RequestPublicationRequestDto dto = RequestPublicationRequestDto.builder()
                .publicationId(1L)
                .requesterIsDriver(false)
                .seats(1)
                .pickupPointOrDestine("San Miguel")
                .build();

        User author = new User();
        author.setId(2L);

        Publication publication = new Publication();
        publication.setId(1L);
        publication.setAuthor(author);
        publication.setDriverToPassenger(true);

        when(publicationService.findEntityById(1L)).thenReturn(publication);

        assertThrows(BusinessRuleException.class, () -> requestPublicationService.createAuthenticated(2L, dto));
        verify(requestPublicationRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessRuleWhenRequesterRoleMatchesPublicationRole() {
        RequestPublicationRequestDto dto = RequestPublicationRequestDto.builder()
                .publicationId(1L)
                .requesterIsDriver(true)
                .seats(1)
                .pickupPointOrDestine("San Miguel")
                .build();

        User author = new User();
        author.setId(1L);

        Publication publication = new Publication();
        publication.setId(1L);
        publication.setAuthor(author);
        publication.setDriverToPassenger(true);

        when(publicationService.findEntityById(1L)).thenReturn(publication);

        assertThrows(BusinessRuleException.class, () -> requestPublicationService.createAuthenticated(2L, dto));
        verify(requestPublicationRepository, never()).save(any());
    }

    @Test
    void shouldThrowDuplicateWhenActiveRequestAlreadyExists() {
        RequestPublicationRequestDto dto = RequestPublicationRequestDto.builder()
                .publicationId(1L)
                .requesterIsDriver(false)
                .seats(1)
                .pickupPointOrDestine("San Miguel")
                .build();

        User author = new User();
        author.setId(1L);

        Publication publication = new Publication();
        publication.setId(1L);
        publication.setAuthor(author);
        publication.setDriverToPassenger(true);

        when(publicationService.findEntityById(1L)).thenReturn(publication);
        when(requestPublicationRepository.existsByPublication_IdAndRequester_IdAndStatusIn(eq(1L), eq(2L), any())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> requestPublicationService.createAuthenticated(2L, dto));
        verify(requestPublicationRepository, never()).save(any());
    }

    @Test
    void shouldThrowForbiddenWhenUpdatingRequestOwnedByAnotherUser() {
        RequestPublicationRequestDto dto = RequestPublicationRequestDto.builder()
                .publicationId(1L)
                .requesterIsDriver(false)
                .seats(1)
                .build();

        User owner = new User();
        owner.setId(2L);

        RequestPublication request = new RequestPublication();
        request.setId(1L);
        request.setRequester(owner);

        when(requestPublicationRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(ForbiddenException.class, () -> requestPublicationService.updateAuthenticated(1L, 3L, dto));
        verify(requestPublicationRepository, never()).save(any());
    }

    @Test
    void shouldThrowForbiddenWhenDeletingRequestOwnedByAnotherUser() {
        User owner = new User();
        owner.setId(2L);

        RequestPublication request = new RequestPublication();
        request.setId(1L);
        request.setRequester(owner);

        when(requestPublicationRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(ForbiddenException.class, () -> requestPublicationService.deleteAuthenticated(1L, 3L));
        verify(requestPublicationRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldReturnRequestsByPublicationWhenCurrentUserIsPublicationAuthor() {
        User author = new User();
        author.setId(1L);

        Publication publication = new Publication();
        publication.setId(20L);
        publication.setAuthor(author);

        User requester = new User();
        requester.setId(2L);

        RequestPublication request = new RequestPublication();
        request.setId(10L);
        request.setPublication(publication);
        request.setRequester(requester);
        request.setRequesterIsDriver(false);
        request.setSeats(1);
        request.setPickupPointOrDestine("San Miguel");
        request.setStatus(Status.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        when(publicationService.findEntityById(20L)).thenReturn(publication);
        when(requestPublicationRepository.findByPublication_Id(20L)).thenReturn(List.of(request));

        List<RequestPublicationResponseDto> result = requestPublicationService.findByPublication(20L, 1L);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void shouldThrowForbiddenWhenListingRequestsOfPublicationOwnedByAnotherUser() {
        User author = new User();
        author.setId(1L);

        Publication publication = new Publication();
        publication.setId(20L);
        publication.setAuthor(author);

        when(publicationService.findEntityById(20L)).thenReturn(publication);

        assertThrows(ForbiddenException.class, () -> requestPublicationService.findByPublication(20L, 2L));
    }

    @Test
    void shouldCancelPendingRequestWhenRequesterOwnsRequest() {
        User requester = new User();
        requester.setId(2L);

        Publication publication = new Publication();
        publication.setId(1L);

        RequestPublication request = new RequestPublication();
        request.setId(5L);
        request.setRequester(requester);
        request.setPublication(publication);
        request.setStatus(Status.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        when(requestPublicationRepository.findById(5L)).thenReturn(Optional.of(request));
        when(requestPublicationRepository.save(any(RequestPublication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequestPublicationResponseDto result = requestPublicationService.cancel(5L, 2L);

        assertEquals(Status.CANCELLED, result.getStatus());
        verify(applicationEventPublisher).publishEvent(any(RequestStatusChangedEvent.class));
    }

    @Test
    void shouldRejectPendingRequestWhenPublicationAuthorOwnsPublication() {
        User author = new User();
        author.setId(1L);

        User requester = new User();
        requester.setId(2L);

        Publication publication = new Publication();
        publication.setId(20L);
        publication.setAuthor(author);

        RequestPublication request = new RequestPublication();
        request.setId(5L);
        request.setRequester(requester);
        request.setPublication(publication);
        request.setStatus(Status.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        when(requestPublicationRepository.findById(5L)).thenReturn(Optional.of(request));
        when(requestPublicationRepository.save(any(RequestPublication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequestPublicationResponseDto result = requestPublicationService.reject(5L, 1L);

        assertEquals(Status.REJECTED, result.getStatus());
        verify(applicationEventPublisher).publishEvent(any(RequestStatusChangedEvent.class));
    }

    @Test
    void shouldThrowBusinessRuleWhenCancellingNonPendingRequest() {
        User requester = new User();
        requester.setId(2L);

        RequestPublication request = new RequestPublication();
        request.setId(5L);
        request.setRequester(requester);
        request.setStatus(Status.REJECTED);

        when(requestPublicationRepository.findById(5L)).thenReturn(Optional.of(request));

        assertThrows(BusinessRuleException.class, () -> requestPublicationService.cancel(5L, 2L));
    }

    @Test
    void shouldAcceptPendingRequestAndCreateRideForDriverPublication() {
        User authorDriver = new User();
        authorDriver.setId(1L);

        User requesterPassenger = new User();
        requesterPassenger.setId(2L);

        Publication publication = new Publication();
        publication.setId(10L);
        publication.setAuthor(authorDriver);
        publication.setDriverToPassenger(true);
        publication.setFromUTEC(true);
        publication.setDestinationOrOrigin("Miraflores");
        publication.setDepartureTime(LocalDateTime.now().plusHours(1));
        publication.setSeats(3);

        RequestPublication request = new RequestPublication();
        request.setId(20L);
        request.setPublication(publication);
        request.setRequester(requesterPassenger);
        request.setRequesterIsDriver(false);
        request.setSeats(1);
        request.setPickupPointOrDestine("Av. Larco 200");
        request.setStatus(Status.PENDING);
        request.setCreatedAt(LocalDateTime.now());

        Vehicle vehicle = new Vehicle();
        vehicle.setId(7L);
        vehicle.setOwner(authorDriver);
        vehicle.setSeats(4);
        publication.setVehicle(vehicle);

        when(requestPublicationRepository.findById(20L)).thenReturn(Optional.of(request));
        when(rideRepository.findByPublication_Id(10L)).thenReturn(Optional.empty());
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride ride = invocation.getArgument(0);
            if (ride.getId() == null) {
                ride.setId(100L);
            }
            return ride;
        });
        when(ridePassengerRepository.sumSeatsReservedByRide_Id(100L)).thenReturn(0);
        when(ridePassengerRepository.existsByRide_IdAndPassenger_Id(100L, 2L)).thenReturn(false);
        when(ridePassengerRepository.save(any(RidePassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestPublicationRepository.save(any(RequestPublication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequestPublicationResponseDto result = requestPublicationService.accept(20L, 1L, 7L);

        assertEquals(Status.ACCEPTED, result.getStatus());
        verify(ridePassengerRepository).save(any(RidePassenger.class));
        verify(applicationEventPublisher).publishEvent(any(RequestStatusChangedEvent.class));
    }

    @Test
    void shouldThrowBusinessRuleWhenVehicleDoesNotBelongToRealDriverOnAccept() {
        User authorDriver = new User();
        authorDriver.setId(1L);

        User requesterPassenger = new User();
        requesterPassenger.setId(2L);

        User anotherOwner = new User();
        anotherOwner.setId(3L);

        Publication publication = new Publication();
        publication.setId(10L);
        publication.setAuthor(authorDriver);
        publication.setDriverToPassenger(true);
        publication.setSeats(3);

        RequestPublication request = new RequestPublication();
        request.setId(20L);
        request.setPublication(publication);
        request.setRequester(requesterPassenger);
        request.setStatus(Status.PENDING);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(7L);
        vehicle.setOwner(anotherOwner);
        vehicle.setSeats(4);
        publication.setVehicle(vehicle);

        when(requestPublicationRepository.findById(20L)).thenReturn(Optional.of(request));

        assertThrows(BusinessRuleException.class, () -> requestPublicationService.accept(20L, 1L, 7L));
        verify(requestPublicationRepository, never()).save(any());
    }

    @Test
    void shouldThrowBusinessRuleWhenNotEnoughSeatsToAcceptPassengerInDriverPublication() {
        User authorDriver = new User();
        authorDriver.setId(1L);

        User requesterPassenger = new User();
        requesterPassenger.setId(2L);

        Publication publication = new Publication();
        publication.setId(10L);
        publication.setAuthor(authorDriver);
        publication.setDriverToPassenger(true);
        publication.setSeats(2);

        RequestPublication request = new RequestPublication();
        request.setId(20L);
        request.setPublication(publication);
        request.setRequester(requesterPassenger);
        request.setSeats(1);
        request.setStatus(Status.PENDING);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(7L);
        vehicle.setOwner(authorDriver);
        vehicle.setSeats(4);
        publication.setVehicle(vehicle);

        Ride existingRide = new Ride();
        existingRide.setId(100L);
        existingRide.setPublication(publication);
        existingRide.setDriver(authorDriver);
        existingRide.setVehicle(vehicle);

        when(requestPublicationRepository.findById(20L)).thenReturn(Optional.of(request));
        when(rideRepository.findByPublication_Id(10L)).thenReturn(Optional.of(existingRide));
        when(ridePassengerRepository.sumSeatsReservedByRide_Id(100L)).thenReturn(2);

        assertThrows(BusinessRuleException.class, () -> requestPublicationService.accept(20L, 1L, 7L));
    }

    @Test
    void shouldAcceptDriverForPassengerPublicationAndRejectOtherPendingRequests() {
        User publicationAuthorPassenger = new User();
        publicationAuthorPassenger.setId(1L);

        User requesterDriver = new User();
        requesterDriver.setId(3L);

        Publication publication = new Publication();
        publication.setId(10L);
        publication.setAuthor(publicationAuthorPassenger);
        publication.setDriverToPassenger(false);
        publication.setFromUTEC(false);
        publication.setDestinationOrOrigin("UTEC");
        publication.setDepartureTime(LocalDateTime.now().plusHours(1));
        publication.setSeats(1);

        RequestPublication acceptedRequest = new RequestPublication();
        acceptedRequest.setId(20L);
        acceptedRequest.setPublication(publication);
        acceptedRequest.setRequester(requesterDriver);
        acceptedRequest.setRequesterIsDriver(true);
        acceptedRequest.setSeats(3);
        acceptedRequest.setPickupPointOrDestine("Puerta UTEC");
        acceptedRequest.setStatus(Status.PENDING);
        acceptedRequest.setCreatedAt(LocalDateTime.now());

        RequestPublication anotherPending = new RequestPublication();
        anotherPending.setId(21L);
        anotherPending.setPublication(publication);
        anotherPending.setRequester(new User());
        anotherPending.setStatus(Status.PENDING);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(7L);
        vehicle.setOwner(requesterDriver);
        vehicle.setSeats(4);

        when(requestPublicationRepository.findById(20L)).thenReturn(Optional.of(acceptedRequest));
        when(vehicleService.findEntityById(7L)).thenReturn(vehicle);
        when(rideRepository.findByPublication_Id(10L)).thenReturn(Optional.empty());
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride ride = invocation.getArgument(0);
            if (ride.getId() == null) {
                ride.setId(100L);
            }
            return ride;
        });
        when(ridePassengerRepository.existsByRide_IdAndPassenger_Id(100L, 1L)).thenReturn(false);
        when(ridePassengerRepository.save(any(RidePassenger.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestPublicationRepository.findByPublication_IdAndStatus(10L, Status.PENDING))
                .thenReturn(List.of(acceptedRequest, anotherPending));
        when(requestPublicationRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(requestPublicationRepository.save(any(RequestPublication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequestPublicationResponseDto result = requestPublicationService.accept(20L, 1L, 7L);

        assertEquals(Status.ACCEPTED, result.getStatus());
        assertEquals(Status.REJECTED, anotherPending.getStatus());
        verify(applicationEventPublisher, times(2)).publishEvent(any(RequestStatusChangedEvent.class));
    }

    @Test
    void shouldRejectDriverRequestWhenRequesterHasNoVehicle() {
        User passengerAuthor = new User();
        passengerAuthor.setId(1L);
        Publication publication = new Publication();
        publication.setId(10L);
        publication.setAuthor(passengerAuthor);
        publication.setDriverToPassenger(false);
        RequestPublicationRequestDto dto = RequestPublicationRequestDto.builder()
                .publicationId(10L)
                .requesterIsDriver(true)
                .seats(2)
                .pickupPointOrDestine("UTEC")
                .build();

        when(publicationService.findEntityById(10L)).thenReturn(publication);
        when(requestPublicationRepository.existsByPublication_IdAndRequester_IdAndStatusIn(eq(10L), eq(2L), any()))
                .thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> requestPublicationService.createAuthenticated(2L, dto));
        verify(requestPublicationRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenRequestIsNoLongerPending() {
        User requester = new User();
        requester.setId(2L);
        RequestPublication request = new RequestPublication();
        request.setId(5L);
        request.setRequester(requester);
        request.setStatus(Status.ACCEPTED);
        when(requestPublicationRepository.findById(5L)).thenReturn(Optional.of(request));

        RequestPublicationRequestDto dto = RequestPublicationRequestDto.builder()
                .publicationId(10L)
                .requesterIsDriver(false)
                .seats(1)
                .build();

        assertThrows(BusinessRuleException.class, () -> requestPublicationService.updateAuthenticated(5L, 2L, dto));
        verify(requestPublicationRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateThatChangesPublication() {
        User requester = new User();
        requester.setId(2L);
        Publication publication = new Publication();
        publication.setId(10L);
        RequestPublication request = new RequestPublication();
        request.setId(5L);
        request.setRequester(requester);
        request.setPublication(publication);
        request.setRequesterIsDriver(false);
        request.setStatus(Status.PENDING);
        when(requestPublicationRepository.findById(5L)).thenReturn(Optional.of(request));

        RequestPublicationRequestDto dto = RequestPublicationRequestDto.builder()
                .publicationId(11L)
                .requesterIsDriver(false)
                .seats(1)
                .build();

        assertThrows(BusinessRuleException.class, () -> requestPublicationService.updateAuthenticated(5L, 2L, dto));
        verify(requestPublicationRepository, never()).save(any());
    }

    @Test
    void shouldRejectDriverOfferThatDoesNotCoverPassengerSeats() {
        User passenger = new User();
        passenger.setId(1L);
        User driver = new User();
        driver.setId(2L);
        Publication publication = new Publication();
        publication.setId(10L);
        publication.setAuthor(passenger);
        publication.setDriverToPassenger(false);
        publication.setSeats(3);
        RequestPublication request = new RequestPublication();
        request.setId(20L);
        request.setPublication(publication);
        request.setRequester(driver);
        request.setRequesterIsDriver(true);
        request.setSeats(2);
        request.setStatus(Status.PENDING);
        Vehicle vehicle = new Vehicle();
        vehicle.setId(7L);
        vehicle.setOwner(driver);
        vehicle.setSeats(4);

        when(requestPublicationRepository.findById(20L)).thenReturn(Optional.of(request));
        when(vehicleService.findEntityById(7L)).thenReturn(vehicle);

        assertThrows(BusinessRuleException.class, () -> requestPublicationService.accept(20L, 1L, 7L));
        verify(rideRepository, never()).save(any());
    }
}
