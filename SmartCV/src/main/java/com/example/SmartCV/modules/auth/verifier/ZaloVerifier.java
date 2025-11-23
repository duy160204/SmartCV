package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.config.OAuthProperties;
import com.example.SmartCV.modules.auth.dto.ZaloUserDTO;

public class ZaloVerifier {

    public static ZaloUserDTO getUserFromCode(String code, OAuthProperties.Zalo props) {
        // TODO: gửi code lên Zalo token endpoint, lấy access token, gọi API user info
        ZaloUserDTO user = new ZaloUserDTO();
        user.setId("456789");
        user.setName("Zalo Test");
        user.setEmail("zalo@test.com");
        return user;
    }
}
