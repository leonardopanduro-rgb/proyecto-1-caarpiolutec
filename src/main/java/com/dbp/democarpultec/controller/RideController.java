package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.RideResponseDto;
import com.dbp.democarpultec.service.RideService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rides")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequiredArgsConstructor
public class RideController {

    private final RideService rideService;

    @GetMapping
    public List<RideResponseDto> findAll() {
        return rideService.findAll();
    }

    @GetMapping("/{id}")
    public RideResponseDto findById(@PathVariable Long id) {
        return rideService.findById(id);
    }

}
