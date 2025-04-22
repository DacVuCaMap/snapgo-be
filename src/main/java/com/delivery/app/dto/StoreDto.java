package com.delivery.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreDto {
    private Long id;
    private Long accountId;
    private String storeName;
    private Double lat;
    private Double lng;
    private LocalDateTime lastUpdated;
}