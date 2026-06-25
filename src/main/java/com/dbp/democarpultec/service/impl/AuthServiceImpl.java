package com.dbp.democarpultec.service.impl;

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
import com.dbp.democarpultec.service.AuthService;
import com.dbp.democarpultec.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String UTEC_DOMAIN = "@utec.edu.pe";

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PasswordEncoder passwordEncoder;

    public AuthResponseDto register(AuthRegisterRequestDto dto) {
        String normalizedEmail = normalizeEmail(dto.getEmail());

        validateUtecEmail(normalizedEmail);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .name(dto.getName())
                .lastName(dto.getLastName())
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .studentCode(dto.getStudentCode())
                .career(dto.getCareer())
                .cycle(dto.getCycle())
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);
        applicationEventPublisher.publishEvent(new UserRegisteredEvent(
                this,
                saved.getId(),
                saved.getEmail(),
                saved.getName()
        ));
        return buildAuthResponse(saved);
    }

    public AuthResponseDto login(AuthLoginRequestDto dto) {
        String normalizedEmail = normalizeEmail(dto.getEmail());
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return buildAuthResponse(user);
    }

    public AuthResponseDto refresh(RefreshTokenRequestDto dto) {
        if (!jwtService.isRefreshTokenValid(dto.getRefreshToken())) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(normalizeEmail(jwtService.extractEmail(dto.getRefreshToken())))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        return buildAuthResponse(user);
    }

    public UserResponseDto getCurrentUserByEmail(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));

        return toResponseDto(user);
    }

    private AuthResponseDto buildAuthResponse(User user) {
        return AuthResponseDto.builder()
                .tokenType("Bearer")
                .accessToken(jwtService.generateToken(user))
                .refreshToken(jwtService.generateRefreshToken(user))
                .user(toResponseDto(user))
                .build();
    }

    private void validateUtecEmail(String email) {
        if (!email.endsWith(UTEC_DOMAIN)) {
            throw new BusinessRuleException("Only @utec.edu.pe emails are allowed");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .studentCode(user.getStudentCode())
                .career(user.getCareer())
                .cycle(user.getCycle())
                .rating(user.getRating())
                .role(user.getRole())
                .build();
    }
}
