package com.delivery.app.dto.Request;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String phoneNumber;
    private String address;
    private String firstName;
    private String lastName;
    private String roleName;
}
