package com.example.SmartCV.modules.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterRequestDTO {
    private String email;
    private String password;
    private String name;
    
}
