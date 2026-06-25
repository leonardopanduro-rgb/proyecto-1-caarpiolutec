package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.RidePassengerResponseDto;
import com.dbp.democarpultec.service.RidePassengerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ride-passengers")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequiredArgsConstructor
public class RidePassengerController {

    private final RidePassengerService ridePassengerService;

    @GetMapping
    public List<RidePassengerResponseDto> findAll() {
        return ridePassengerService.findAll();
    }

    @GetMapping("/{id}")
    public RidePassengerResponseDto findById(@PathVariable Long id) {
        return ridePassengerService.findById(id);
    }

}
