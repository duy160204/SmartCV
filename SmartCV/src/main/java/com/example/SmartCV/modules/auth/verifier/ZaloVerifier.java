package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.config.OAuthProperties;
import com.example.SmartCV.modules.auth.dto.OAuthUserInfo;
import com.example.SmartCV.modules.auth.service.OAuthService.OAuthException;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class ZaloVerifier {

    public static OAuthUserInfo exchangeCode(String code, OAuthProperties.Zalo props) throws OAuthException {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Exchange Code
            String tokenUrl = "https://oauth.zaloapp.com/v4/access_token";
            // Zalo expects headers: secret_key, app_id, code, grant_type
            // OR form-data. Docs say POST x-www-form-urlencoded
            HttpHeaders headers = new HttpHeaders();
            headers.add("secret_key", props.getClientSecret());
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("app_id", props.getClientId());
            body.add("code", code);
            body.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, request, Map.class);
            String accessToken = (String) tokenResponse.getBody().get("access_token");

            if (accessToken == null)
                throw new OAuthException("Failed to get Zalo access token");

            // 2. Get User Info
            String userUrl = "https://graph.zalo.me/v2.0/me?fields=id,name,picture,email";
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.add("access_token", accessToken);
            HttpEntity<Void> userRequest = new HttpEntity<>(authHeaders);

            ResponseEntity<Map> userResponse = restTemplate.exchange(userUrl, HttpMethod.GET, userRequest, Map.class);
            Map<String, Object> userData = userResponse.getBody();
            if (userData == null)
                throw new OAuthException("Failed to get Zalo user info");

            // Zalo ID is numeric usually, but treated as string
            String id = String.valueOf(userData.get("id"));
            String email = (String) userData.get("email");
            // Note: Zalo apps check "email" permission. If not granted, no email returned.

            if (email == null || email.isBlank()) {
                throw new OAuthException(
                        "Zalo account does not allow email access. Please login with a provider that shares email.");
            }

            // Zalo doesn't return email_verified
            return new OAuthUserInfo(id, email, false);

        } catch (Exception e) {
            throw new OAuthException("Zalo login failed: " + e.getMessage());
        }
    }
}
