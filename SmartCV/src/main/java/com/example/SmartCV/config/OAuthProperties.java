package com.example.SmartCV.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "oauth")
@Getter
@Setter
public class OAuthProperties {

    private Google google = new Google();
    private GitHub github = new GitHub();
    private Facebook facebook = new Facebook();
    private LinkedIn linkedin = new LinkedIn();
    private Zalo zalo = new Zalo();

    @Getter
    @Setter
    public static class Google {
        private String clientId;
        private String clientSecret;
    }

    @Getter
    @Setter
    public static class GitHub {
        private String clientId;
        private String clientSecret;
    }

    @Getter
    @Setter
    public static class Facebook {
        private String clientId;
        private String clientSecret;
    }

    @Getter
    @Setter
    public static class LinkedIn {
        private String clientId;
        private String clientSecret;
    }

    @Getter
    @Setter
    public static class Zalo {
        private String clientId;
        private String clientSecret;
    }
}
