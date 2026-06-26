package com.food.FoopApp.auth_user.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private String token;
    private List<String> roles;
}
