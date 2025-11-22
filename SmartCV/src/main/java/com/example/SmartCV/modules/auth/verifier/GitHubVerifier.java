package com.example.SmartCV.modules.auth.verifier;

import java.io.IOException;

import com.example.SmartCV.modules.auth.dto.GitHubUserDTO;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GitHub;

/**
 * Verifier for GitHub access token using the org.kohsuke:github-api library.
 *
 * Returns a GitHubUserDTO with basic fields.
 */
public final class GitHubVerifier {

    private GitHubVerifier() {}

    /**
     * Verify GitHub access token and return basic user info.
     *
     * @param accessToken GitHub OAuth access token (user access token)
     * @return GitHubUserDTO or null if token invalid / error
     * @throws IOException on network/api errors
     */
    public static GitHubUserDTO verify(String accessToken) throws IOException {
        if (accessToken == null || accessToken.isBlank()) {
            return null;
        }

        // Connect using token
        GitHub gh = GitHub.connectUsingOAuth(accessToken);

        GHMyself ghUser = gh.getMyself(); // lấy thông tin user hiện tại

        if (ghUser == null) {
            return null;
        }

        GitHubUserDTO dto = new GitHubUserDTO();
        dto.setId(String.valueOf(ghUser.getId()));
        dto.setLogin(ghUser.getLogin());
        dto.setName(ghUser.getName());
        dto.setEmail(ghUser.getEmail()); // có thể null nếu user ẩn email

        return dto;
    }
}
