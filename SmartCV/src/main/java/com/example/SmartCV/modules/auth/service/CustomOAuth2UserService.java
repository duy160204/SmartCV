package com.example.SmartCV.modules.auth.service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.SmartCV.modules.auth.domain.OAuthAccount;
import com.example.SmartCV.modules.auth.domain.Role;
import com.example.SmartCV.modules.auth.domain.User;

import com.example.SmartCV.modules.auth.repository.OAuthAccountRepository;
import com.example.SmartCV.modules.auth.repository.RoleRepository;
import com.example.SmartCV.modules.auth.repository.UserRepository;
import com.example.SmartCV.modules.subscription.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oauthAccountRepository;
    private final RoleRepository roleRepository;
    private final SubscriptionService subscriptionService;
    private final PasswordEncoder passwordEncoder;
    private final AuthKafkaProducer authKafkaProducer;

    public OidcUser loadOidcUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUserService delegate = new OidcUserService();
        OidcUser oidcUser = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oidcUser.getAttributes();

        OAuth2UserInfo userInfo = extractUserInfo(registrationId, attributes);
        User user = processUser(userInfo, registrationId);

        String roleName = roleRepository.findById(user.getRoleId())
                .map(Role::getName)
                .orElse("user");

        Map<String, Object> modifiedAttributes = new HashMap<>(attributes);
        modifiedAttributes.put("email", user.getEmail());

        return new DefaultOidcUser(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase())),
                oidcUser.getIdToken(),
                oidcUser.getUserInfo(),
                "email"); // use email as name attribute
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        if (userRequest instanceof OidcUserRequest oidcUserRequest) {
            return loadOidcUser(oidcUserRequest);
        }

        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        OAuth2UserInfo userInfo = extractUserInfo(registrationId, attributes);
        User user = processUser(userInfo, registrationId);

        String roleName = roleRepository.findById(user.getRoleId())
                .map(Role::getName)
                .orElse("user");

        Map<String, Object> modifiedAttributes = new HashMap<>(attributes);
        modifiedAttributes.put("email", user.getEmail());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase())),
                modifiedAttributes,
                "email"); // use email as name attribute
    }

    private OAuth2UserInfo extractUserInfo(String provider, Map<String, Object> attributes) {
        String email = "";
        String id;
        boolean verified = false;

        switch (provider.toLowerCase()) {
            case "google":
                id = (String) attributes.get("sub");
                email = (String) attributes.get("email");
                verified = Boolean.TRUE.equals(attributes.get("email_verified"));
                break;
            case "github":
                id = String.valueOf(attributes.get("id"));
                email = (String) attributes.get("email"); // FIX: Github might have null email
                verified = true;
                break;
            case "facebook":
                id = (String) attributes.get("id");
                email = (String) attributes.get("email");
                verified = true;
                break;
            case "linkedin":
                // FIX: LinkedIn mapping
                id = attributes.containsKey("sub") ? (String) attributes.get("sub") : String.valueOf(attributes.get("id"));
                email = (String) attributes.get("emailAddress");
                if (email == null) {
                    email = (String) attributes.get("email");
                }
                verified = Boolean.TRUE.equals(attributes.get("email_verified"));
                break;
            case "zalo":
                id = (String) attributes.get("id");
                verified = false;
                break;
            default:
                throw new OAuth2AuthenticationException(new org.springframework.security.oauth2.core.OAuth2Error("unknown_provider"), "Unknown provider: " + provider);
        }

        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();

        // FIX: Add fallback email if null
        // ADDED: ensure email is NEVER null
        if (email == null || email.isBlank() || "null".equalsIgnoreCase(email)) {
             email = id + "@" + provider.toLowerCase() + ".local";
        }

        return new OAuth2UserInfo(id, email, verified);
    }

    private User processUser(OAuth2UserInfo userInfo, String provider) {
        String email = userInfo.email();

        // UPDATED: Logic fallback moved to extractUserInfo. Removed exception since email is guaranteed to be never null.

        // Normalize email
        String finalEmail = email.trim().toLowerCase();

        final boolean[] isNewUser = { false };
        User user = userRepository.findByEmailIgnoreCase(finalEmail).orElseGet(() -> {
            Role role = roleRepository.findByName("user")
                    .orElseThrow(() -> new OAuth2AuthenticationException(new org.springframework.security.oauth2.core.OAuth2Error("role_not_found"), "Role user not found"));

            User u = new User();
            u.setEmail(finalEmail);
            u.setUsername("user_" + UUID.randomUUID().toString().substring(0, 8));
            u.setRoleId(role.getId());
            u.setVerified(true); // OAUTH always verified
            u.setLocked(false);
            u.setPassword(null); // OAuth users have no password

            isNewUser[0] = true;
            return userRepository.save(u);
        });

        if (isNewUser[0]) {
            subscriptionService.initFreeSubscription(user.getId());
            authKafkaProducer.sendUserRegistered(user.getEmail(), provider);
            authKafkaProducer.sendSubscriptionActivated(user.getEmail(), "FREE");
        } else {
            // UPSERT logic: Link provider if not set, set verified = true
            boolean modified = false;
            
            if (!user.isVerified()) {
                user.setVerified(true);
                user.setLocked(false);
                modified = true;
            }
            
            if (modified) {
                user = userRepository.save(user);
            }
        }

        authKafkaProducer.sendOAuth2Login(user.getEmail(), provider);

        Long uid = user.getId();
        oauthAccountRepository.findByUserIdAndProvider(uid, provider)
                .orElseGet(() -> {
                    OAuthAccount oa = new OAuthAccount();
                    oa.setUserId(uid);
                    oa.setProvider(provider);
                    oa.setProviderUserId(userInfo.id());
                    return oauthAccountRepository.save(oa);
                });

        return user;
    }

    private record OAuth2UserInfo(String id, String email, boolean verified) {
    }
}
