package com.delivery.app.dto;

import lombok.Data;

@Data
public class AccountDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String avatar;
    private String email;
    private String password;
    private String roleName;
}
