package com.example.SmartCV.common.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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

    private final Path fileStorageLocation;
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList("image/jpeg", "image/png", "image/webp");

    public PreviewStorageService(@Value("${app.storage.preview-dir}") String previewDir) {
        if (previewDir == null || previewDir.trim().isEmpty()) {
            throw new IllegalArgumentException("Preview directory configuration is missing");
        }
        try {
            this.fileStorageLocation = Paths.get(previewDir).toAbsolutePath().normalize();
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize preview storage directory at " + previewDir, e);
        }
    }

    public String save(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        if (file.getContentType() == null || !ALLOWED_MIME_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Sorry! Only JPEG, PNG, and WebP images are allowed. Provided: " + file.getContentType());
        }

        String originalFileName = StringUtils
                .cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");

        try {
            if (originalFileName.contains("..")) {
                throw new IllegalArgumentException("Filename contains invalid path sequence: " + originalFileName);
            }

            String fileExtension = "";
            int extensionIndex = originalFileName.lastIndexOf('.');
            if (extensionIndex > 0) {
                fileExtension = originalFileName.substring(extensionIndex);
            }

            // Generate unique filename using UUID
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            Path targetLocation = this.fileStorageLocation.resolve(newFileName).normalize();

            // Prevent path traversal attacks
            if (!targetLocation.startsWith(this.fileStorageLocation)) {
                throw new SecurityException("Cannot store file outside current directory: " + targetLocation);
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "preview/" + newFileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Could not store file " + originalFileName, ex);
        }
    }

    public Resource load(String filename) {
        try {
            if (filename.contains("..")) {
                throw new SecurityException("Filename contains invalid path sequence: " + filename);
            }

            Path filePath = this.fileStorageLocation.resolve(filename).normalize();

            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new SecurityException("Invalid path requested: " + filename);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new IllegalArgumentException("Could not read file or file does not exist: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException("Malformed file path requested: " + filename, ex);
        }
    }

    public void delete(String filename) {
        try {
            if (filename.contains("..")) {
                throw new SecurityException("Filename contains invalid path sequence: " + filename);
            }

            Path filePath = this.fileStorageLocation.resolve(filename).normalize();

            if (!filePath.startsWith(this.fileStorageLocation)) {
                throw new SecurityException("Invalid delete path requested: " + filename);
            }

            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not delete file: " + filename, ex);
        }
    }
}
