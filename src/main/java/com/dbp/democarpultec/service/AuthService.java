package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.AuthLoginRequestDto;
import com.dbp.democarpultec.dto.AuthRegisterRequestDto;
import com.dbp.democarpultec.dto.AuthResponseDto;
import com.dbp.democarpultec.dto.RefreshTokenRequestDto;
import com.dbp.democarpultec.dto.UserResponseDto;

public interface AuthService {
    AuthResponseDto register(AuthRegisterRequestDto dto);

    AuthResponseDto login(AuthLoginRequestDto dto);

    AuthResponseDto refresh(RefreshTokenRequestDto dto);

    UserResponseDto getCurrentUserByEmail(String email);
}
