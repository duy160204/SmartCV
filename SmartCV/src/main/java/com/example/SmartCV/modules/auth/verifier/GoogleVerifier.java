package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.config.OAuthProperties;
import com.example.SmartCV.modules.auth.dto.OAuthUserInfo;
import com.example.SmartCV.modules.auth.service.OAuthService.OAuthException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

public class GoogleVerifier {

    public static OAuthUserInfo exchangeCode(String code, OAuthProperties.Google props) throws OAuthException {
        try {
            // 1. Exchange Code for Tokens
            RestTemplate restTemplate = new RestTemplate();
            String tokenEndpoint = "https://oauth2.googleapis.com/token";

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("code", code);
            body.add("client_id", props.getClientId());
            body.add("client_secret", props.getClientSecret());
            body.add("redirect_uri", props.getRedirectUri());
            body.add("grant_type", "authorization_code");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenEndpoint, request, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new OAuthException("Failed to exchange code for Google token");
            }

            String idTokenString = (String) response.getBody().get("id_token");
            if (idTokenString == null) {
                throw new OAuthException("Google did not return ID Token");
            }

            // 2. Verify ID Token
            return verify(idTokenString, props);

        } catch (Exception e) {
            throw new OAuthException("Google login failed: " + e.getMessage());
        }
    }

    private static OAuthUserInfo verify(String idTokenString, OAuthProperties.Google props) throws OAuthException {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(props.getClientId()))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new OAuthException("Invalid Google ID Token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String userId = payload.getSubject();
            String email = payload.getEmail();
            boolean emailVerified = payload.getEmailVerified();

            if (email == null || email.isBlank()) {
                throw new OAuthException("Google account does not have an email");
            }

            return new OAuthUserInfo(userId, email, emailVerified);

        } catch (GeneralSecurityException | IOException e) {
            throw new OAuthException("Google verification failed: " + e.getMessage());
        }
    }
}
