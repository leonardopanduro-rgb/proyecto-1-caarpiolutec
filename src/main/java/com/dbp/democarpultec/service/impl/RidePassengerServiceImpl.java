package com.dbp.democarpultec.service.impl;

import com.dbp.democarpultec.dto.RidePassengerRequestDto;
import com.dbp.democarpultec.dto.RidePassengerResponseDto;
import com.dbp.democarpultec.model.RidePassenger;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.repository.RidePassengerRepository;
import com.dbp.democarpultec.service.RidePassengerService;
import com.dbp.democarpultec.service.RideService;
import com.dbp.democarpultec.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RidePassengerServiceImpl implements RidePassengerService {

    private final RidePassengerRepository ridePassengerRepository;
    private final UserService userService;
    private final RideService rideService;

    @Transactional(readOnly = true)
    public List<RidePassengerResponseDto> findAll() {
        return ridePassengerRepository.findAll().stream().map(this::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public RidePassengerResponseDto findById(Long id) {
        return toResponseDto(findEntityById(id));
    }

    public RidePassengerResponseDto create(RidePassengerRequestDto dto) {
        RidePassenger passenger = new RidePassenger();
        updateEntity(passenger, dto);
        return toResponseDto(ridePassengerRepository.save(passenger));
    }

    public RidePassengerResponseDto update(Long id, RidePassengerRequestDto dto) {
        RidePassenger passenger = findEntityById(id);
        updateEntity(passenger, dto);
        return toResponseDto(ridePassengerRepository.save(passenger));
    }

    public void delete(Long id) {
        if (!ridePassengerRepository.existsById(id)) {
            throw new EntityNotFoundException("RidePassenger not found with id " + id);
        }
        ridePassengerRepository.deleteById(id);
    }

    public RidePassenger findEntityById(Long id) {
        return ridePassengerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("RidePassenger not found with id " + id));
    }

    private void updateEntity(RidePassenger passenger, RidePassengerRequestDto dto) {
        passenger.setPassenger(userService.findEntityById(dto.getPassengerId()));
        passenger.setRide(rideService.findEntityById(dto.getRideId()));
        passenger.setSeatsReserved(dto.getSeatsReserved());
        passenger.setPickupPoint(dto.getPickupPoint());
    }

    private RidePassengerResponseDto toResponseDto(RidePassenger passenger) {
        User user = passenger.getPassenger();
        return RidePassengerResponseDto.builder()
                .id(passenger.getId())
                .passengerId(user.getId())
                .rideId(passenger.getRide().getId())
                .seatsReserved(passenger.getSeatsReserved())
                .pickupPoint(passenger.getPickupPoint())
                .passengerName(buildFullName(user))
                .passengerCareer(user.getCareer())
                .passengerRating(user.getRating())
                .pickupLatitude(passenger.getPickupLatitude())
                .pickupLongitude(passenger.getPickupLongitude())
                .build();
    }

    private String buildFullName(User user) {
        String first = user.getName() == null ? "" : user.getName();
        String last = user.getLastName() == null ? "" : user.getLastName();
        return (first + " " + last).trim();
    }
}
