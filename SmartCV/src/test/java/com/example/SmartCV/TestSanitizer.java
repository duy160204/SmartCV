package com.example.SmartCV;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSanitizer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSanitizePreservesHandlebars() {
        String input1 = "{\"html\": \"<img src=\\\"{{profile.avatar}}\\\" />\"}";
        String input2 = "{\"html\": \"<div>{{#if show}}<span>Hello</span>{{/if}}</div>\"}";
        String input3 = "<a href=\"{{profile.link}}\">Link</a>"; // Invalid JSON

        assertEquals("{\"html\":\"<img src=\\\"{{profile.avatar}}\\\" />\"}", sanitize(input1, objectMapper));
        assertEquals("{\"html\":\"<div>{{#if show}}<span>Hello</span>{{/if}}</div>\"}", sanitize(input2, objectMapper));
        assertEquals(input3, sanitize(input3, objectMapper));
    }

    private static String sanitize(String content, ObjectMapper objectMapper) {
        if (content == null || content.isBlank()) {
            return content;
        }
        try {
            JsonNode node = objectMapper.readTree(content);
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return content;
        }
    }
}
