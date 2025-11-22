package com.example.SmartCV.modules.auth.dto;

/**
 * Simple DTO to carry GitHub user info returned by GitHubVerifier.
 */
public class GitHubUserDTO {
    private String id;
    private String login;
    private String name;
    private String email;

    public GitHubUserDTO() {}

    public GitHubUserDTO(String id, String login, String name, String email) {
        this.id = id;
        this.login = login;
        this.name = name;
        this.email = email;
    }

    // --- getters & setters ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
