package com.delivery.app.dto.Request;

import lombok.Data;

@Data
public class AccountRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String avatar;
    private String email;
    private String password;
    private String roleName;
}
