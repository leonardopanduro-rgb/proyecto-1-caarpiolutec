package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.ReviewRequestDto;
import com.dbp.democarpultec.dto.ReviewResponseDto;
import com.dbp.democarpultec.model.Review;
import com.dbp.democarpultec.model.enums.Role;

import java.util.List;

public interface ReviewService {
    List<ReviewResponseDto> findAll();

    ReviewResponseDto findById(Long id);

    ReviewResponseDto createAuthenticated(Long authenticatedUserId, ReviewRequestDto dto);

    ReviewResponseDto updateAuthenticated(Long id, Long authenticatedUserId, ReviewRequestDto dto);

    void deleteAuthenticated(Long id, Long authenticatedUserId, Role role);

    Review findEntityById(Long id);
}
