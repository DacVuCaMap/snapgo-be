package com.delivery.app.repository;

import com.delivery.app.Entity.ShipperLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShipperLocationRepository extends JpaRepository<ShipperLocation,Long> {
    Optional<ShipperLocation> findByShipperId(Long shipperId);

    @Query("SELECT sl FROM ShipperLocation sl " +
            "JOIN Account a ON sl.shipperId = a.id " +
            "WHERE sl.isOnline = true " +
            "AND (:status IS NULL OR sl.status = :status)")
    List<ShipperLocation> findNearbyShippers(
            @Param("status") Integer status
    );
}
