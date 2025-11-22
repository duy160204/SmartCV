package com.example.SmartCV.modules.auth.verifier;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

/**
 * Simple static verifier for Google ID Tokens.
 *
 * IMPORTANT:
 * - Set environment variable GOOGLE_CLIENT_ID to your Google OAuth client id
 *   (or modify this class to read from application.properties).
 */
public final class GoogleVerifier {

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private GoogleVerifier() {}

    /**
     * Verifies the given Google ID token and returns the GoogleIdToken object.
     *
     * @param idTokenString the raw ID token from client
     * @return GoogleIdToken (payload can be obtained via getPayload()), or null if invalid
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public static GoogleIdToken.Payload verify(String idTokenString) throws GeneralSecurityException, IOException {
        if (idTokenString == null || idTokenString.isBlank()) {
            return null;
        }

        // CLIENT_ID from env var (you can change to read from properties)
        String clientId = System.getenv("GOOGLE_CLIENT_ID");
        if (clientId == null || clientId.isBlank()) {
            throw new IllegalStateException("Environment variable GOOGLE_CLIENT_ID is not set. Please set it or modify GoogleVerifier.");
        }

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY
        ).setAudience(Collections.singletonList(clientId))
         .setIssuer("https://accounts.google.com")
         .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            return null;
        }
        return idToken.getPayload();
    }
}
