package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.VehicleRequestDto;
import com.dbp.democarpultec.dto.VehicleResponseDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.service.AuthService;
import com.dbp.democarpultec.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final AuthService authService;

    @GetMapping
    public List<VehicleResponseDto> findAll() {
        return vehicleService.findAll();
    }

    @GetMapping("/{id}")
    public VehicleResponseDto findById(@PathVariable Long id) {
        return vehicleService.findById(id);
    }

    @PostMapping
    public ResponseEntity<VehicleResponseDto> create(
            Principal principal,
            @Valid @RequestBody VehicleRequestDto vehicle
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehicleService.createAuthenticated(currentUser.getId(), vehicle));
    }

    @PutMapping("/{id}")
    public VehicleResponseDto update(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody VehicleRequestDto vehicle
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return vehicleService.updateAuthenticated(id, currentUser.getId(), vehicle);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        vehicleService.deleteAuthenticated(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
