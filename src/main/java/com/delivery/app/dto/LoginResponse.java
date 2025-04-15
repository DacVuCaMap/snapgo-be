package com.delivery.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LoginResponse {
    private String roleName;
    private String token;
    private boolean active;
    private LocalDateTime expirationTime;
    private String name;
    private String avatar;
    private String email;
}
