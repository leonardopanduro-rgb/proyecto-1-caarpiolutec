package com.dbp.democarpultec.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptCounterRequestDto {

    // Requerido solo cuando el solicitante es el conductor (publicacion "busca conductor").
    private Long vehicleId;
}
