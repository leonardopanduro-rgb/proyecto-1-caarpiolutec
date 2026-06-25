package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.RidePassengerRequestDto;
import com.dbp.democarpultec.dto.RidePassengerResponseDto;
import com.dbp.democarpultec.model.Ride;
import com.dbp.democarpultec.model.RidePassenger;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.repository.RidePassengerRepository;
import com.dbp.democarpultec.service.impl.RidePassengerServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RidePassengerServiceTest {
    @Mock
    private RidePassengerRepository ridePassengerRepository;

    @Mock
    private UserService userService;

    @Mock
    private RideService rideService;

    @InjectMocks
    private RidePassengerServiceImpl ridePassengerService;

    @Test
    void shouldCreateRidePassengerWhenValidData() {
        RidePassengerRequestDto dto = RidePassengerRequestDto.builder()
                .passengerId(1L)
                .rideId(2L)
                .seatsReserved(2)
                .pickupPoint("San Miguel")
                .build();

        User passenger = new User();
        passenger.setId(1L);

        Ride ride = new Ride();
        ride.setId(2L);

        RidePassenger saved = new RidePassenger();
        saved.setId(1L);
        saved.setPassenger(passenger);
        saved.setRide(ride);
        saved.setSeatsReserved(2);
        saved.setPickupPoint("San Miguel");

        when(userService.findEntityById(1L)).thenReturn(passenger);
        when(rideService.findEntityById(2L)).thenReturn(ride);
        when(ridePassengerRepository.save(any(RidePassenger.class))).thenReturn(saved);

        RidePassengerResponseDto result = ridePassengerService.create(dto);

        assertNotNull(result);
        assertEquals(1L, result.getPassengerId());
        assertEquals(2L, result.getRideId());
        assertEquals(2, result.getSeatsReserved());
        assertEquals("San Miguel", result.getPickupPoint());

        verify(userService).findEntityById(1L);
        verify(rideService).findEntityById(2L);
        verify(ridePassengerRepository).save(any(RidePassenger.class));
    }

    @Test
    void shouldReturnRidePassengerWhenIdExists() {
        User passenger = new User();
        passenger.setId(1L);

        Ride ride = new Ride();
        ride.setId(2L);

        RidePassenger entity = new RidePassenger();
        entity.setId(10L);
        entity.setPassenger(passenger);
        entity.setRide(ride);
        entity.setSeatsReserved(3);
        entity.setPickupPoint("Centro");

        when(ridePassengerRepository.findById(10L)).thenReturn(Optional.of(entity));

        RidePassengerResponseDto result = ridePassengerService.findById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(1L, result.getPassengerId());
        assertEquals(2L, result.getRideId());
        assertEquals(3, result.getSeatsReserved());

        verify(ridePassengerRepository).findById(10L);
    }

    @Test
    void shouldThrowExceptionWhenRidePassengerNotFound() {
        when(ridePassengerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            ridePassengerService.findById(99L);
        });

        verify(ridePassengerRepository).findById(99L);
    }

    @Test
    void shouldUpdateRidePassengerWhenValidData() {
        RidePassengerRequestDto dto = RidePassengerRequestDto.builder()
                .passengerId(1L)
                .rideId(2L)
                .seatsReserved(4)
                .pickupPoint("Miraflores")
                .build();

        User passenger = new User();
        passenger.setId(1L);

        Ride ride = new Ride();
        ride.setId(2L);

        RidePassenger existing = new RidePassenger();
        existing.setId(10L);

        RidePassenger updated = new RidePassenger();
        updated.setId(10L);
        updated.setPassenger(passenger);
        updated.setRide(ride);
        updated.setSeatsReserved(4);
        updated.setPickupPoint("Miraflores");

        when(ridePassengerRepository.findById(10L)).thenReturn(Optional.of(existing));
        when(userService.findEntityById(1L)).thenReturn(passenger);
        when(rideService.findEntityById(2L)).thenReturn(ride);
        when(ridePassengerRepository.save(any(RidePassenger.class))).thenReturn(updated);

        RidePassengerResponseDto result = ridePassengerService.update(10L, dto);

        assertNotNull(result);
        assertEquals(4, result.getSeatsReserved());
        assertEquals("Miraflores", result.getPickupPoint());

        verify(ridePassengerRepository).findById(10L);
        verify(userService).findEntityById(1L);
        verify(rideService).findEntityById(2L);
        verify(ridePassengerRepository).save(any(RidePassenger.class));
    }

    @Test
    void shouldDeleteRidePassengerWhenExists() {
        when(ridePassengerRepository.existsById(10L)).thenReturn(true);
        ridePassengerService.delete(10L);
        verify(ridePassengerRepository).existsById(10L);
        verify(ridePassengerRepository).deleteById(10L);
    }
}
