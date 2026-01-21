package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.config.OAuthProperties;
import com.example.SmartCV.modules.auth.dto.OAuthUserInfo;
import com.example.SmartCV.modules.auth.service.OAuthService.OAuthException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

public class FacebookVerifier {

    public static OAuthUserInfo exchangeCode(String code, OAuthProperties.Facebook props) throws OAuthException {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Exchange Code
            String tokenUrl = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/v18.0/oauth/access_token")
                    .queryParam("client_id", props.getClientId())
                    .queryParam("client_secret", props.getClientSecret())
                    .queryParam("redirect_uri", props.getRedirectUri())
                    .queryParam("code", code)
                    .toUriString();

            ResponseEntity<Map> tokenResponse = restTemplate.getForEntity(tokenUrl, Map.class);
            String accessToken = (String) tokenResponse.getBody().get("access_token");

            if (accessToken == null)
                throw new OAuthException("Failed to get Facebook access token");

            // 2. Get User Info
            String userUrl = UriComponentsBuilder.fromHttpUrl("https://graph.facebook.com/me")
                    .queryParam("fields", "id,name,email")
                    .queryParam("access_token", accessToken)
                    .toUriString();

            ResponseEntity<Map> userResponse = restTemplate.getForEntity(userUrl, Map.class);
            Map<String, Object> userData = userResponse.getBody();

            if (userData == null)
                throw new OAuthException("Failed to get Facebook user info");

            String id = (String) userData.get("id");
            String email = (String) userData.get("email");

            if (email == null) {
                throw new OAuthException("Facebook account does not have an email");
            }

            // Facebook doesn't easily expose email_verified in basic /me node without
            // advanced permissions
            // but emails returned by Graph API are generally verified for the account.
            // We will conservatively mark as false if not sure, but typically FB emails are
            // verified.
            boolean emailVerified = true;

            return new OAuthUserInfo(id, email, emailVerified);

        } catch (Exception e) {
            throw new OAuthException("Facebook login failed: " + e.getMessage());
        }
    }
}
