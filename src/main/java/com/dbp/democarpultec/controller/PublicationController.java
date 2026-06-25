package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.PublicationRequestDto;
import com.dbp.democarpultec.dto.PublicationResponseDto;
import com.dbp.democarpultec.dto.RequestPublicationRequestDto;
import com.dbp.democarpultec.dto.RequestPublicationResponseDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.service.AuthService;
import com.dbp.democarpultec.service.PublicationService;
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
@RequestMapping("/api/v1/publications")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequiredArgsConstructor
public class PublicationController {

    private final PublicationService publicationService;
    private final RequestPublicationService requestPublicationService;
    private final AuthService authService;

    @GetMapping
    public List<PublicationResponseDto> findAll() {
        return publicationService.findAll();
    }

    @GetMapping("/{id}")
    public PublicationResponseDto findById(@PathVariable Long id) {
        return publicationService.findById(id);
    }

    @GetMapping("/{publicationId}/requests")
    public List<RequestPublicationResponseDto> findRequestsByPublication(
            Principal principal,
            @PathVariable Long publicationId
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return requestPublicationService.findByPublication(publicationId, currentUser.getId());
    }

    @PostMapping("/{publicationId}/requests")
    public ResponseEntity<RequestPublicationResponseDto> createRequestForPublication(
            Principal principal,
            @PathVariable Long publicationId,
            @Valid @RequestBody RequestPublicationRequestDto requestPublication
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        RequestPublicationResponseDto response = requestPublicationService.createForPublication(
                publicationId,
                currentUser.getId(),
                requestPublication
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping
    public ResponseEntity<PublicationResponseDto> create(
            Principal principal,
            @Valid @RequestBody PublicationRequestDto publication
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(publicationService.createAuthenticated(currentUser.getId(), publication));
    }

    @PutMapping("/{id}")
    public PublicationResponseDto update(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody PublicationRequestDto publication
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return publicationService.updateAuthenticated(id, currentUser.getId(), publication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Principal principal,
            @PathVariable Long id
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        publicationService.deleteAuthenticated(id, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
