package com.dbp.democarpultec.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequestDto {

    @NotBlank
    @Size(max = 12)
    @Pattern(
            regexp = "^[A-Za-z0-9]{3}-?[A-Za-z0-9]{3,4}$",
            message = "La placa no tiene un formato valido (ej. ABC-123)"
    )
    private String plate;

    @NotBlank
    @Size(max = 40)
    private String brand;

    @NotBlank
    @Size(max = 40)
    private String model;

    @Size(max = 30)
    private String color;

    @NotNull
    @Min(1)
    @Max(value = 20, message = "La capacidad maxima permitida es 20 asientos")
    private Integer seats;
}
