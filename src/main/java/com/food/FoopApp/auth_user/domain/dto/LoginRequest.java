package com.food.FoopApp.auth_user.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Email must be required")
    @Email(message = "invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
