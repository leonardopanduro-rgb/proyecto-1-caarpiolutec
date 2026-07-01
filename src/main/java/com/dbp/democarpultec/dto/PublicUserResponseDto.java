package com.dbp.democarpultec.dto;

import com.dbp.democarpultec.model.enums.Carreras;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vista publica de un usuario: solo datos no sensibles, apta para que cualquier
 * usuario autenticado consulte el perfil de otro estudiante (sin email, telefono
 * ni codigo de estudiante).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicUserResponseDto {

    private Long id;
    private String name;
    private String lastName;
    private Carreras career;
    private Double rating;
}
