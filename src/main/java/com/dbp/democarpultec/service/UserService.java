package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.PublicUserResponseDto;
import com.dbp.democarpultec.dto.UserRequestDto;
import com.dbp.democarpultec.dto.UserResponseDto;
import com.dbp.democarpultec.model.User;

import java.util.List;

public interface UserService {
    List<UserResponseDto> findAll();

    UserResponseDto findById(Long id);

    PublicUserResponseDto findPublicById(Long id);

    UserResponseDto create(UserRequestDto dto);

    UserResponseDto update(Long id, UserRequestDto dto);

    void delete(Long id);

    User findEntityById(Long id);

    User findEntityByEmail(String email);

    void updateRating(Long userId, Double rating);
}
