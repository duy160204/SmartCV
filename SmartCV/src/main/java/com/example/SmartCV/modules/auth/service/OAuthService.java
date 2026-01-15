package com.example.SmartCV.modules.auth.service;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SmartCV.config.OAuthProperties;
import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.modules.auth.domain.OAuthAccount;
import com.example.SmartCV.modules.auth.domain.RefreshToken;
import com.example.SmartCV.modules.auth.domain.Role;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.dto.FacebookUserDTO;
import com.example.SmartCV.modules.auth.dto.GitHubUserDTO;
import com.example.SmartCV.modules.auth.dto.LinkedInUserDTO;
import com.example.SmartCV.modules.auth.dto.ZaloUserDTO;
import com.example.SmartCV.modules.auth.repository.OAuthAccountRepository;
import com.example.SmartCV.modules.auth.repository.RoleRepository;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.auth.service.RefreshTokenService;
import com.example.SmartCV.modules.auth.verifier.FacebookVerifier;
import com.example.SmartCV.modules.auth.verifier.GitHubVerifier;
import com.example.SmartCV.modules.auth.verifier.GoogleVerifier;
import com.example.SmartCV.modules.auth.verifier.LinkedInVerifier;
import com.example.SmartCV.modules.auth.verifier.ZaloVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

@Service
public class OAuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OAuthAccountRepository oauthAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private OAuthProperties oauthProperties;

    @Autowired
    private RefreshTokenService refreshTokenService;

    // =================== GOOGLE =================== //
    public AuthResponseDTO loginWithGoogle(String code) throws OAuthException {
        try {
            String accessToken = GoogleVerifier.exchangeCodeForAccessToken(code, oauthProperties.getGoogle());
            GoogleIdToken.Payload payload = GoogleVerifier.getPayload(accessToken);

            if (payload == null)
                throw new OAuthException("Invalid Google payload");

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            return loginOrCreateOAuthUser(email, name, "google", email);

        } catch (Exception e) {
            throw new OAuthException("Google login failed: " + e.getMessage());
        }
    }

    // =================== GITHUB =================== //
    public AuthResponseDTO loginWithGitHub(String code) throws OAuthException {
        try {
            GitHubUserDTO user = GitHubVerifier.getUserFromCode(code, oauthProperties.getGithub());
            if (user == null)
                throw new OAuthException("Invalid GitHub user");

            String email = user.getEmail();
            if (email == null)
                email = "github_" + user.getId() + "@github-user.com";

            return loginOrCreateOAuthUser(email, user.getName(), "github", user.getId());

        } catch (Exception e) {
            throw new OAuthException("GitHub login failed: " + e.getMessage());
        }
    }

    // =================== FACEBOOK =================== //
    public AuthResponseDTO loginWithFacebook(String code) throws OAuthException {
        try {
            FacebookUserDTO user = FacebookVerifier.getUserFromCode(code, oauthProperties.getFacebook());
            if (user == null)
                throw new OAuthException("Invalid Facebook user");

            String email = user.getEmail();
            if (email == null)
                email = "facebook_" + user.getId() + "@facebook-user.com";

            return loginOrCreateOAuthUser(email, user.getName(), "facebook", user.getId());

        } catch (Exception e) {
            throw new OAuthException("Facebook login failed: " + e.getMessage());
        }
    }

    // =================== LINKEDIN =================== //
    public AuthResponseDTO loginWithLinkedIn(String code) throws OAuthException {
        try {
            LinkedInUserDTO user = LinkedInVerifier.getUserFromCode(code, oauthProperties.getLinkedin());
            if (user == null)
                throw new OAuthException("Invalid LinkedIn user");

            String email = user.getEmail();
            if (email == null)
                email = "linkedin_" + user.getId() + "@linkedin-user.com";

            return loginOrCreateOAuthUser(email, user.getName(), "linkedin", user.getId());

        } catch (Exception e) {
            throw new OAuthException("LinkedIn login failed: " + e.getMessage());
        }
    }

    // =================== ZALO =================== //
    public AuthResponseDTO loginWithZalo(String code) throws OAuthException {
        try {
            ZaloUserDTO user = ZaloVerifier.getUserFromCode(code, oauthProperties.getZalo());
            if (user == null)
                throw new OAuthException("Invalid Zalo user");

            String email = user.getEmail();
            if (email == null)
                email = "zalo_" + user.getId() + "@zalo-user.com";

            return loginOrCreateOAuthUser(email, user.getName(), "zalo", user.getId());

        } catch (Exception e) {
            throw new OAuthException("Zalo login failed: " + e.getMessage());
        }
    }

    // =================== LOGIC CHUNG =================== //
    private AuthResponseDTO loginOrCreateOAuthUser(String email, String name, String provider, String providerUserId)
            throws OAuthException {
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.getPassword() != null) {
                throw new OAuthException("Email already registered with password");
            }
        } else {
            Role defaultRole = roleRepository.findByName("user")
                    .orElseThrow(() -> new OAuthException("Default role 'user' not found"));

            user = new User();
            user.setEmail(email);
            user.setUsername(name != null ? name : "User" + new Random().nextInt(1000));
            user.setRoleId(defaultRole.getId());
            user.setVerified(true);
            userRepository.save(user);
        }

        Optional<OAuthAccount> oauthOpt = oauthAccountRepository.findByUserIdAndProvider(user.getId(), provider);
        if (oauthOpt.isEmpty()) {
            OAuthAccount oauth = new OAuthAccount();
            oauth.setUserId(user.getId());
            oauth.setProvider(provider);
            oauth.setProviderUserId(providerUserId);
            oauthAccountRepository.save(oauth);
        }

        // Lấy role name
        String role = roleRepository.findById(user.getRoleId())
                .map(Role::getName)
                .orElse("user");

        // Tạo access token
        String accessToken = jwtUtils.generateToken(user);

        // Tạo refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDTO(
                user.getEmail(),
                user.getUsername(),
                provider,
                user.isVerified(),
                role,
                accessToken,
                refreshToken.getToken());
    }

    // =================== EXCEPTION =================== //
    public static class OAuthException extends Exception {
        public OAuthException(String message) {
            super(message);
        }
    }
}
