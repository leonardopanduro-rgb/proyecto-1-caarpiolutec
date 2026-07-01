package com.dbp.democarpultec.dto;

import jakarta.validation.constraints.Future;
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

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationRequestDto {

    @NotNull
    private Boolean fromUTEC;

    @NotNull
    private Boolean driverToPassenger;

    @NotNull
    @Min(1)
    private Integer seats;

    @NotBlank
    @Size(max = 120)
    private String titulo;

    @Size(max = 500)
    private String descripcion;

    @NotBlank
    @Size(max = 120)
    private String destinationOrOrigin;

    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double externalLatitude;

    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double externalLongitude;

    @NotNull
    @Future(message = "La fecha y hora de salida deben estar en el futuro")
    private LocalDateTime departureTime;

    private Long vehicleId;

    private Long authorId;
}
