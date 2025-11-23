package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.config.OAuthProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class GoogleVerifier {

    public static String exchangeCodeForAccessToken(String code, OAuthProperties.Google props) throws IOException {
        // TODO: POST request tới Google Token Endpoint để lấy access token
        return "access_token_from_google";
    }

    public static GoogleIdToken.Payload getPayload(String accessToken) throws GeneralSecurityException, IOException {
        // TODO: verify access token và trả về payload
        GoogleIdToken.Payload payload = new GoogleIdToken.Payload();
        payload.setEmail("test@gmail.com");
        payload.set("name", "Test User");
        return payload;
    }
}
