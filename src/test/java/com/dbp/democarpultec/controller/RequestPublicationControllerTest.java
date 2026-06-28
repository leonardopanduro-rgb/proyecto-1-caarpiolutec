package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.RequestPublicationRequestDto;
import com.dbp.democarpultec.dto.RequestPublicationResponseDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.exception.ForbiddenException;
import com.dbp.democarpultec.model.enums.Status;
import com.dbp.democarpultec.service.AuthService;
import com.dbp.democarpultec.service.RequestPublicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

@WebMvcTest(RequestPublicationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RequestPublicationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RequestPublicationService requestPublicationService;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    private final LocalDateTime createdAt = LocalDateTime.of(2025, 6, 1, 9, 0);

    private RequestPublicationResponseDto buildResponse() {
        return RequestPublicationResponseDto.builder()
                .id(1L)
                .publicationId(1L)
                .requesterId(2L)
                .requesterIsDriver(false)
                .seats(2)
                .message("Me interesa el viaje")
                .pickupPointOrDestine("Av. Larco 200")
                .status(Status.PENDING)
                .createdAt(createdAt)
                .build();
    }

    private RequestPublicationRequestDto buildRequest() {
        return RequestPublicationRequestDto.builder()
                .publicationId(1L)
                .requesterId(2L)
                .requesterIsDriver(false)
                .seats(2)
                .message("Me interesa el viaje")
                .pickupPointOrDestine("Av. Larco 200")
                .build();
    }

    private UserResponseDto buildCurrentUser() {
        return UserResponseDto.builder()
                .id(2L)
                .email("carlos@utec.edu.pe")
                .name("Carlos")
                .lastName("Lopez")
                .build();
    }

    @Test
    void shouldReturnAllRequestPublicationsWhenRequestsExist() throws Exception {
        when(requestPublicationService.findAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/request-publications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].seats").value(2));

        verify(requestPublicationService).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoRequestPublicationsExist() throws Exception {
        when(requestPublicationService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/request-publications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnRequestPublicationWhenIdExists() throws Exception {
        when(requestPublicationService.findById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/request-publications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.requesterId").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.requesterIsDriver").value(false));

        verify(requestPublicationService).findById(1L);
    }

    @Test
    void shouldReturn404WhenRequestPublicationNotFound() throws Exception {
        when(requestPublicationService.findById(99L)).thenThrow(new EntityNotFoundException("RequestPublication not found with id 99"));

        mockMvc.perform(get("/api/v1/request-publications/99"))
                .andExpect(status().isNotFound());

        verify(requestPublicationService).findById(99L);
    }

    @Test
    void shouldCreateRequestPublicationWhenValidRequest() throws Exception {
        when(authService.getCurrentUserByEmail("carlos@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(requestPublicationService.createAuthenticated(eq(2L), any(RequestPublicationRequestDto.class))).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/request-publications")
                        .principal(() -> "carlos@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Me interesa el viaje"));

        verify(requestPublicationService).createAuthenticated(eq(2L), any(RequestPublicationRequestDto.class));
    }

    @Test
    void shouldReturn400WhenSeatsIsZero() throws Exception {
        RequestPublicationRequestDto invalid = RequestPublicationRequestDto.builder()
                .publicationId(1L)
                .requesterId(2L)
                .requesterIsDriver(false)
                .seats(0)
                .build();

        mockMvc.perform(post("/api/v1/request-publications")
                        .principal(() -> "carlos@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(requestPublicationService, never()).createAuthenticated(anyLong(), any());
    }

    @Test
    void shouldReturn400WhenRequiredFieldsAreNull() throws Exception {
        RequestPublicationRequestDto invalid = RequestPublicationRequestDto.builder()
                .seats(1)
                .build();

        mockMvc.perform(post("/api/v1/request-publications")
                        .principal(() -> "carlos@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(requestPublicationService, never()).createAuthenticated(anyLong(), any());
    }

    @Test
    void shouldUpdateRequestPublicationWhenValidRequest() throws Exception {
        RequestPublicationResponseDto updated = RequestPublicationResponseDto.builder()
                .id(1L)
                .publicationId(1L)
                .requesterId(2L)
                .requesterIsDriver(false)
                .seats(1)
                .message("Actualizo mi solicitud")
                .pickupPointOrDestine("Av. Benavides 500")
                .status(Status.PENDING)
                .createdAt(createdAt)
                .build();

        when(authService.getCurrentUserByEmail("carlos@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(requestPublicationService.updateAuthenticated(eq(1L), eq(2L), any(RequestPublicationRequestDto.class))).thenReturn(updated);

        RequestPublicationRequestDto req = RequestPublicationRequestDto.builder()
                .publicationId(1L)
                .requesterId(2L)
                .requesterIsDriver(false)
                .seats(1)
                .message("Actualizo mi solicitud")
                .pickupPointOrDestine("Av. Benavides 500")
                .build();

        mockMvc.perform(put("/api/v1/request-publications/1")
                        .principal(() -> "carlos@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.seats").value(1))
                .andExpect(jsonPath("$.pickupPointOrDestine").value("Av. Benavides 500"));

        verify(requestPublicationService).updateAuthenticated(eq(1L), eq(2L), any(RequestPublicationRequestDto.class));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentRequestPublication() throws Exception {
        when(authService.getCurrentUserByEmail("carlos@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(requestPublicationService.updateAuthenticated(eq(99L), eq(2L), any(RequestPublicationRequestDto.class))).thenThrow(new EntityNotFoundException("RequestPublication not found with id 99"));

        mockMvc.perform(put("/api/v1/request-publications/99")
                        .principal(() -> "carlos@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isNotFound());

        verify(requestPublicationService).updateAuthenticated(eq(99L), eq(2L), any(RequestPublicationRequestDto.class));
    }

    @Test
    void shouldDeleteRequestPublicationWhenIdExists() throws Exception {
        when(authService.getCurrentUserByEmail("carlos@utec.edu.pe")).thenReturn(buildCurrentUser());
        doNothing().when(requestPublicationService).deleteAuthenticated(1L, 2L);

        mockMvc.perform(delete("/api/v1/request-publications/1")
                        .principal(() -> "carlos@utec.edu.pe"))
                .andExpect(status().isNoContent());

        verify(requestPublicationService).deleteAuthenticated(1L, 2L);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentRequestPublication() throws Exception {
        when(authService.getCurrentUserByEmail("carlos@utec.edu.pe")).thenReturn(buildCurrentUser());
        doThrow(new EntityNotFoundException("RequestPublication not found with id 99")).when(requestPublicationService).deleteAuthenticated(99L, 2L);

        mockMvc.perform(delete("/api/v1/request-publications/99")
                        .principal(() -> "carlos@utec.edu.pe"))
                .andExpect(status().isNotFound());

        verify(requestPublicationService).deleteAuthenticated(99L, 2L);
    }

    @Test
    void shouldCancelRequestWhenRequesterOwnsRequestAndStatusIsPending() throws Exception {
        RequestPublicationResponseDto cancelled = buildResponse();
        cancelled.setStatus(Status.CANCELLED);

        when(authService.getCurrentUserByEmail("carlos@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(requestPublicationService.cancel(1L, 2L)).thenReturn(cancelled);

        mockMvc.perform(patch("/api/v1/request-publications/1/cancel")
                        .principal(() -> "carlos@utec.edu.pe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldRejectRequestWhenAuthorOwnsPublicationAndStatusIsPending() throws Exception {
        RequestPublicationResponseDto rejected = buildResponse();
        rejected.setStatus(Status.REJECTED);

        when(authService.getCurrentUserByEmail("carlos@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(requestPublicationService.reject(1L, 2L)).thenReturn(rejected);

        mockMvc.perform(patch("/api/v1/request-publications/1/reject")
                        .principal(() -> "carlos@utec.edu.pe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void shouldReturnForbiddenWhenRejectingRequestOwnedByAnotherAuthor() throws Exception {
        when(authService.getCurrentUserByEmail("carlos@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(requestPublicationService.reject(1L, 2L))
                .thenThrow(new ForbiddenException("You are not the owner of this publication"));

        mockMvc.perform(patch("/api/v1/request-publications/1/reject")
                        .principal(() -> "carlos@utec.edu.pe"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAcceptRequestWhenAuthorOwnsPublicationAndVehicleIdIsValid() throws Exception {
        RequestPublicationResponseDto accepted = buildResponse();
        accepted.setStatus(Status.ACCEPTED);

        when(authService.getCurrentUserByEmail("carlos@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(requestPublicationService.accept(1L, 2L, 7L)).thenReturn(accepted);

        mockMvc.perform(patch("/api/v1/request-publications/1/accept")
                        .principal(() -> "carlos@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 7
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(requestPublicationService).accept(1L, 2L, 7L);
    }

    @Test
    void shouldReturn400WhenAcceptRequestDoesNotIncludeVehicleId() throws Exception {
        when(authService.getCurrentUserByEmail("carlos@utec.edu.pe")).thenReturn(buildCurrentUser());

        mockMvc.perform(patch("/api/v1/request-publications/1/accept")
                        .principal(() -> "carlos@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(requestPublicationService, never()).accept(anyLong(), anyLong(), anyLong());
    }

    @Test
    void shouldReturnForbiddenWhenAcceptingRequestOwnedByAnotherAuthor() throws Exception {
        when(authService.getCurrentUserByEmail("carlos@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(requestPublicationService.accept(1L, 2L, 7L))
                .thenThrow(new ForbiddenException("You are not the owner of this publication"));

        mockMvc.perform(patch("/api/v1/request-publications/1/accept")
                        .principal(() -> "carlos@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleId": 7
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
