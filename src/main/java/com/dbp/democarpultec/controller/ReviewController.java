package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.ReviewRequestDto;
import com.dbp.democarpultec.dto.ReviewResponseDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.service.AuthService;
import com.dbp.democarpultec.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reviews")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    @GetMapping
    public List<ReviewResponseDto> findAll() {
        return reviewService.findAll();
    }

    @GetMapping("/{id}")
    public ReviewResponseDto findById(@PathVariable Long id) {
        return reviewService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ReviewResponseDto> create(
            Principal principal,
            @Valid @RequestBody ReviewRequestDto review
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createAuthenticated(currentUser.getId(), review));
    }

    @PutMapping("/{id}")
    public ReviewResponseDto update(
            Principal principal,
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto review
    ) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        return reviewService.updateAuthenticated(id, currentUser.getId(), review);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Principal principal, @PathVariable Long id) {
        UserResponseDto currentUser = authService.getCurrentUserByEmail(principal.getName());
        reviewService.deleteAuthenticated(id, currentUser.getId(), currentUser.getRole());
        return ResponseEntity.noContent().build();
    }
}
