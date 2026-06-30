package com.dbp.democarpultec.model;

import com.dbp.democarpultec.model.enums.Status;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table
public class RequestPublication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Publicación a la que se está respondiendo
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "publication_id", nullable = false)
    private Publication publication;

    // Usuario que hace la solicitud/propuesta
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    // 1 -> el requester está actuando como conductor
    // 0 -> el requester está actuando como pasajero
    @Column(nullable = false)
    private Boolean requesterIsDriver;

    // Si requestIsDriver = 1, está actuando como destino final del CONDUCTOR
    // si es 0, es pick up point para el PASAJERO
    private String pickupPointOrDestine;

    private Double externalLatitude;

    private Double externalLongitude;

    // Si requesterIsDriver = true, seats = asientos que ofrece
    // Si requesterIsDriver = false, seats = asientos que pide
    @Column(nullable = false)
    private Integer seats;

    private String message;

    // Tarifa propuesta por el solicitante (aporte sugerido en S/).
    private Double proposedFare;

    // Contraoferta del autor de la publicacion.
    private Double counterFare;

    // Tarifa final acordada cuando la solicitud es aceptada.
    private Double agreedFare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = Status.PENDING;
        }
    }
}
