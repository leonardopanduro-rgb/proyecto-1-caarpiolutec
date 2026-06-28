package com.dbp.democarpultec.service;

import com.dbp.democarpultec.dto.RequestPublicationRequestDto;
import com.dbp.democarpultec.dto.RequestPublicationResponseDto;
import com.dbp.democarpultec.model.RequestPublication;

import java.util.List;

public interface RequestPublicationService {
    List<RequestPublicationResponseDto> findAll();

    RequestPublicationResponseDto findById(Long id);

    RequestPublicationResponseDto create(RequestPublicationRequestDto dto);

    RequestPublicationResponseDto createAuthenticated(Long authenticatedUserId, RequestPublicationRequestDto dto);

    RequestPublicationResponseDto update(Long id, RequestPublicationRequestDto dto);

    RequestPublicationResponseDto updateAuthenticated(Long id, Long authenticatedUserId, RequestPublicationRequestDto dto);

    void delete(Long id);

    void deleteAuthenticated(Long id, Long authenticatedUserId);

    RequestPublication findEntityById(Long id);

    List<RequestPublicationResponseDto> findByPublication(Long publicationId, Long authenticatedUserId);

    RequestPublicationResponseDto createForPublication(
            Long publicationId,
            Long authenticatedUserId,
            RequestPublicationRequestDto dto
    );

    RequestPublicationResponseDto reject(Long requestId, Long authenticatedUserId);

    RequestPublicationResponseDto cancel(Long requestId, Long authenticatedUserId);

    RequestPublicationResponseDto accept(Long requestId, Long authenticatedUserId, Long vehicleId);
}
