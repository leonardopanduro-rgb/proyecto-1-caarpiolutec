package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.RidePassengerResponseDto;
import com.dbp.democarpultec.service.RidePassengerService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RidePassengerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RidePassengerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RidePassengerService ridePassengerService;

    @Test
    void shouldReturnAllRidePassengersWhenPassengersExist() throws Exception {
        when(ridePassengerService.findAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/ride-passengers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].passengerId").value(1));
    }

    @Test
    void shouldReturnRidePassengerWhenIdExists() throws Exception {
        when(ridePassengerService.findById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/ride-passengers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rideId").value(1));
    }

    @Test
    void shouldReturn404WhenRidePassengerNotFound() throws Exception {
        when(ridePassengerService.findById(99L))
                .thenThrow(new EntityNotFoundException("RidePassenger not found with id 99"));

        mockMvc.perform(get("/api/v1/ride-passengers/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotExposeManualPassengerCreation() throws Exception {
        mockMvc.perform(post("/api/v1/ride-passengers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());

        verify(ridePassengerService, never()).create(any());
    }

    @Test
    void shouldNotExposeManualPassengerUpdates() throws Exception {
        mockMvc.perform(put("/api/v1/ride-passengers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());

        verify(ridePassengerService, never()).update(anyLong(), any());
    }

    @Test
    void shouldNotExposeManualPassengerDeletion() throws Exception {
        mockMvc.perform(delete("/api/v1/ride-passengers/1"))
                .andExpect(status().isMethodNotAllowed());

        verify(ridePassengerService, never()).delete(anyLong());
    }

    private RidePassengerResponseDto buildResponse() {
        return RidePassengerResponseDto.builder()
                .id(1L)
                .passengerId(1L)
                .rideId(1L)
                .seatsReserved(2)
                .pickupPoint("Av. Larco 123")
                .build();
    }
}
