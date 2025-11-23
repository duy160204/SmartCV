package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.config.OAuthProperties;
import com.example.SmartCV.modules.auth.dto.GitHubUserDTO;

public class GitHubVerifier {

    public static GitHubUserDTO getUserFromCode(String code, OAuthProperties.GitHub props) {
        // TODO: gửi code lên GitHub Token endpoint, nhận access token, gọi API lấy user info
        GitHubUserDTO user = new GitHubUserDTO();
        user.setId("123456");
        user.setName("GitHub Test");
        user.setEmail("github@test.com");
        return user;
    }
}
