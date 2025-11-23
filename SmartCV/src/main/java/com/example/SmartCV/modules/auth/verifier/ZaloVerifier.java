package com.example.SmartCV.modules.auth.verifier;

import com.example.SmartCV.modules.auth.dto.ZaloUserDTO;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

public class ZaloVerifier {

    public static ZaloUserDTO verify(String accessToken, String clientId, String clientSecret) {
        String url = "https://graph.zalo.me/v2.0/me?access_token=" + accessToken;

        RestTemplate rest = new RestTemplate();
        Map<String, Object> res = rest.getForObject(url, Map.class);

        if (res == null || res.get("id") == null) return null;

        ZaloUserDTO user = new ZaloUserDTO();
        user.setId(String.valueOf(res.get("id")));
        user.setName((String) res.get("name"));
        user.setEmail((String) res.get("email"));
        return user;
    }
}
