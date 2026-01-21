package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.config.OAuthProperties;
import com.example.SmartCV.modules.auth.dto.OAuthUserInfo;
import com.example.SmartCV.modules.auth.service.OAuthService.OAuthException;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class GitHubVerifier {

    public static OAuthUserInfo exchangeCode(String code, OAuthProperties.GitHub props) throws OAuthException {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // 1. Exchange Code
            String tokenUrl = "https://github.com/login/oauth/access_token";
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", props.getClientId());
            body.add("client_secret", props.getClientSecret());
            body.add("code", code);
            body.add("redirect_uri", props.getRedirectUri());

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new OAuthException("Failed to exchange GitHub code");
            }

            String accessToken = (String) response.getBody().get("access_token");
            if (accessToken == null) {
                throw new OAuthException("GitHub did not return access token");
            }

            // 2. Get User Info
            String userUrl = "https://api.github.com/user";
            HttpHeaders authHeaders = new HttpHeaders();
            authHeaders.setBearerAuth(accessToken);
            authHeaders.setAccept(List.of(MediaType.parseMediaType("application/vnd.github.v3+json")));

            HttpEntity<Void> userRequest = new HttpEntity<>(authHeaders);
            ResponseEntity<Map> userResponse = restTemplate.exchange(userUrl, HttpMethod.GET, userRequest, Map.class);

            Map<String, Object> userData = userResponse.getBody();
            if (userData == null)
                throw new OAuthException("Failed to get GitHub user info");

            String id = String.valueOf(userData.get("id"));
            String email = (String) userData.get("email");

            // 3. If email is null (private), fetch emails
            boolean emailVerified = false;
            if (email == null) {
                String emailsUrl = "https://api.github.com/user/emails";
                ResponseEntity<List> emailsResponse = restTemplate.exchange(emailsUrl, HttpMethod.GET, userRequest,
                        List.class);

                List<Map<String, Object>> emails = emailsResponse.getBody();
                if (emails != null) {
                    for (Map<String, Object> emailObj : emails) {
                        Boolean primary = (Boolean) emailObj.get("primary");
                        Boolean verified = (Boolean) emailObj.get("verified");
                        if (Boolean.TRUE.equals(primary) && Boolean.TRUE.equals(verified)) {
                            email = (String) emailObj.get("email");
                            emailVerified = true;
                            break;
                        }
                    }
                }
            } else {
                // Public profile email implies control, but we should assume unverified if not
                // explicit?
                // GitHub public API doesn't explicitly flag the public email as verified in the
                // main response,
                // but it's usually the public profile email.
                // Strict security: treat as unverified or require fetching emails endpoint
                // always?
                // Let's assume verifying via /emails is safest if we want strict
                // 'emailVerified'.
                // For now, if returned in public profile, it's acceptable but verification
                // status is ambiguous.
                // We will set verified=false if not confirmed via emails endpoint, OR we can
                // fetch emails always.
                // Optimization: Fetch emails if we need strict verification.
                // Let's stick to the flow above. If it came from public profile, we assume
                // verified=false unless we check.
            }

            if (email == null) {
                throw new OAuthException("GitHub account has no public or primary verified email");
            }

            return new OAuthUserInfo(id, email, emailVerified);

        } catch (Exception e) {
            throw new OAuthException("GitHub login failed: " + e.getMessage());
        }
    }
}
