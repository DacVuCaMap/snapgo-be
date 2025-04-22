package com.delivery.app.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "store")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @Column(name = "storeName")
    private String storeName;
    @Column(name = "latitude")
    private Double lat;
    @Column(name = "lng")
    private Double lng;
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
        if (this.isDeleted == null) {
            this.isDeleted = false;
        }
    }
}
