package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.VehicleRequestDto;
import com.dbp.democarpultec.dto.VehicleResponseDto;
import com.dbp.democarpultec.exception.BusinessRuleException;
import com.dbp.democarpultec.exception.ForbiddenException;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.Vehicle;
import com.dbp.democarpultec.repository.VehicleRepository;
import com.dbp.democarpultec.service.impl.VehicleServiceImpl;
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
public class VehicleServiceTest {
    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    @Test
    void shouldCreateVehicleWhenValidData(){
        VehicleRequestDto dto = VehicleRequestDto.builder()
                .plate("ABC-123")
                .brand("Toyota")
                .model("Corolla")
                .color("Rojo")
                .seats(4)
                .build();

        User owner = new User();
        owner.setId(1L);
        owner.setName("Juan");

        Vehicle savedVehicle = new Vehicle();
        savedVehicle.setId(1L);
        savedVehicle.setOwner(owner);
        savedVehicle.setPlate("ABC-123");
        savedVehicle.setBrand("Toyota");
        savedVehicle.setModel("Corolla");
        savedVehicle.setColor("Rojo");
        savedVehicle.setSeats(4);

        when(userService.findEntityById(1L)).thenReturn(owner);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(savedVehicle);

        VehicleResponseDto result = vehicleService.createAuthenticated(1L, dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ABC-123", result.getPlate());
        assertEquals("Toyota", result.getBrand());
        assertEquals(1L, result.getOwnerId());

        verify(userService).findEntityById(1L);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void shouldReturnVehicleWhenIdExists(){
        User owner = new User();
        owner.setId(1L);
        owner.setName("Juan");

        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setOwner(owner);
        vehicle.setPlate("ABC-123");
        vehicle.setBrand("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setColor("Rojo");
        vehicle.setSeats(4);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleResponseDto result = vehicleService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ABC-123", result.getPlate());
        assertEquals("Toyota", result.getBrand());
        assertEquals("Corolla", result.getModel());
        assertEquals(1L, result.getOwnerId());

        verify(vehicleRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenVehicleNotFound(){
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            vehicleService.findById(99L);
        });

        verify(vehicleRepository).findById(99L);
    }

    @Test
    void shouldUpdateVehicleWhenValidData(){
        VehicleRequestDto dto = VehicleRequestDto.builder()
                .plate("XYZ-999")
                .brand("Honda")
                .model("Civic")
                .color("Negro")
                .seats(5)
                .build();

        User owner = new User();
        owner.setId(1L);
        owner.setName("Juan");

        Vehicle existingVehicle = new Vehicle();
        existingVehicle.setId(1L);
        existingVehicle.setOwner(owner);
        existingVehicle.setPlate("OLD-123");
        existingVehicle.setBrand("Toyota");

        Vehicle updatedVehicle = new Vehicle();
        updatedVehicle.setId(1L);
        updatedVehicle.setOwner(owner);
        updatedVehicle.setPlate("XYZ-999");
        updatedVehicle.setBrand("Honda");
        updatedVehicle.setModel("Civic");
        updatedVehicle.setColor("Negro");
        updatedVehicle.setSeats(5);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(updatedVehicle);

        VehicleResponseDto result = vehicleService.updateAuthenticated(1L, 1L, dto);

        assertNotNull(result);
        assertEquals("XYZ-999", result.getPlate());
        assertEquals("Honda", result.getBrand());
        assertEquals("Civic", result.getModel());
        assertEquals(5, result.getSeats());

        verify(vehicleRepository).findById(1L);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void shouldDeleteVehicleWhenVehicleExists(){
        User owner = new User();
        owner.setId(1L);
        Vehicle vehicle = new Vehicle();
        vehicle.setOwner(owner);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        vehicleService.deleteAuthenticated(1L, 1L);
        verify(vehicleRepository).deleteById(1L);
    }

    @Test
    void shouldRejectThirdVehicleForSameOwner() {
        VehicleRequestDto dto = VehicleRequestDto.builder()
                .plate("ABC-123")
                .brand("Toyota")
                .model("Corolla")
                .seats(4)
                .build();

        when(vehicleRepository.countByOwner_Id(1L)).thenReturn(2L);

        assertThrows(BusinessRuleException.class, () -> vehicleService.createAuthenticated(1L, dto));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenVehicleBelongsToAnotherUser() {
        User owner = new User();
        owner.setId(2L);
        Vehicle vehicle = new Vehicle();
        vehicle.setOwner(owner);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleRequestDto dto = VehicleRequestDto.builder()
                .plate("ABC-123")
                .brand("Toyota")
                .model("Corolla")
                .seats(4)
                .build();

        assertThrows(ForbiddenException.class, () -> vehicleService.updateAuthenticated(1L, 1L, dto));
        verify(vehicleRepository, never()).save(any());
    }
}
