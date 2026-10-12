package com.smartfactory.system.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private Long userId;
    private String username;
    private String realName;
    private List<String> roles;
}
