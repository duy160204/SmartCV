package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.config.OAuthProperties;
import com.example.SmartCV.modules.auth.dto.LinkedInUserDTO;

public class LinkedInVerifier {

    public static LinkedInUserDTO getUserFromCode(String code, OAuthProperties.LinkedIn props) {
        // TODO: gửi code lên LinkedIn token endpoint, lấy access token, gọi API user info
        LinkedInUserDTO user = new LinkedInUserDTO();
        user.setId("987654");
        user.setName("LinkedIn Test");
        user.setEmail("linkedin@test.com");
        return user;
    }
}
