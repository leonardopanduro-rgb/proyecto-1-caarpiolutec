package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.UserRequestDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.model.enums.Carreras;
import com.dbp.democarpultec.service.AuthService;
import com.dbp.democarpultec.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

    private UserResponseDto buildResponse(){
        return UserResponseDto.builder()
                .id(1L)
                .name("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .phone("999999999")
                .studentCode("U202410032")
                .career(Carreras.Ciencia_de_la_Computacion)
                .cycle(6)
                .rating(4.5)
                .build();
    }

    private UserRequestDto buildRequest(){
        return UserRequestDto.builder()
                .name("Juan")
                .lastName("Perez")
                .email("juan@test.com")
                .phone("999999999")
                .studentCode("U202410032")
                .career(Carreras.Ciencia_de_la_Computacion)
                .cycle(6)
                .build();
    }

    @Test
    void shouldReturnAllUsersWhenUsersExist() throws Exception {
        when(userService.findAll()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Juan"))
                .andExpect(jsonPath("$[0].email").value("juan@test.com"));

        verify(userService).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoUsersExist() throws Exception {
        when(userService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnUserWhenIdExists() throws Exception {
        when(userService.findById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.rating").value(4.5));

        verify(userService).findById(1L);
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.findById(99L)).thenThrow(new EntityNotFoundException("User not found with id 99"));

        mockMvc.perform(get("/api/v1/users/99"))
                .andExpect(status().isNotFound());

        verify(userService).findById(99L);
    }

    @Test
    void shouldCreateUserWhenValidRequest() throws Exception {
        when(userService.create(any(UserRequestDto.class))).thenReturn(buildResponse());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@test.com"));

        verify(userService).create(any(UserRequestDto.class));
    }

    @Test
    void shouldReturn400WhenRequestIsMissingRequiredFields() throws Exception {
        UserRequestDto invalid = UserRequestDto.builder()
                .name("")
                .email("no-es-email")
                .build();

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).create(any());
    }

    @Test
    void shouldUpdateUserWhenValidRequest() throws Exception {
        UserResponseDto updated = UserResponseDto.builder()
                .id(1L)
                .name("Pedro")
                .lastName("Lopez")
                .email("pedro@test.com")
                .rating(4.0)
                .build();

        when(userService.update(eq(1L), any(UserRequestDto.class))).thenReturn(updated);

        UserRequestDto req = UserRequestDto.builder()
                .name("Pedro")
                .lastName("Lopez")
                .email("pedro@test.com")
                .build();

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pedro"))
                .andExpect(jsonPath("$.email").value("pedro@test.com"));

        verify(userService).update(eq(1L), any(UserRequestDto.class));
    }

    @Test
    void shouldReturn404WhenUpdatingNonExistentUser() throws Exception {
        when(userService.update(eq(99L), any(UserRequestDto.class))).thenThrow(new EntityNotFoundException("User not found with id 99"));

        mockMvc.perform(put("/api/v1/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUserWhenIdExists() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
        doThrow(new EntityNotFoundException("User not found with id 99")).when(userService).delete(99L);

        mockMvc.perform(delete("/api/v1/users/99"))
                .andExpect(status().isNotFound());

        verify(userService).delete(99L);
    }

    @Test
    void shouldReturnAuthenticatedUserWhenTokenIsValid() throws Exception {
        when(authService.getCurrentUserByEmail("juan@test.com")).thenReturn(buildResponse());

        mockMvc.perform(get("/api/v1/users/me")
                        .principal(() -> "juan@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("juan@test.com"));

        verify(authService).getCurrentUserByEmail("juan@test.com");
    }
}
