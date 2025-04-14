package com.delivery.app.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private boolean isRemember;
    private String loginSource;
}
