package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.VehicleRequestDto;
import com.dbp.democarpultec.dto.VehicleResponseDto;
import com.dbp.democarpultec.model.Vehicle;

import java.util.List;

public interface VehicleService {
    List<VehicleResponseDto> findAll();

    VehicleResponseDto findById(Long id);

    VehicleResponseDto createAuthenticated(Long authenticatedUserId, VehicleRequestDto dto);

    VehicleResponseDto updateAuthenticated(Long id, Long authenticatedUserId, VehicleRequestDto dto);

    void deleteAuthenticated(Long id, Long authenticatedUserId);

    Vehicle findEntityById(Long id);

    Vehicle findOwnedVehicleById(Long vehicleId, Long ownerId);

    boolean userHasVehicle(Long ownerId);
}
