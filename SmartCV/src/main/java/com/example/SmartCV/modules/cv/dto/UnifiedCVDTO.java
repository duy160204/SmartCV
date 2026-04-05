package com.example.SmartCV.modules.cv.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = false)
public class UnifiedCVDTO {
    private Profile profile;
    private List<Experience> experience;
    private List<Skill> skills;
    private List<Project> projects;
    private List<Language> languages;
    private List<Certification> certifications;
    private List<Award> awards;
    private List<Education> education;
    private List<String> interests;
    private List<Reference> references;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class Profile {
        private String name; private String title; private String email; private String phone;
        private String website; private String location; private String summary;
        private String photo; private String gender; private String birthday; private String address;
        private java.util.Map<String, Object> extras = new java.util.HashMap<>();
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class Experience {
        private String company; private String position; private String date;
        private String description;
        private java.util.Map<String, Object> extras = new java.util.HashMap<>();
    }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class Skill { private String name; private String level; private java.util.Map<String, Object> extras = new java.util.HashMap<>(); }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class Project { private String name; private String role; private String date; private String description; private String link; private java.util.Map<String, Object> extras = new java.util.HashMap<>(); }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class Language { private String language; private String proficiency; private java.util.Map<String, Object> extras = new java.util.HashMap<>(); }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class Certification { private String name; private String issuer; private String date; private java.util.Map<String, Object> extras = new java.util.HashMap<>(); }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class Award { private String name; private String issuer; private String year; private java.util.Map<String, Object> extras = new java.util.HashMap<>(); }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class Education { private String school; private String degree; private String major; private String date; private java.util.Map<String, Object> extras = new java.util.HashMap<>(); }
    @Data
    @JsonIgnoreProperties(ignoreUnknown = false)
    public static class Reference { private String name; private String position; private String company; private String contact; private java.util.Map<String, Object> extras = new java.util.HashMap<>(); }
}
