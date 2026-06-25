package com.dbp.democarpultec.controller;

import com.dbp.democarpultec.dto.AuthLoginRequestDto;
import com.dbp.democarpultec.dto.AuthRegisterRequestDto;
import com.dbp.democarpultec.dto.AuthResponseDto;
import com.dbp.democarpultec.dto.RefreshTokenRequestDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.exception.UnauthorizedException;
import com.dbp.democarpultec.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void shouldRegisterWhenRequestIsValid() throws Exception {
        AuthRegisterRequestDto request = AuthRegisterRequestDto.builder()
                .name("Juan")
                .lastName("Perez")
                .email("juan@utec.edu.pe")
                .password("Password123")
                .build();

        when(authService.register(any(AuthRegisterRequestDto.class))).thenReturn(buildAuthResponse());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user.email").value("juan@utec.edu.pe"));

        verify(authService).register(any(AuthRegisterRequestDto.class));
    }

    @Test
    void shouldLoginWhenRequestIsValid() throws Exception {
        AuthLoginRequestDto request = AuthLoginRequestDto.builder()
                .email("juan@utec.edu.pe")
                .password("Password123")
                .build();

        when(authService.login(any(AuthLoginRequestDto.class))).thenReturn(buildAuthResponse());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).login(any(AuthLoginRequestDto.class));
    }

    @Test
    void shouldRefreshTokensWhenRequestIsValid() throws Exception {
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder()
                .refreshToken("existing-refresh-token")
                .build();

        when(authService.refresh(any(RefreshTokenRequestDto.class))).thenReturn(buildAuthResponse());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).refresh(any(RefreshTokenRequestDto.class));
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        AuthLoginRequestDto request = AuthLoginRequestDto.builder()
                .email("juan@utec.edu.pe")
                .password("bad-password")
                .build();

        when(authService.login(any(AuthLoginRequestDto.class))).thenThrow(new UnauthorizedException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/api/v1/auth/login"));
    }

    @Test
    void shouldReturnValidationErrorsWhenRegistrationDataIsInvalid() throws Exception {
        AuthRegisterRequestDto request = AuthRegisterRequestDto.builder()
                .name("Juan")
                .lastName("Perez")
                .email("juan@utec.edu.pe")
                .password("short")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void shouldReturnBadRequestWhenJsonBodyIsMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }

    private AuthResponseDto buildAuthResponse() {
        return AuthResponseDto.builder()
                .tokenType("Bearer")
                .accessToken("jwt-token")
                .refreshToken("refresh-token")
                .user(UserResponseDto.builder()
                        .id(1L)
                        .name("Juan")
                        .lastName("Perez")
                        .email("juan@utec.edu.pe")
                        .build())
                .build();
    }
}
