package com.dbp.democarpultec.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestPublicationCounterRequestDto {

    @NotNull
    @DecimalMin(value = "0.0")
    private Double counterFare;
}
