package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.RideResponseDto;
import com.dbp.democarpultec.service.RideService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RideController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RideControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RideService rideService;

    @Test
    void shouldReturnAllRidesWhenRidesExist() throws Exception {
        when(rideService.findAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/rides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].destinationOrOrigin").value("Miraflores"));
    }

    @Test
    void shouldReturnRideWhenIdExists() throws Exception {
        when(rideService.findById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/rides/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driverId").value(1));
    }

    @Test
    void shouldReturn404WhenRideNotFound() throws Exception {
        when(rideService.findById(99L)).thenThrow(new EntityNotFoundException("Ride not found with id 99"));

        mockMvc.perform(get("/api/v1/rides/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotExposeManualRideCreation() throws Exception {
        mockMvc.perform(post("/api/v1/rides")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());

        verify(rideService, never()).create(any());
    }

    @Test
    void shouldNotExposeManualRideUpdates() throws Exception {
        mockMvc.perform(put("/api/v1/rides/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());

        verify(rideService, never()).update(anyLong(), any());
    }

    @Test
    void shouldNotExposeManualRideDeletion() throws Exception {
        mockMvc.perform(delete("/api/v1/rides/1"))
                .andExpect(status().isMethodNotAllowed());

        verify(rideService, never()).delete(anyLong());
    }

    private RideResponseDto buildResponse() {
        return RideResponseDto.builder()
                .id(1L)
                .publicationId(1L)
                .driverId(1L)
                .vehicleId(1L)
                .fromUTEC(true)
                .destinationOrOrigin("Miraflores")
                .departureTime(LocalDateTime.of(2025, 6, 1, 8, 30))
                .build();
    }
}
