package com.delivery.app.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ShipperLocationDto {
    private Long shipperId;
    private String avatar;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Double latitude;
    private Double longitude;
    private Double distance;

    public ShipperLocationDto(Long shipperId, String avatar, String firstName, String lastName, String phoneNumber, Double latitude, Double longitude, Double distance) {
        this.shipperId = shipperId;
        this.avatar = avatar;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }
}
