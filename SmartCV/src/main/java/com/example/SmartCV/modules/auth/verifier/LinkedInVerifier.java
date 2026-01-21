package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.config.OAuthProperties;
import com.example.SmartCV.modules.auth.dto.OAuthUserInfo;
import com.example.SmartCV.modules.auth.service.OAuthService.OAuthException;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class LinkedInVerifier {

    public static OAuthUserInfo exchangeCode(String code, OAuthProperties.LinkedIn props) throws OAuthException {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Exchange Code
            String tokenUrl = "https://www.linkedin.com/oauth/v2/accessToken";
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", props.getRedirectUri());
            body.add("client_id", props.getClientId());
            body.add("client_secret", props.getClientSecret());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUrl, request, Map.class);
            String accessToken = (String) tokenResponse.getBody().get("access_token");

            if (accessToken == null)
                throw new OAuthException("Failed to get LinkedIn access token");

            // 2. Get User Info (OIDC userinfo endpoint is easiest)
            String userUrl = "https://api.linkedin.com/v2/userinfo";
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setBearerAuth(accessToken);
            HttpEntity<Void> userRequest = new HttpEntity<>(authHeaders);

            ResponseEntity<Map> userResponse = restTemplate.exchange(userUrl, HttpMethod.GET, userRequest, Map.class);
            Map<String, Object> userData = userResponse.getBody();

            if (userData == null)
                throw new OAuthException("Failed to get LinkedIn user info");

            // LinkedIn OIDC: sub, email, email_verified
            String id = (String) userData.get("sub");
            String email = (String) userData.get("email");
            Boolean verified = (Boolean) userData.get("email_verified");

            if (email == null) {
                throw new OAuthException("LinkedIn account does not have an email");
            }

            return new OAuthUserInfo(id, email, Boolean.TRUE.equals(verified));

        } catch (Exception e) {
            throw new OAuthException("LinkedIn login failed: " + e.getMessage());
        }
    }
}
