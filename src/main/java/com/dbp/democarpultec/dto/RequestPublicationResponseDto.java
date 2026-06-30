package com.dbp.democarpultec.dto;

import com.dbp.democarpultec.model.enums.Carreras;
import com.dbp.democarpultec.model.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPublicationResponseDto {

    private Long id;
    private Long publicationId;
    private Long requesterId;
    private Boolean requesterIsDriver;
    private Integer seats;
    private String message;
    private String pickupPointOrDestine;
    private Double externalLatitude;
    private Double externalLongitude;
    private Double distanceToUtecKm;
    private Status status;
    private LocalDateTime createdAt;
    private String requesterName;
    private Carreras requesterCareer;
    private Double requesterRating;
    private Double proposedFare;
    private Double counterFare;
    private Double agreedFare;
}
