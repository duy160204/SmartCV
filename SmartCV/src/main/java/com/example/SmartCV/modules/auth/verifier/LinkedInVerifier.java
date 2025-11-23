package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.modules.auth.dto.LinkedInUserDTO;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

public class LinkedInVerifier {

    public static LinkedInUserDTO verify(String accessToken, String clientId, String clientSecret) {
        String url = "https://api.linkedin.com/v2/me";
        RestTemplate rest = new RestTemplate();

        Map<String, Object> res = rest.getForObject(url + "?oauth2_access_token=" + accessToken, Map.class);
        if (res == null || res.get("id") == null) return null;

        LinkedInUserDTO user = new LinkedInUserDTO();
        user.setId((String) res.get("id"));
        user.setName((String) res.get("localizedFirstName") + " " + res.get("localizedLastName"));
        user.setEmail(null); // cần gọi thêm endpoint email nếu muốn
        return user;
    }
}
