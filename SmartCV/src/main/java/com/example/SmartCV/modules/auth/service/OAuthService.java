package com.example.SmartCV.modules.auth.service;

import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SmartCV.config.OAuthProperties;
import com.example.SmartCV.common.utils.JWTUtils;
import com.example.SmartCV.modules.auth.domain.OAuthAccount;
import com.example.SmartCV.modules.auth.domain.Role;
import com.example.SmartCV.modules.auth.domain.User;
import com.example.SmartCV.modules.auth.dto.AuthResponseDTO;
import com.example.SmartCV.modules.auth.dto.GitHubUserDTO;
import com.example.SmartCV.modules.auth.dto.FacebookUserDTO;
import com.example.SmartCV.modules.auth.dto.LinkedInUserDTO;
import com.example.SmartCV.modules.auth.dto.ZaloUserDTO;
import com.example.SmartCV.modules.auth.repository.OAuthAccountRepository;
import com.example.SmartCV.modules.auth.repository.RoleRepository;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.auth.verifier.GoogleVerifier;
import com.example.SmartCV.modules.auth.verifier.GitHubVerifier;
import com.example.SmartCV.modules.auth.verifier.FacebookVerifier;
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

    // ============================= GOOGLE LOGIN ============================= //
    public AuthResponseDTO loginWithGoogle(String idToken) throws OAuthException {
        try {
            GoogleIdToken.Payload payload = GoogleVerifier.verify(
                    idToken,
                    oauthProperties.getGoogle().getClientId()
            );
            if (payload == null) throw new OAuthException("Invalid Google Token");

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            return loginOrCreateOAuthUser(email, name, "google", email);
        } catch (Exception e) {
            throw new OAuthException("Google verification failed: " + e.getMessage());
        }
    }

    // ============================= GITHUB LOGIN ============================= //
    public AuthResponseDTO loginWithGitHub(String accessToken) throws OAuthException {
        try {
            GitHubUserDTO githubUser = GitHubVerifier.verify(
                    accessToken,
                    oauthProperties.getGithub().getClientId(),
                    oauthProperties.getGithub().getClientSecret()
            );
            if (githubUser == null) throw new OAuthException("Invalid GitHub Token");

            String email = githubUser.getEmail();
            if (email == null) email = "github_" + githubUser.getId() + "@github-user.com";

            return loginOrCreateOAuthUser(email, githubUser.getName(), "github", githubUser.getId());
        } catch (Exception e) {
            throw new OAuthException("GitHub verification failed: " + e.getMessage());
        }
    }

    // ============================= FACEBOOK LOGIN ============================= //
    public AuthResponseDTO loginWithFacebook(String accessToken) throws OAuthException {
        try {
            FacebookUserDTO fbUser = FacebookVerifier.verify(
                    accessToken,
                    oauthProperties.getFacebook().getClientId(),
                    oauthProperties.getFacebook().getClientSecret()
            );
            if (fbUser == null) throw new OAuthException("Invalid Facebook Token");

            String email = fbUser.getEmail();
            if (email == null) email = "facebook_" + fbUser.getId() + "@facebook-user.com";

            return loginOrCreateOAuthUser(email, fbUser.getName(), "facebook", fbUser.getId());
        } catch (Exception e) {
            throw new OAuthException("Facebook verification failed: " + e.getMessage());
        }
    }

    // ============================= LINKEDIN LOGIN ============================= //
    public AuthResponseDTO loginWithLinkedIn(String accessToken) throws OAuthException {
        try {
            LinkedInUserDTO liUser = LinkedInVerifier.verify(
                    accessToken,
                    oauthProperties.getLinkedin().getClientId(),
                    oauthProperties.getLinkedin().getClientSecret()
            );
            if (liUser == null) throw new OAuthException("Invalid LinkedIn Token");

            String email = liUser.getEmail();
            if (email == null) email = "linkedin_" + liUser.getId() + "@linkedin-user.com";

            return loginOrCreateOAuthUser(email, liUser.getName(), "linkedin", liUser.getId());
        } catch (Exception e) {
            throw new OAuthException("LinkedIn verification failed: " + e.getMessage());
        }
    }

    // ============================= ZALO LOGIN ============================= //
    public AuthResponseDTO loginWithZalo(String accessToken) throws OAuthException {
        try {
            ZaloUserDTO zaloUser = ZaloVerifier.verify(
                    accessToken,
                    oauthProperties.getZalo().getClientId(),
                    oauthProperties.getZalo().getClientSecret()
            );
            if (zaloUser == null) throw new OAuthException("Invalid Zalo Token");

            String email = zaloUser.getEmail();
            if (email == null) email = "zalo_" + zaloUser.getId() + "@zalo-user.com";

            return loginOrCreateOAuthUser(email, zaloUser.getName(), "zalo", zaloUser.getId());
        } catch (Exception e) {
            throw new OAuthException("Zalo verification failed: " + e.getMessage());
        }
    }

    // ============================= LOGIC CHUNG ============================= //
    private AuthResponseDTO loginOrCreateOAuthUser(String email, String name, String provider, String providerUserId) throws OAuthException {
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            if (user.getPassword() != null) {
                throw new OAuthException("Email already registered with password. Please login normally.");
            }
        } else {
            Role defaultRole = roleRepository.findByName("guest")
                    .orElseThrow(() -> new OAuthException("Default role not found"));

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

        String token = jwtUtils.generateToken(user);
        return new AuthResponseDTO(user.getEmail(), user.getUsername(), provider, user.isVerified(), token);
    }

    // ============================= EXCEPTION ============================= //
    public static class OAuthException extends Exception {
        public OAuthException(String message) { super(message); }
    }
}
