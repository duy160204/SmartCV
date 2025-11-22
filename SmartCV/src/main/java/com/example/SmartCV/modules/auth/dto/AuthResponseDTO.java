package com.example.SmartCV.modules.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AuthResponseDTO {
    private String email;
    private String name;
    private String provider;
    private boolean isVerified;
    private String token; // thêm field token

    public AuthResponseDTO(String email, String name, String provider, boolean isVerified, String token) {
        this.email = email;
        this.name = name;
        this.provider = provider;
        this.isVerified = isVerified;
        this.token = token;
    }
}
