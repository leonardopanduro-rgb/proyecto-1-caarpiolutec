package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.PublicationRequestDto;
import com.dbp.democarpultec.dto.PublicationResponseDto;
import com.dbp.democarpultec.dto.RequestPublicationRequestDto;
import com.dbp.democarpultec.dto.RequestPublicationResponseDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.model.enums.Status;
import com.dbp.democarpultec.service.AuthService;
import com.dbp.democarpultec.service.PublicationService;
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

@WebMvcTest(PublicationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PublicationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PublicationService publicationService;

    @MockitoBean
    private RequestPublicationService requestPublicationService;

    @MockitoBean
    private AuthService authService;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    // La publicacion exige salida futura (@Future), por eso usamos un instante por delante del ahora.
    private final LocalDateTime departureTime = LocalDateTime.now().plusDays(1).withNano(0);

    private PublicationResponseDto buildResponse() {
        return PublicationResponseDto.builder()
                .id(1L)
                .fromUTEC(true)
                .driverToPassenger(true)
                .seats(3)
                .titulo("Viaje a Miraflores")
                .descripcion("Salgo puntual")
                .destinationOrOrigin("Miraflores")
                .departureTime(departureTime)
                .authorId(1L)
                .rideId(null)
                .build();
    }

    private PublicationRequestDto buildRequest() {
        return PublicationRequestDto.builder()
                .fromUTEC(true)
                .driverToPassenger(true)
                .seats(3)
                .titulo("Viaje a Miraflores")
                .descripcion("Salgo puntual")
                .destinationOrOrigin("Miraflores")
                .departureTime(departureTime)
                .authorId(1L)
                .build();
    }

    private UserResponseDto buildCurrentUser() {
        return UserResponseDto.builder()
                .id(1L)
                .email("juan@utec.edu.pe")
                .name("Juan")
                .lastName("Perez")
                .build();
    }

    private RequestPublicationResponseDto buildRequestPublicationResponse() {
        return RequestPublicationResponseDto.builder()
                .id(9L)
                .publicationId(1L)
                .requesterId(2L)
                .requesterIsDriver(false)
                .seats(1)
                .pickupPointOrDestine("San Miguel")
                .status(Status.PENDING)
                .createdAt(LocalDateTime.of(2025, 6, 1, 9, 0))
                .build();
    }

    @Test
    void shouldReturnAllPublicationsWhenPublicationsExist() throws Exception {
        when(publicationService.findAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/publications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Viaje a Miraflores"))
                .andExpect(jsonPath("$[0].seats").value(3));

        verify(publicationService).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoPublicationsExist() throws Exception {
        when(publicationService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/publications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnPublicationWhenIdExists() throws Exception {
        when(publicationService.findById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/publications/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Viaje a Miraflores"))
                .andExpect(jsonPath("$.fromUTEC").value(true))
                .andExpect(jsonPath("$.authorId").value(1));

        verify(publicationService).findById(1L);
    }

    @Test
    void shouldReturn404WhenPublicationNotFound() throws Exception {
        when(publicationService.findById(99L)).thenThrow(new EntityNotFoundException("Publication not found with id 99"));

        mockMvc.perform(get("/api/v1/publications/99"))
                .andExpect(status().isNotFound());

        verify(publicationService).findById(99L);
    }

    @Test
    void shouldCreatePublicationWhenValidRequest() throws Exception {
        when(authService.getCurrentUserByEmail("juan@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(publicationService.createAuthenticated(eq(1L), any(PublicationRequestDto.class))).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/publications")
                        .principal(() -> "juan@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Viaje a Miraflores"))
                .andExpect(jsonPath("$.driverToPassenger").value(true));

        verify(publicationService).createAuthenticated(eq(1L), any(PublicationRequestDto.class));
    }

    @Test
    void shouldCreateRequestForPublicationWhenValidRequest() throws Exception {
        RequestPublicationRequestDto request = RequestPublicationRequestDto.builder()
                .requesterIsDriver(false)
                .seats(1)
                .pickupPointOrDestine("San Miguel")
                .message("me interesa")
                .build();

        when(authService.getCurrentUserByEmail("juan@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(requestPublicationService.createForPublication(eq(1L), eq(1L), any(RequestPublicationRequestDto.class)))
                .thenReturn(buildRequestPublicationResponse());

        mockMvc.perform(post("/api/v1/publications/1/requests")
                        .principal(() -> "juan@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnRequestsForPublicationWhenCurrentUserIsOwner() throws Exception {
        when(authService.getCurrentUserByEmail("juan@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(requestPublicationService.findByPublication(1L, 1L)).thenReturn(List.of(buildRequestPublicationResponse()));

        mockMvc.perform(get("/api/v1/publications/1/requests")
                        .principal(() -> "juan@utec.edu.pe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(9));
    }

    @Test
    void shouldReturn400WhenTituloIsBlank() throws Exception {
        PublicationRequestDto invalid = PublicationRequestDto.builder()
                .fromUTEC(true)
                .driverToPassenger(true)
                .seats(2)
                .titulo("")
                .destinationOrOrigin("San Isidro")
                .departureTime(departureTime)
                .authorId(1L)
                .build();

        mockMvc.perform(post("/api/v1/publications")
                        .principal(() -> "juan@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(publicationService, never()).createAuthenticated(anyLong(), any());
    }

    @Test
    void shouldReturn400WhenSeatsIsZero() throws Exception {
        PublicationRequestDto invalid = PublicationRequestDto.builder()
                .fromUTEC(true)
                .driverToPassenger(true)
                .seats(0)
                .titulo("Viaje")
                .destinationOrOrigin("Barranco")
                .departureTime(departureTime)
                .authorId(1L)
                .build();

        mockMvc.perform(post("/api/v1/publications")
                        .principal(() -> "juan@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(publicationService, never()).createAuthenticated(anyLong(), any());
    }

    @Test
    void shouldReturn400WhenDepartureTimeIsInThePast() throws Exception {
        PublicationRequestDto invalid = PublicationRequestDto.builder()
                .fromUTEC(true)
                .driverToPassenger(true)
                .seats(2)
                .titulo("Viaje")
                .destinationOrOrigin("Barranco")
                .departureTime(LocalDateTime.now().minusDays(1))
                .authorId(1L)
                .build();

        mockMvc.perform(post("/api/v1/publications")
                        .principal(() -> "juan@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(publicationService, never()).createAuthenticated(anyLong(), any());
    }

    @Test
    void shouldReturn400WhenRequiredFieldsAreNull() throws Exception {
        PublicationRequestDto invalid = PublicationRequestDto.builder()
                .titulo("Viaje")
                .destinationOrOrigin("Surco")
                .build();

        mockMvc.perform(post("/api/v1/publications")
                        .principal(() -> "juan@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(publicationService, never()).createAuthenticated(anyLong(), any());
    }

    @Test
    void shouldUpdatePublicationWhenValidRequest() throws Exception {
        PublicationResponseDto updated = PublicationResponseDto.builder()
                .id(1L)
                .fromUTEC(false)
                .driverToPassenger(false)
                .seats(2)
                .titulo("Viaje hacia UTEC")
                .descripcion("Recojo en Surco")
                .destinationOrOrigin("Surco")
                .departureTime(departureTime)
                .authorId(1L)
                .build();

        when(authService.getCurrentUserByEmail("juan@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(publicationService.updateAuthenticated(eq(1L), eq(1L), any(PublicationRequestDto.class))).thenReturn(updated);

        PublicationRequestDto req = PublicationRequestDto.builder()
                .fromUTEC(false)
                .driverToPassenger(false)
                .seats(2)
                .titulo("Viaje hacia UTEC")
                .descripcion("Recojo en Surco")
                .destinationOrOrigin("Surco")
                .departureTime(departureTime)
                .authorId(1L)
                .build();

        mockMvc.perform(put("/api/v1/publications/1")
                        .principal(() -> "juan@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Viaje hacia UTEC"))
                .andExpect(jsonPath("$.fromUTEC").value(false))
                .andExpect(jsonPath("$.seats").value(2));

        verify(publicationService).updateAuthenticated(eq(1L), eq(1L), any(PublicationRequestDto.class));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentPublication() throws Exception {
        when(authService.getCurrentUserByEmail("juan@utec.edu.pe")).thenReturn(buildCurrentUser());
        when(publicationService.updateAuthenticated(eq(99L), eq(1L), any(PublicationRequestDto.class))).thenThrow(new EntityNotFoundException("Publication not found with id 99"));

        mockMvc.perform(put("/api/v1/publications/99")
                        .principal(() -> "juan@utec.edu.pe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isNotFound());

        verify(publicationService).updateAuthenticated(eq(99L), eq(1L), any(PublicationRequestDto.class));
    }

    @Test
    void shouldDeletePublicationWhenIdExists() throws Exception {
        when(authService.getCurrentUserByEmail("juan@utec.edu.pe")).thenReturn(buildCurrentUser());
        doNothing().when(publicationService).deleteAuthenticated(1L, 1L);

        mockMvc.perform(delete("/api/v1/publications/1")
                        .principal(() -> "juan@utec.edu.pe"))
                .andExpect(status().isNoContent());

        verify(publicationService).deleteAuthenticated(1L, 1L);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentPublication() throws Exception {
        when(authService.getCurrentUserByEmail("juan@utec.edu.pe")).thenReturn(buildCurrentUser());
        doThrow(new EntityNotFoundException("Publication not found with id 99")).when(publicationService).deleteAuthenticated(99L, 1L);

        mockMvc.perform(delete("/api/v1/publications/99")
                        .principal(() -> "juan@utec.edu.pe"))
                .andExpect(status().isNotFound());

        verify(publicationService).deleteAuthenticated(99L, 1L);
    }
}
