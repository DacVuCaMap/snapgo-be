package com.delivery.app.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "shipper_location")
@Data
public class ShipperLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "shipper_id", nullable = false)
    private Long shipperId;
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "is_online", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isOnline;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    @Column(name = "area")
    private Long area;
    @Column(name = "status", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer status;
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}
