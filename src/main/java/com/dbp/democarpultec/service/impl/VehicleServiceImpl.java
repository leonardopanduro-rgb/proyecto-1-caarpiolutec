package com.dbp.democarpultec.service.impl;

import com.dbp.democarpultec.dto.VehicleRequestDto;
import com.dbp.democarpultec.dto.VehicleResponseDto;
import com.dbp.democarpultec.exception.BusinessRuleException;
import com.dbp.democarpultec.exception.ForbiddenException;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.Vehicle;
import com.dbp.democarpultec.repository.VehicleRepository;
import com.dbp.democarpultec.service.UserService;
import com.dbp.democarpultec.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private static final int MAX_VEHICLES_PER_USER = 2;

    private final VehicleRepository vehicleRepository;
    private final UserService userService;

    public List<VehicleResponseDto> findAll() {
        return vehicleRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    public VehicleResponseDto findById(Long id) {
        return toResponseDto(findEntityById(id));
    }

    public VehicleResponseDto createAuthenticated(Long authenticatedUserId, VehicleRequestDto dto) {
        if (vehicleRepository.countByOwner_Id(authenticatedUserId) >= MAX_VEHICLES_PER_USER) {
            throw new BusinessRuleException("A user can register up to 2 vehicles");
        }

        User owner = userService.findEntityById(authenticatedUserId);
        Vehicle vehicle = new Vehicle();
        vehicle.setOwner(owner);
        updateEntityData(vehicle, dto);
        return toResponseDto(vehicleRepository.save(vehicle));
    }

    public VehicleResponseDto updateAuthenticated(Long id, Long authenticatedUserId, VehicleRequestDto dto) {
        Vehicle vehicle = findEntityById(id);
        validateOwnership(vehicle, authenticatedUserId);
        updateEntityData(vehicle, dto);
        return toResponseDto(vehicleRepository.save(vehicle));
    }

    public void deleteAuthenticated(Long id, Long authenticatedUserId) {
        Vehicle vehicle = findEntityById(id);
        validateOwnership(vehicle, authenticatedUserId);
        vehicleRepository.deleteById(id);
    }

    public Vehicle findEntityById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle not found with id " + id));
    }

    public Vehicle findOwnedVehicleById(Long vehicleId, Long ownerId) {
        Vehicle vehicle = findEntityById(vehicleId);
        validateOwnership(vehicle, ownerId);
        return vehicle;
    }

    public boolean userHasVehicle(Long ownerId) {
        return vehicleRepository.existsByOwner_Id(ownerId);
    }

    private void updateEntityData(Vehicle vehicle, VehicleRequestDto dto) {
        vehicle.setPlate(dto.getPlate());
        vehicle.setBrand(dto.getBrand());
        vehicle.setModel(dto.getModel());
        vehicle.setColor(dto.getColor());
        vehicle.setSeats(dto.getSeats());
    }

    private void validateOwnership(Vehicle vehicle, Long authenticatedUserId) {
        if (!vehicle.getOwner().getId().equals(authenticatedUserId)) {
            throw new ForbiddenException("You are not the owner of this vehicle");
        }
    }

    private VehicleResponseDto toResponseDto(Vehicle vehicle) {
        return VehicleResponseDto.builder()
                .id(vehicle.getId())
                .ownerId(vehicle.getOwner().getId())
                .plate(vehicle.getPlate())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .color(vehicle.getColor())
                .seats(vehicle.getSeats())
                .build();
    }
}
