package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.RideRequestDto;
import com.dbp.democarpultec.dto.RideResponseDto;
import com.dbp.democarpultec.model.Ride;

import java.util.List;

public interface RideService {
    List<RideResponseDto> findAll();

    RideResponseDto findById(Long id);

    RideResponseDto create(RideRequestDto dto);

    RideResponseDto update(Long id, RideRequestDto dto);

    void delete(Long id);

    Ride findEntityById(Long id);
}
