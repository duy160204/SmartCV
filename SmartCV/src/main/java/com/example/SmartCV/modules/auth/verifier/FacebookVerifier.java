package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.modules.auth.dto.FacebookUserDTO;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

public class FacebookVerifier {

    public static FacebookUserDTO verify(String accessToken, String clientId, String clientSecret) {
        String url = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;

        RestTemplate rest = new RestTemplate();
        Map<String, Object> res = rest.getForObject(url, Map.class);

        if (res == null || res.get("id") == null) return null;

        FacebookUserDTO user = new FacebookUserDTO();
        user.setId((String) res.get("id"));
        user.setName((String) res.get("name"));
        user.setEmail((String) res.get("email"));
        return user;
    }
}
