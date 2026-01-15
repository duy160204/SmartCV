package com.example.SmartCV.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String email;
    private String name;
    private String avatarURL;
    private String role;
    private boolean isVerified;
}
