package com.dbp.democarpultec.service;

import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.enums.Role;

public interface JwtService {
    String generateToken(User user);

    String generateRefreshToken(User user);

    String extractEmail(String token);

    Long extractUserId(String token);

    Role extractRole(String token);

    boolean isTokenValid(String token);

    boolean isRefreshTokenValid(String token);
}
