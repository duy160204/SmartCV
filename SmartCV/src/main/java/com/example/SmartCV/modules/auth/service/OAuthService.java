package com.example.SmartCV.modules.auth.service;

import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.modules.auth.domain.OAuthAccount;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.dto.GitHubUserDTO;
import com.example.SmartCV.modules.auth.repository.OAuthAccountRepository;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.auth.verifier.GoogleVerifier;
import com.example.SmartCV.modules.auth.verifier.GitHubVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

@Service
public class OAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthAccountRepository oauthAccountRepository;

    @Autowired
    private JWTUtils jwtUtils;

    // ============================= GOOGLE LOGIN ============================= //

    public AuthResponseDTO loginWithGoogle(String idToken) throws Exception {
        GoogleIdToken.Payload payload = GoogleVerifier.verify(idToken);

        if (payload == null) {
            throw new RuntimeException("Invalid Google Token");
        }

        String email = payload.getEmail();
        String name = (String) payload.get("name");

        return loginOrCreateOAuthUser(email, name, "google");
    }

    // ============================= GITHUB LOGIN ============================= //

    public AuthResponseDTO loginWithGitHub(String accessToken) throws Exception {
        GitHubUserDTO githubUser = GitHubVerifier.verify(accessToken);

        if (githubUser == null) {
            throw new RuntimeException("Invalid GitHub Token");
        }

        String email = githubUser.getEmail();
        String name = githubUser.getName();

        // GitHub có thể không trả email, tạo email fake
        if (email == null) {
            email = "github_" + githubUser.getId() + "@github-user.com";
        }

        return loginOrCreateOAuthUser(email, name, "github");
    }

    // ============================= LOGIC CHUNG ============================= //

    private AuthResponseDTO loginOrCreateOAuthUser(String email, String name, String provider) {

        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;

        if (existingUser.isPresent()) {

            user = existingUser.get();

            // Nếu là tài khoản local → không cho đăng nhập bằng OAuth
            if (user.getPassword() != null) {
                throw new RuntimeException("Email already registered with password. Please login normally.");
            }

        } else {

            // Tạo user mới
            user = new User();
            user.setEmail(email);
            user.setUsername(name != null ? name : "User" + new Random().nextInt(1000));
            user.setRoleId(2L);   // role mặc định
            user.setVerified(true);

            userRepository.save(user);

            // Tạo OAuth Account mapping
            OAuthAccount oauth = new OAuthAccount();
            oauth.setUserId(user.getId());
            oauth.setProvider(provider);
            oauth.setProviderUserId(email);

            oauthAccountRepository.save(oauth);
        }

        // Sinh JWT
        String token = jwtUtils.generateToken(user);

        // Trả về DTO với token
        return new AuthResponseDTO(
                user.getEmail(),
                user.getUsername(),
                provider,
                user.isVerified(),
                token
        );
    }
}
