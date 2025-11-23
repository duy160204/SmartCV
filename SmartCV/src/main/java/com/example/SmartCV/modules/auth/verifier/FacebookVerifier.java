package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.config.OAuthProperties;
import com.example.SmartCV.modules.auth.dto.FacebookUserDTO;

public class FacebookVerifier {

    public static FacebookUserDTO getUserFromCode(String code, OAuthProperties.Facebook props) {
        // TODO: gửi code lên Facebook token endpoint, lấy access token, gọi API user info
        FacebookUserDTO user = new FacebookUserDTO();
        user.setId("654321");
        user.setName("Facebook Test");
        user.setEmail("facebook@test.com");
        return user;
    }
}
