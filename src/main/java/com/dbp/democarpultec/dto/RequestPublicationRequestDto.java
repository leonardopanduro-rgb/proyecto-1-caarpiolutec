package com.dbp.democarpultec.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPublicationRequestDto {

    private Long publicationId;

    private Long requesterId;

    @NotNull
    private Boolean requesterIsDriver;

    @NotNull
    @Min(1)
    private Integer seats;

    @Size(max = 500)
    private String message;

    // Tarifa que ofrece el solicitante (aporte en S/). Opcional.
    @DecimalMin(value = "0.0")
    private Double proposedFare;

    @NotBlank
    @Size(max = 120)
    private String pickupPointOrDestine;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double externalLatitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double externalLongitude;
}
