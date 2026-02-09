package com.topsell.backend.dto;

import com.topsell.backend.entity.Role;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String password; // Puede ser null para usuarios GUEST
    private Role role;
}
