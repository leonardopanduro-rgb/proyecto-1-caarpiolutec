package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.RideRequestDto;
import com.dbp.democarpultec.dto.RideResponseDto;
import com.dbp.democarpultec.model.Publication;
import com.dbp.democarpultec.model.Ride;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.Vehicle;
import com.dbp.democarpultec.repository.RideRepository;
import com.dbp.democarpultec.service.impl.RideServiceImpl;
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
public class RideServiceTest {
    @Mock
    private RideRepository rideRepository;

    @Mock
    private PublicationService publicationService;

    @Mock
    private UserService userService;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private RideServiceImpl rideService;

    @Test
    void shouldCreateRideWhenValidData() {
        RideRequestDto dto = RideRequestDto.builder()
                .publicationId(1L)
                .driverId(2L)
                .vehicleId(3L)
                .fromUTEC(true)
                .destinationOrOrigin("Miraflores")
                .departureTime(LocalDateTime.now())
                .build();

        Publication publication = new Publication();
        publication.setId(1L);

        User driver = new User();
        driver.setId(2L);
        driver.setName("Carlos");

        Vehicle vehicle = new Vehicle();
        vehicle.setId(3L);
        vehicle.setPlate("ABC-123");

        Ride savedRide = new Ride();
        savedRide.setId(1L);
        savedRide.setPublication(publication);
        savedRide.setDriver(driver);
        savedRide.setVehicle(vehicle);
        savedRide.setFromUTEC(true);
        savedRide.setDestinationOrOrigin("Miraflores");
        savedRide.setDepartureTime(dto.getDepartureTime());

        when(publicationService.findEntityById(1L)).thenReturn(publication);
        when(userService.findEntityById(2L)).thenReturn(driver);
        when(vehicleService.findEntityById(3L)).thenReturn(vehicle);
        when(rideRepository.save(any(Ride.class))).thenReturn(savedRide);

        RideResponseDto result = rideService.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getPublicationId());
        assertEquals(2L, result.getDriverId());
        assertEquals(3L, result.getVehicleId());
        assertEquals("Miraflores", result.getDestinationOrOrigin());

        verify(publicationService).findEntityById(1L);
        verify(userService).findEntityById(2L);
        verify(vehicleService).findEntityById(3L);
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void shouldReturnRideWhenIdExists() {
        Publication publication = new Publication();
        publication.setId(1L);

        User driver = new User();
        driver.setId(2L);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(3L);

        Ride ride = new Ride();
        ride.setId(1L);
        ride.setPublication(publication);
        ride.setDriver(driver);
        ride.setVehicle(vehicle);
        ride.setFromUTEC(true);
        ride.setDestinationOrOrigin("Miraflores");
        ride.setDepartureTime(LocalDateTime.now());

        when(rideRepository.findById(1L)).thenReturn(Optional.of(ride));

        RideResponseDto result = rideService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getPublicationId());
        assertEquals(2L, result.getDriverId());
        assertEquals(3L, result.getVehicleId());
        assertEquals("Miraflores", result.getDestinationOrOrigin());

        verify(rideRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenRideNotFound() {
        when(rideRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            rideService.findById(99L);
        });

        verify(rideRepository).findById(99L);
    }

    @Test
    void shouldUpdateRideWhenValidData() {
        RideRequestDto dto = RideRequestDto.builder()
                .publicationId(1L)
                .driverId(2L)
                .vehicleId(3L)
                .fromUTEC(false)
                .destinationOrOrigin("Centro de Lima")
                .departureTime(LocalDateTime.now())
                .build();

        Publication publication = new Publication();
        publication.setId(1L);

        User driver = new User();
        driver.setId(2L);

        Vehicle vehicle = new Vehicle();
        vehicle.setId(3L);

        Ride existingRide = new Ride();
        existingRide.setId(1L);
        existingRide.setFromUTEC(true);
        existingRide.setDestinationOrOrigin("Antiguo");

        Ride updatedRide = new Ride();
        updatedRide.setId(1L);
        updatedRide.setPublication(publication);
        updatedRide.setDriver(driver);
        updatedRide.setVehicle(vehicle);
        updatedRide.setFromUTEC(false);
        updatedRide.setDestinationOrOrigin("Centro de Lima");
        updatedRide.setDepartureTime(dto.getDepartureTime());

        when(rideRepository.findById(1L)).thenReturn(Optional.of(existingRide));
        when(publicationService.findEntityById(1L)).thenReturn(publication);
        when(userService.findEntityById(2L)).thenReturn(driver);
        when(vehicleService.findEntityById(3L)).thenReturn(vehicle);
        when(rideRepository.save(any(Ride.class))).thenReturn(updatedRide);

        RideResponseDto result = rideService.update(1L, dto);

        assertNotNull(result);
        assertEquals(false, result.getFromUTEC());
        assertEquals("Centro de Lima", result.getDestinationOrOrigin());
        assertEquals(2L, result.getDriverId());
        assertEquals(3L, result.getVehicleId());

        verify(rideRepository).findById(1L);
        verify(publicationService).findEntityById(1L);
        verify(userService).findEntityById(2L);
        verify(vehicleService).findEntityById(3L);
        verify(rideRepository).save(any(Ride.class));
    }

    @Test
    void shouldDeleteRideWhenRideExists() {
        when(rideRepository.existsById(1L)).thenReturn(true);
        rideService.delete(1L);
        verify(rideRepository).existsById(1L);
        verify(rideRepository).deleteById(1L);
    }
}
