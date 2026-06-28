package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.PublicationRequestDto;
import com.dbp.democarpultec.dto.PublicationResponseDto;
import com.dbp.democarpultec.model.Publication;

import java.util.List;

public interface PublicationService {
    List<PublicationResponseDto> findAll();

    PublicationResponseDto findById(Long id);

    PublicationResponseDto create(PublicationRequestDto dto);

    PublicationResponseDto createAuthenticated(Long authenticatedUserId, PublicationRequestDto dto);

    PublicationResponseDto update(Long id, PublicationRequestDto dto);

    PublicationResponseDto updateAuthenticated(Long id, Long authenticatedUserId, PublicationRequestDto dto);

    void delete(Long id);

    void deleteAuthenticated(Long id, Long authenticatedUserId);

    Publication findEntityById(Long id);
}
