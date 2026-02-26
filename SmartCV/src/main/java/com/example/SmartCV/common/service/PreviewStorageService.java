package com.example.SmartCV.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class PreviewStorageService {

    private Path fileStorageLocation;
    private final String previewDir;
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp");

    public PreviewStorageService(@Value("${app.storage.preview-dir}") String previewDir) {
        this.previewDir = previewDir;
    }

    @PostConstruct
    public void init() {
        try {
            this.fileStorageLocation = Paths.get(previewDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize preview storage", e);
        }
    }

    public String save(MultipartFile file) {
        // Validate file type
        if (file.getContentType() == null || !ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Sorry! Only JPEG, PNG, and WebP images are allowed. Provided: " + file.getContentType());
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if (originalFileName.contains("..")) {
                throw new IllegalArgumentException(
                        "Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Generate unique filename using UUID
            String fileExtension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                fileExtension = originalFileName.substring(i);
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Resolve target location
            // Resolve target location
            Path targetLocation = this.fileStorageLocation.resolve(newFileName).normalize();

            if (!targetLocation.startsWith(this.fileStorageLocation)) {
                throw new SecurityException("Invalid path: " + targetLocation);
            }

            // Ensure we are not overwriting
            if (Files.exists(targetLocation)) {
                throw new RuntimeException("File collision occurred. Please try again.");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "preview/" + newFileName; // Relative path stored in DB
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    public Resource load(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();

            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new SecurityException("Invalid path: " + filename);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Could not read file: " + filename, ex);
        }
    }

    public void delete(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();

            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new SecurityException("Invalid path: " + filename);
            }

            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file: " + filename, ex);
        }
    }
}
