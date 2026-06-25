package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.AuthLoginRequestDto;
import com.dbp.democarpultec.dto.AuthRegisterRequestDto;
import com.dbp.democarpultec.dto.AuthResponseDto;
import com.dbp.democarpultec.dto.RefreshTokenRequestDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.event.UserRegisteredEvent;
import com.dbp.democarpultec.exception.BusinessRuleException;
import com.dbp.democarpultec.exception.DuplicateResourceException;
import com.dbp.democarpultec.exception.UnauthorizedException;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.enums.Role;
import com.dbp.democarpultec.repository.UserRepository;
import com.dbp.democarpultec.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldRegisterWhenDataIsValid() {
        AuthRegisterRequestDto request = AuthRegisterRequestDto.builder()
                .name("Juan")
                .lastName("Perez")
                .email("juan@utec.edu.pe")
                .password("Password123")
                .build();

        when(userRepository.findByEmail("juan@utec.edu.pe")).thenReturn(Optional.empty());
        when(jwtService.generateToken(any(User.class))).thenReturn("token");
        when(jwtService.generateRefreshToken(any(User.class))).thenReturn("refresh-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        AuthResponseDto response = authService.register(request);

        assertNotNull(response);
        assertEquals("Bearer", response.getTokenType());
        assertEquals("token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("juan@utec.edu.pe", response.getUser().getEmail());
        assertEquals(Role.USER, response.getUser().getRole());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();
        assertNotNull(savedUser.getPasswordHash());
        assertNotEquals("Password123", savedUser.getPasswordHash());
        assertTrue(savedUser.getPasswordHash().startsWith("$2"));
        assertEquals(Role.USER, savedUser.getRole());

        ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        UserRegisteredEvent event = eventCaptor.getValue();
        assertEquals(1L, event.getUserId());
        assertEquals("juan@utec.edu.pe", event.getEmail());
    }

    @Test
    void shouldRejectRegisterWhenEmailDomainIsNotUtec() {
        AuthRegisterRequestDto request = AuthRegisterRequestDto.builder()
                .name("Juan")
                .lastName("Perez")
                .email("juan@gmail.com")
                .password("Password123")
                .build();

        assertThrows(BusinessRuleException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldRejectRegisterWhenEmailAlreadyExists() {
        AuthRegisterRequestDto request = AuthRegisterRequestDto.builder()
                .name("Juan")
                .lastName("Perez")
                .email("juan@utec.edu.pe")
                .password("Password123")
                .build();

        when(userRepository.findByEmail("juan@utec.edu.pe")).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginWhenCredentialsAreValid() {
        AuthLoginRequestDto request = AuthLoginRequestDto.builder()
                .email("juan@utec.edu.pe")
                .password("Password123")
                .build();

        User user = new User();
        user.setId(1L);
        user.setName("Juan");
        user.setLastName("Perez");
        user.setEmail("juan@utec.edu.pe");
        user.setPasswordHash(passwordEncoder.encode("Password123"));

        when(userRepository.findByEmail("juan@utec.edu.pe")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");

        AuthResponseDto response = authService.login(request);

        assertNotNull(response);
        assertEquals("token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("juan@utec.edu.pe", response.getUser().getEmail());
    }

    @Test
    void shouldRefreshTokensWhenRefreshTokenIsValid() {
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder()
                .refreshToken("valid-refresh-token")
                .build();
        User user = new User();
        user.setEmail("juan@utec.edu.pe");

        when(jwtService.isRefreshTokenValid("valid-refresh-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-refresh-token")).thenReturn("juan@utec.edu.pe");
        when(userRepository.findByEmail("juan@utec.edu.pe")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");

        AuthResponseDto response = authService.refresh(request);

        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    @Test
    void shouldRejectRefreshWhenRefreshTokenIsInvalid() {
        RefreshTokenRequestDto request = RefreshTokenRequestDto.builder()
                .refreshToken("invalid-refresh-token")
                .build();

        when(jwtService.isRefreshTokenValid("invalid-refresh-token")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.refresh(request));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void shouldRejectLoginWhenPasswordDoesNotMatch() {
        AuthLoginRequestDto request = AuthLoginRequestDto.builder()
                .email("juan@utec.edu.pe")
                .password("wrong")
                .build();

        User user = new User();
        user.setEmail("juan@utec.edu.pe");
        user.setPasswordHash(passwordEncoder.encode("Password123"));

        when(userRepository.findByEmail("juan@utec.edu.pe")).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void shouldReturnCurrentUserWhenAuthenticatedEmailExists() {
        User user = new User();
        user.setId(1L);
        user.setName("Juan");
        user.setLastName("Perez");
        user.setEmail("juan@utec.edu.pe");

        when(userRepository.findByEmail("juan@utec.edu.pe")).thenReturn(Optional.of(user));

        UserResponseDto response = authService.getCurrentUserByEmail("JUAN@UTEC.EDU.PE");

        assertEquals(1L, response.getId());
        assertEquals("juan@utec.edu.pe", response.getEmail());
    }

    @Test
    void shouldRejectCurrentUserWhenAuthenticatedEmailDoesNotExist() {
        when(userRepository.findByEmail("nobody@utec.edu.pe")).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.getCurrentUserByEmail("nobody@utec.edu.pe"));
    }
}
