package com.dbp.democarpultec.service.impl;

import com.dbp.democarpultec.dto.PublicationRequestDto;
import com.dbp.democarpultec.dto.PublicationResponseDto;
import com.dbp.democarpultec.exception.BusinessRuleException;
import com.dbp.democarpultec.exception.ForbiddenException;
import com.dbp.democarpultec.model.Publication;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.Vehicle;
import com.dbp.democarpultec.repository.PublicationRepository;
import com.dbp.democarpultec.service.GeoService;
import com.dbp.democarpultec.service.GoogleMapsService;
import com.dbp.democarpultec.service.PublicationService;
import com.dbp.democarpultec.service.UserService;
import com.dbp.democarpultec.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicationServiceImpl implements PublicationService {

    private final PublicationRepository publicationRepository;
    private final UserService userService;
    private final VehicleService vehicleService;
    private final GeoService geoService;

    public List<PublicationResponseDto> findAll() {
        return publicationRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    public PublicationResponseDto findById(Long id) {
        return toResponseDto(findEntityById(id));
    }

    public PublicationResponseDto create(PublicationRequestDto dto) {
        Publication publication = new Publication();
        updateEntity(publication, dto);
        return toResponseDto(publicationRepository.save(publication));
    }

    public PublicationResponseDto createAuthenticated(Long authenticatedUserId, PublicationRequestDto dto) {
        Publication publication = new Publication();
        User author = userService.findEntityById(authenticatedUserId);
        publication.setAuthor(author);
        updateEntityData(publication, dto, author);
        return toResponseDto(publicationRepository.save(publication));
    }

    public PublicationResponseDto update(Long id, PublicationRequestDto dto) {
        Publication publication = findEntityById(id);
        updateEntity(publication, dto);
        return toResponseDto(publicationRepository.save(publication));
    }

    public PublicationResponseDto updateAuthenticated(Long id, Long authenticatedUserId, PublicationRequestDto dto) {
        Publication publication = findEntityById(id);
        validateAuthorOwnership(publication, authenticatedUserId);
        updateEntityData(publication, dto, publication.getAuthor());
        return toResponseDto(publicationRepository.save(publication));
    }

    public void delete(Long id) {
        if (!publicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Publication not found with id " + id);
        }
        publicationRepository.deleteById(id);
    }

    public void deleteAuthenticated(Long id, Long authenticatedUserId) {
        Publication publication = findEntityById(id);
        validateAuthorOwnership(publication, authenticatedUserId);
        publicationRepository.deleteById(id);
    }

    public Publication findEntityById(Long id) {
        return publicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Publication not found with id " + id));
    }

    private void updateEntity(Publication publication, PublicationRequestDto dto) {
        User author = userService.findEntityById(dto.getAuthorId());
        publication.setAuthor(author);
        updateEntityData(publication, dto, author);
    }

    private void updateEntityData(Publication publication, PublicationRequestDto dto, User author) {
        Double latitude = dto.getExternalLatitude();
        Double longitude = dto.getExternalLongitude();
        validateCoordinatePair(latitude, longitude, "publication");
        if (latitude == null && longitude == null) {
            GoogleMapsService.Coordinates coordinates = geoService.geocode(dto.getDestinationOrOrigin());
            if (coordinates != null) {
                latitude = coordinates.latitude();
                longitude = coordinates.longitude();
            }
        }

        publication.setFromUTEC(dto.getFromUTEC());
        publication.setDriverToPassenger(dto.getDriverToPassenger());
        publication.setSeats(dto.getSeats());
        publication.setTitulo(dto.getTitulo());
        publication.setDescripcion(dto.getDescripcion());
        publication.setDestinationOrOrigin(dto.getDestinationOrOrigin());
        publication.setExternalLatitude(latitude);
        publication.setExternalLongitude(longitude);
        publication.setDepartureTime(dto.getDepartureTime());
        assignVehicleWhenDriverPublication(publication, dto, author);
    }

    private void assignVehicleWhenDriverPublication(Publication publication, PublicationRequestDto dto, User author) {
        if (Boolean.TRUE.equals(dto.getDriverToPassenger())) {
            if (dto.getVehicleId() == null) {
                throw new BusinessRuleException("A driver publication requires a vehicle");
            }
            Vehicle vehicle = vehicleService.findOwnedVehicleById(dto.getVehicleId(), author.getId());
            if (dto.getSeats() > vehicle.getSeats()) {
                throw new BusinessRuleException("Offered seats cannot exceed vehicle capacity");
            }
            publication.setVehicle(vehicle);
            return;
        }

        if (dto.getVehicleId() != null) {
            throw new BusinessRuleException("A passenger publication cannot select a vehicle");
        }
        publication.setVehicle(null);
    }

    private void validateAuthorOwnership(Publication publication, Long authenticatedUserId) {
        if (!publication.getAuthor().getId().equals(authenticatedUserId)) {
            throw new ForbiddenException("You are not the owner of this publication");
        }
    }

    private PublicationResponseDto toResponseDto(Publication publication) {
        return PublicationResponseDto.builder()
                .id(publication.getId())
                .fromUTEC(publication.getFromUTEC())
                .driverToPassenger(publication.getDriverToPassenger())
                .seats(publication.getSeats())
                .titulo(publication.getTitulo())
                .descripcion(publication.getDescripcion())
                .destinationOrOrigin(publication.getDestinationOrOrigin())
                .externalLatitude(publication.getExternalLatitude())
                .externalLongitude(publication.getExternalLongitude())
                .distanceToUtecKm(geoService.distanceToUtecKm(
                        publication.getExternalLatitude(),
                        publication.getExternalLongitude()
                ))
                .departureTime(publication.getDepartureTime())
                .authorId(publication.getAuthor().getId())
                .vehicleId(publication.getVehicle() == null ? null : publication.getVehicle().getId())
                .rideId(publication.getRide() == null ? null : publication.getRide().getId())
                .build();
    }

    private void validateCoordinatePair(Double latitude, Double longitude, String resourceName) {
        boolean hasOnlyOneCoordinate = (latitude == null && longitude != null)
                || (latitude != null && longitude == null);
        if (hasOnlyOneCoordinate) {
            throw new BusinessRuleException("Both coordinates are required for " + resourceName + " location");
        }
    }
}
