package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.ReviewRequestDto;
import com.dbp.democarpultec.dto.ReviewResponseDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.model.enums.Role;
import com.dbp.democarpultec.service.AuthService;
import com.dbp.democarpultec.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private ReviewService reviewService;
    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(authService.getCurrentUserByEmail("reviewer@utec.edu.pe"))
                .thenReturn(UserResponseDto.builder().id(1L).role(Role.USER).build());
    }

    @Test
    void shouldReturnAllReviewsWhenReviewsExist() throws Exception {
        when(reviewService.findAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void shouldReturnReviewWhenIdExists() throws Exception {
        when(reviewService.findById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewerId").value(1));
    }

    @Test
    void shouldReturn404WhenReviewNotFound() throws Exception {
        when(reviewService.findById(99L)).thenThrow(new EntityNotFoundException("Review not found with id 99"));

        mockMvc.perform(get("/api/v1/reviews/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateReviewUsingAuthenticatedReviewer() throws Exception {
        when(reviewService.createAuthenticated(eq(1L), any(ReviewRequestDto.class))).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/reviews")
                        .principal(() -> "reviewer@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));

        verify(reviewService).createAuthenticated(eq(1L), any(ReviewRequestDto.class));
    }

    @Test
    void shouldReturn400WhenRatingExceedsMaximum() throws Exception {
        ReviewRequestDto invalid = ReviewRequestDto.builder()
                .rideId(1L)
                .reviewedId(2L)
                .rating(6)
                .build();

        mockMvc.perform(post("/api/v1/reviews")
                        .principal(() -> "reviewer@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(reviewService, never()).createAuthenticated(anyLong(), any());
    }

    @Test
    void shouldUpdateOnlyAuthenticatedReviewAuthor() throws Exception {
        ReviewResponseDto updated = buildResponse();
        updated.setRating(3);
        updated.setComment("Bien, pero llego tarde");
        when(reviewService.updateAuthenticated(eq(1L), eq(1L), any(ReviewRequestDto.class))).thenReturn(updated);

        ReviewRequestDto request = buildRequest();
        request.setRating(3);
        request.setComment("Bien, pero llego tarde");

        mockMvc.perform(put("/api/v1/reviews/1")
                        .principal(() -> "reviewer@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(3));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentReview() throws Exception {
        when(reviewService.updateAuthenticated(eq(99L), eq(1L), any(ReviewRequestDto.class)))
                .thenThrow(new EntityNotFoundException("Review not found with id 99"));

        mockMvc.perform(put("/api/v1/reviews/99")
                        .principal(() -> "reviewer@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteReviewUsingAuthenticatedUser() throws Exception {
        mockMvc.perform(delete("/api/v1/reviews/1")
                        .principal(() -> "reviewer@utec.edu.pe"))
                .andExpect(status().isNoContent());

        verify(reviewService).deleteAuthenticated(1L, 1L, Role.USER);
    }

    private ReviewRequestDto buildRequest() {
        return ReviewRequestDto.builder()
                .rideId(1L)
                .reviewedId(2L)
                .rating(5)
                .comment("Excelente conductor")
                .build();
    }

    private ReviewResponseDto buildResponse() {
        return ReviewResponseDto.builder()
                .id(1L)
                .rideId(1L)
                .reviewerId(1L)
                .reviewedId(2L)
                .rating(5)
                .comment("Excelente conductor")
                .createdAt(LocalDateTime.of(2025, 6, 1, 10, 0))
                .build();
    }
}
