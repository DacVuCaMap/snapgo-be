package com.delivery.app.dto.Request;

import lombok.Data;

@Data
public class ShipperLocationUpdateRequest {
    private Double latitude;
    private Double longitude;
    private boolean isOnline;
    private Long area;
    private Integer status;
}
