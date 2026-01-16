package com.example.SmartCV.modules.auth.service;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.config.OAuthProperties;
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
import com.example.SmartCV.modules.auth.verifier.FacebookVerifier;
import com.example.SmartCV.modules.auth.verifier.GitHubVerifier;
import com.example.SmartCV.modules.auth.verifier.GoogleVerifier;
import com.example.SmartCV.modules.auth.verifier.LinkedInVerifier;
import com.example.SmartCV.modules.auth.verifier.ZaloVerifier;
import com.example.SmartCV.modules.subscription.service.SubscriptionService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

import jakarta.servlet.http.HttpServletResponse;

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

    @Autowired
    private SubscriptionService subscriptionService;

    // ================= AUTHORIZATION URL =================

    public String getGoogleAuthorizationUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", oauthProperties.getGoogle().getClientId())
                .queryParam("redirect_uri", oauthProperties.getGoogle().getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    public String getGithubAuthorizationUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://github.com/login/oauth/authorize")
                .queryParam("client_id", oauthProperties.getGithub().getClientId())
                .queryParam("redirect_uri", oauthProperties.getGithub().getRedirectUri())
                .queryParam("scope", "read:user user:email")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    public String getFacebookAuthorizationUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://www.facebook.com/v18.0/dialog/oauth")
                .queryParam("client_id", oauthProperties.getFacebook().getClientId())
                .queryParam("redirect_uri", oauthProperties.getFacebook().getRedirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", "email,public_profile")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    public String getLinkedInAuthorizationUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://www.linkedin.com/oauth/v2/authorization")
                .queryParam("response_type", "code")
                .queryParam("client_id", oauthProperties.getLinkedin().getClientId())
                .queryParam("redirect_uri", oauthProperties.getLinkedin().getRedirectUri())
                .queryParam("scope", "openid profile email")
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    public String getZaloAuthorizationUrl(String state) {
        return UriComponentsBuilder
                .fromUriString("https://oauth.zaloapp.com/v4/permission")
                .queryParam("app_id", oauthProperties.getZalo().getClientId())
                .queryParam("redirect_uri", oauthProperties.getZalo().getRedirectUri())
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    // ==================================================
    // ================= OAUTH LOGIN ====================
    // ==================================================

    public AuthResponseDTO loginWithGoogle(String code) throws OAuthException {
        try {
            String accessToken = GoogleVerifier.exchangeCodeForAccessToken(code, oauthProperties.getGoogle());
            GoogleIdToken.Payload payload = GoogleVerifier.getPayload(accessToken);

            if (payload == null)
                throw new OAuthException("Invalid Google payload");

            return loginOrCreateOAuthUser(
                    payload.getEmail(),
                    (String) payload.get("name"),
                    "google",
                    payload.getSubject());
        } catch (Exception e) {
            throw new OAuthException("Google login failed: " + e.getMessage());
        }
    }

    public AuthResponseDTO loginWithGitHub(String code) throws OAuthException {
        try {
            GitHubUserDTO user = GitHubVerifier.getUserFromCode(code, oauthProperties.getGithub());
            if (user == null)
                throw new OAuthException("Invalid GitHub user");

            String email = user.getEmail() != null
                    ? user.getEmail()
                    : "github_" + user.getId() + "@github-user.com";

            return loginOrCreateOAuthUser(email, user.getName(), "github", user.getId());
        } catch (Exception e) {
            throw new OAuthException("GitHub login failed: " + e.getMessage());
        }
    }

    public AuthResponseDTO loginWithFacebook(String code) throws OAuthException {
        try {
            FacebookUserDTO user = FacebookVerifier.getUserFromCode(code, oauthProperties.getFacebook());
            if (user == null)
                throw new OAuthException("Invalid Facebook user");

            String email = user.getEmail() != null
                    ? user.getEmail()
                    : "facebook_" + user.getId() + "@facebook-user.com";

            return loginOrCreateOAuthUser(email, user.getName(), "facebook", user.getId());
        } catch (Exception e) {
            throw new OAuthException("Facebook login failed: " + e.getMessage());
        }
    }

    public AuthResponseDTO loginWithLinkedIn(String code) throws OAuthException {
        try {
            LinkedInUserDTO user = LinkedInVerifier.getUserFromCode(code, oauthProperties.getLinkedin());
            if (user == null)
                throw new OAuthException("Invalid LinkedIn user");

            String email = user.getEmail() != null
                    ? user.getEmail()
                    : "linkedin_" + user.getId() + "@linkedin-user.com";

            return loginOrCreateOAuthUser(email, user.getName(), "linkedin", user.getId());
        } catch (Exception e) {
            throw new OAuthException("LinkedIn login failed: " + e.getMessage());
        }
    }

    public AuthResponseDTO loginWithZalo(String code) throws OAuthException {
        try {
            ZaloUserDTO user = ZaloVerifier.getUserFromCode(code, oauthProperties.getZalo());
            if (user == null)
                throw new OAuthException("Invalid Zalo user");

            String email = user.getEmail() != null
                    ? user.getEmail()
                    : "zalo_" + user.getId() + "@zalo-user.com";

            return loginOrCreateOAuthUser(email, user.getName(), "zalo", user.getId());
        } catch (Exception e) {
            throw new OAuthException("Zalo login failed: " + e.getMessage());
        }
    }

    // ==================================================
    // ================= CORE LOGIC =====================
    // ==================================================

    private AuthResponseDTO loginOrCreateOAuthUser(
            String email,
            String name,
            String provider,
            String providerUserId) throws OAuthException {

        // Track if this is a new user for subscription init
        final boolean[] isNewUser = { false };

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role role = roleRepository.findByName("user")
                    .orElseThrow(() -> new RuntimeException("Role user not found"));

            User u = new User();
            u.setEmail(email);
            u.setUsername(name != null ? name : "User" + new Random().nextInt(1000));
            u.setRoleId(role.getId());

            // ===== CORE STATUS FOR OAUTH =====
            u.setVerified(true); // OAuth provider already verified email
            u.setLocked(false); // 🔓 UNLOCK - OAuth users don't need email verification

            isNewUser[0] = true;
            return userRepository.save(u);
        });

        // ===== INIT FREE SUBSCRIPTION FOR NEW OAUTH USERS =====
        if (isNewUser[0]) {
            subscriptionService.initFreeSubscription(user.getId());
        }

        if (user.getPassword() != null) {
            throw new OAuthException("Email already registered with password");
        }

        // ===== ENSURE ACCOUNT IS UNLOCKED (for returning OAuth users) =====
        if (user.isLocked()) {
            user.setLocked(false);
            user.setVerified(true);
            userRepository.save(user);
        }

        oauthAccountRepository
                .findByUserIdAndProvider(user.getId(), provider)
                .orElseGet(() -> {
                    OAuthAccount oa = new OAuthAccount();
                    oa.setUserId(user.getId());
                    oa.setProvider(provider);
                    oa.setProviderUserId(providerUserId);
                    return oauthAccountRepository.save(oa);
                });

        String role = roleRepository.findById(user.getRoleId())
                .map(Role::getName)
                .orElse("user");

        String accessToken = jwtUtils.generateToken(user);
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

    // ==================================================
    // ================== STATE COOKIE ==================
    // ==================================================

    public String generateState(HttpServletResponse response) {
        String state = UUID.randomUUID().toString();

        ResponseCookie cookie = ResponseCookie.from("oauth_state", state)
                .httpOnly(true)
                .secure(false) // true khi HTTPS
                .path("/")
                .maxAge(300)
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        return state;
    }

    // ==================================================
    // ================== EXCEPTION =====================
    // ==================================================

    public static class OAuthException extends Exception {
        public OAuthException(String message) {
            super(message);
        }
    }
}
