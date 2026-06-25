package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.PublicationRequestDto;
import com.dbp.democarpultec.dto.PublicationResponseDto;
import com.dbp.democarpultec.exception.BusinessRuleException;
import com.dbp.democarpultec.exception.ForbiddenException;
import com.dbp.democarpultec.model.Publication;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.Vehicle;
import com.dbp.democarpultec.repository.PublicationRepository;
import com.dbp.democarpultec.service.impl.PublicationServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PublicationServiceTest {
    @Mock
    private PublicationRepository publicationRepository;
    @Mock
    private UserService userService;
    @Mock
    private GeoService geoService;
    @Mock
    private VehicleService vehicleService;
    @InjectMocks
    private PublicationServiceImpl publicationService;

    @Test
    void shouldCreateDriverPublicationWithOwnedVehicle() {
        User author = user(1L);
        Vehicle vehicle = vehicle(author, 7L, 4);
        PublicationRequestDto dto = driverPublicationDto(3);

        when(userService.findEntityById(1L)).thenReturn(author);
        when(vehicleService.findOwnedVehicleById(7L, 1L)).thenReturn(vehicle);
        when(publicationRepository.save(any(Publication.class))).thenAnswer(invocation -> {
            Publication publication = invocation.getArgument(0);
            publication.setId(1L);
            return publication;
        });

        PublicationResponseDto result = publicationService.createAuthenticated(1L, dto);

        assertEquals(1L, result.getAuthorId());
        assertEquals(7L, result.getVehicleId());
        assertEquals(3, result.getSeats());
        verify(publicationRepository).save(any(Publication.class));
    }

    @Test
    void shouldIgnoreAuthorIdWhenCreatingUsingAuthenticatedUser() {
        User authenticatedAuthor = user(1L);
        Vehicle vehicle = vehicle(authenticatedAuthor, 7L, 4);
        PublicationRequestDto dto = driverPublicationDto(3);
        dto.setAuthorId(999L);

        when(userService.findEntityById(1L)).thenReturn(authenticatedAuthor);
        when(vehicleService.findOwnedVehicleById(7L, 1L)).thenReturn(vehicle);
        when(publicationRepository.save(any(Publication.class))).thenAnswer(invocation -> {
            Publication publication = invocation.getArgument(0);
            publication.setId(10L);
            return publication;
        });

        PublicationResponseDto result = publicationService.createAuthenticated(1L, dto);

        assertEquals(1L, result.getAuthorId());
        verify(userService, never()).findEntityById(999L);
    }

    @Test
    void shouldRejectDriverPublicationWithoutVehicle() {
        PublicationRequestDto dto = driverPublicationDto(2);
        dto.setVehicleId(null);
        when(userService.findEntityById(1L)).thenReturn(user(1L));

        assertThrows(BusinessRuleException.class, () -> publicationService.createAuthenticated(1L, dto));
        verify(publicationRepository, never()).save(any());
    }

    @Test
    void shouldRejectDriverPublicationWhenSeatsExceedVehicleCapacity() {
        User author = user(1L);
        when(userService.findEntityById(1L)).thenReturn(author);
        when(vehicleService.findOwnedVehicleById(7L, 1L)).thenReturn(vehicle(author, 7L, 4));

        assertThrows(BusinessRuleException.class,
                () -> publicationService.createAuthenticated(1L, driverPublicationDto(5)));
        verify(publicationRepository, never()).save(any());
    }

    @Test
    void shouldUseGeocodedCoordinatesWhenCreatingPublicationWithAddressOnly() {
        User author = user(1L);
        when(userService.findEntityById(1L)).thenReturn(author);
        when(vehicleService.findOwnedVehicleById(7L, 1L)).thenReturn(vehicle(author, 7L, 4));
        when(geoService.geocode("Miraflores"))
                .thenReturn(new GoogleMapsService.Coordinates(-12.121, -77.031));
        when(publicationRepository.save(any(Publication.class))).thenAnswer(invocation -> {
            Publication publication = invocation.getArgument(0);
            publication.setId(1L);
            return publication;
        });

        PublicationResponseDto result = publicationService.createAuthenticated(1L, driverPublicationDto(2));

        assertEquals(-12.121, result.getExternalLatitude());
        assertEquals(-77.031, result.getExternalLongitude());
    }

    @Test
    void shouldUpdatePassengerPublicationOwnedByAuthenticatedUser() {
        User author = user(1L);
        Publication existing = new Publication();
        existing.setId(1L);
        existing.setAuthor(author);
        PublicationRequestDto dto = passengerPublicationDto();

        when(publicationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(publicationRepository.save(any(Publication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PublicationResponseDto result = publicationService.updateAuthenticated(1L, 1L, dto);

        assertEquals("Viaje a San Isidro", result.getTitulo());
        assertNull(result.getVehicleId());
    }

    @Test
    void shouldReturnPublicationWhenIdExists() {
        Publication publication = new Publication();
        publication.setId(1L);
        publication.setAuthor(user(1L));
        publication.setTitulo("Viaje");
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));

        PublicationResponseDto result = publicationService.findById(1L);

        assertEquals(1L, result.getId());
        assertEquals(1L, result.getAuthorId());
    }

    @Test
    void shouldThrowExceptionWhenPublicationNotFound() {
        when(publicationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> publicationService.findById(99L));
    }

    @Test
    void shouldThrowForbiddenWhenUpdatingPublicationOwnedByAnotherUser() {
        Publication publication = new Publication();
        publication.setId(1L);
        publication.setAuthor(user(1L));
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));

        assertThrows(ForbiddenException.class,
                () -> publicationService.updateAuthenticated(1L, 2L, passengerPublicationDto()));
        verify(publicationRepository, never()).save(any());
    }

    @Test
    void shouldThrowForbiddenWhenDeletingPublicationOwnedByAnotherUser() {
        Publication publication = new Publication();
        publication.setId(1L);
        publication.setAuthor(user(1L));
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(publication));

        assertThrows(ForbiddenException.class, () -> publicationService.deleteAuthenticated(1L, 2L));
        verify(publicationRepository, never()).deleteById(anyLong());
    }

    private PublicationRequestDto driverPublicationDto(int seats) {
        return PublicationRequestDto.builder()
                .fromUTEC(true)
                .driverToPassenger(true)
                .vehicleId(7L)
                .seats(seats)
                .titulo("Viaje a Miraflores")
                .descripcion("Salida despues de clases")
                .destinationOrOrigin("Miraflores")
                .departureTime(LocalDateTime.now())
                .build();
    }

    private PublicationRequestDto passengerPublicationDto() {
        return PublicationRequestDto.builder()
                .fromUTEC(false)
                .driverToPassenger(false)
                .seats(2)
                .titulo("Viaje a San Isidro")
                .destinationOrOrigin("San Isidro")
                .departureTime(LocalDateTime.now())
                .build();
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    private Vehicle vehicle(User owner, Long id, int seats) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setOwner(owner);
        vehicle.setSeats(seats);
        return vehicle;
    }
}
