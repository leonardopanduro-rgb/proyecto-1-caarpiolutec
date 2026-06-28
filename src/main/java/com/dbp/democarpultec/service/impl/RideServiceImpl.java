package com.dbp.democarpultec.service.impl;

import com.dbp.democarpultec.dto.RideRequestDto;
import com.dbp.democarpultec.dto.RideResponseDto;
import com.dbp.democarpultec.model.Ride;
import com.dbp.democarpultec.repository.RideRepository;
import com.dbp.democarpultec.service.PublicationService;
import com.dbp.democarpultec.service.RideService;
import com.dbp.democarpultec.service.UserService;
import com.dbp.democarpultec.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final PublicationService publicationService;
    private final UserService userService;
    private final VehicleService vehicleService;

    public List<RideResponseDto> findAll() {
        return rideRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    public RideResponseDto findById(Long id) {
        return toResponseDto(findEntityById(id));
    }

    public RideResponseDto create(RideRequestDto dto) {
        Ride ride = new Ride();
        updateEntity(ride, dto);
        return toResponseDto(rideRepository.save(ride));
    }

    public RideResponseDto update(Long id, RideRequestDto dto) {
        Ride ride = findEntityById(id);
        updateEntity(ride, dto);
        return toResponseDto(rideRepository.save(ride));
    }

    public void delete(Long id) {
        if (!rideRepository.existsById(id)) {
            throw new EntityNotFoundException("Ride not found with id " + id);
        }
        rideRepository.deleteById(id);
    }

    public Ride findEntityById(Long id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ride not found with id " + id));
    }

    private void updateEntity(Ride ride, RideRequestDto dto) {
        ride.setPublication(publicationService.findEntityById(dto.getPublicationId()));
        ride.setDriver(userService.findEntityById(dto.getDriverId()));
        ride.setVehicle(vehicleService.findEntityById(dto.getVehicleId()));
        ride.setFromUTEC(dto.getFromUTEC());
        ride.setDestinationOrOrigin(dto.getDestinationOrOrigin());
        ride.setDepartureTime(dto.getDepartureTime());
    }

    private RideResponseDto toResponseDto(Ride ride) {
        return RideResponseDto.builder()
                .id(ride.getId())
                .publicationId(ride.getPublication().getId())
                .driverId(ride.getDriver().getId())
                .vehicleId(ride.getVehicle().getId())
                .fromUTEC(ride.getFromUTEC())
                .destinationOrOrigin(ride.getDestinationOrOrigin())
                .departureTime(ride.getDepartureTime())
                .build();
    }
}
