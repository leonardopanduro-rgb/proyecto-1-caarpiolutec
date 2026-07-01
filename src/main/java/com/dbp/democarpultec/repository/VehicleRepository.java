package com.dbp.democarpultec.repository;

import com.dbp.democarpultec.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    long countByOwner_Id(Long ownerId);

    boolean existsByOwner_Id(Long ownerId);

    boolean existsByPlateIgnoreCase(String plate);

    boolean existsByPlateIgnoreCaseAndIdNot(String plate, Long id);
}
