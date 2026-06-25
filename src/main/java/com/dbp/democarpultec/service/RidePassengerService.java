package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.RidePassengerRequestDto;
import com.dbp.democarpultec.dto.RidePassengerResponseDto;
import com.dbp.democarpultec.model.RidePassenger;

import java.util.List;

public interface RidePassengerService {
    List<RidePassengerResponseDto> findAll();

    RidePassengerResponseDto findById(Long id);

    RidePassengerResponseDto create(RidePassengerRequestDto dto);

    RidePassengerResponseDto update(Long id, RidePassengerRequestDto dto);

    void delete(Long id);

    RidePassenger findEntityById(Long id);
}
