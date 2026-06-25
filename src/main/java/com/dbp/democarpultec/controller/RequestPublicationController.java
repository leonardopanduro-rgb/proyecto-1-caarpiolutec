package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.RequestPublicationAcceptRequestDto;
import com.dbp.democarpultec.dto.RequestPublicationRequestDto;
import com.dbp.democarpultec.dto.RequestPublicationResponseDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.service.AuthService;
import com.dbp.democarpultec.service.RequestPublicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/request-publications")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequiredArgsConstructor
public class RequestPublicationController {

    private final RequestPublicationService requestPublicationService;
    private final AuthService authService;

    @GetMapping
    public List<RequestPublicationResponseDto> findAll() {
        return requestPublicationService.findAll();
    }

    @GetMapping("/{id}")
    public RequestPublicationResponseDto findById(@PathVariable Long id) {
        return requestPublicationService.findById(id);
    }

    @PatchMapping("/{id}/cancel")
    public RequestPublicationResponseDto cancel(
            Principal principal,
            @PathVariable Long id
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return requestPublicationService.cancel(id, currentUser.getId());
    }

    @PatchMapping("/{id}/reject")
    public RequestPublicationResponseDto reject(
            Principal principal,
            @PathVariable Long id
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return requestPublicationService.reject(id, currentUser.getId());
    }

    @PatchMapping("/{id}/accept")
    public RequestPublicationResponseDto accept(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody RequestPublicationAcceptRequestDto acceptRequest
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return requestPublicationService.accept(id, currentUser.getId(), acceptRequest.getVehicleId());
    }

    @PostMapping
    public ResponseEntity<RequestPublicationResponseDto> create(
            Principal principal,
            @Valid @RequestBody RequestPublicationRequestDto requestPublication
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(requestPublicationService.createAuthenticated(currentUser.getId(), requestPublication));
    }

    @PutMapping("/{id}")
    public RequestPublicationResponseDto update(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody RequestPublicationRequestDto requestPublication
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return requestPublicationService.updateAuthenticated(id, currentUser.getId(), requestPublication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Principal principal,
            @PathVariable Long id
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        requestPublicationService.deleteAuthenticated(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
