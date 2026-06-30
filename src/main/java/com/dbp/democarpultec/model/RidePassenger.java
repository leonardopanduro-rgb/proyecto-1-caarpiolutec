package com.dbp.democarpultec.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(
        name = "ride_passengers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"ride_id", "passenger_id"})
        }
)
public class RidePassenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usuario que fue pasajero en el ride
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false)
    private User passenger;

    // Ride al que pertenece este pasajero
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    // Cantidad de asientos reservados por este pasajero
    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer seatsReserved;

    // Opcional: punto donde lo recogen
    private String pickupPoint;

    // Coordenadas exactas de la parada (marcadas por el pasajero en el mapa).
    private Double pickupLatitude;

    private Double pickupLongitude;
}
