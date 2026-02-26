package com.example.SmartCV.common.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

class PreviewStorageServiceTest {

    private PreviewStorageService previewStorageService;
    private Path tempStorageDir;

    @BeforeEach
    void setUp() throws IOException {
        tempStorageDir = Files.createTempDirectory("preview-test");
        previewStorageService = new PreviewStorageService(tempStorageDir.toString());
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.walk(tempStorageDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Test
    void testSaveValidImage() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "dummy image content".getBytes());

        String relativePath = previewStorageService.save(file);

        assertNotNull(relativePath);
        assertTrue(relativePath.startsWith("preview/"));
        assertTrue(relativePath.endsWith(".png"));

        // Verify file was actually saved
        String filename = relativePath.replace("preview/", "");
        Path savedFile = tempStorageDir.resolve(filename);
        assertTrue(Files.exists(savedFile));
    }

    @Test
    void testSaveInvalidMimeType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "dummy pdf content".getBytes());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            previewStorageService.save(file);
        });

        assertTrue(exception.getMessage().contains("Only JPEG, PNG, and WebP images are allowed"));
    }

    @Test
    void testLoadExistingFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "load-test.webp",
                "image/webp",
                "dummy content".getBytes());

        String relativePath = previewStorageService.save(file);
        String filename = relativePath.replace("preview/", "");

        Resource resource = previewStorageService.load(filename);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void testDeleteExistingFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "delete-test.jpeg",
                "image/jpeg",
                "dummy content".getBytes());

        String relativePath = previewStorageService.save(file);
        String filename = relativePath.replace("preview/", "");

        assertTrue(Files.exists(tempStorageDir.resolve(filename)));

        previewStorageService.delete(filename);

        assertFalse(Files.exists(tempStorageDir.resolve(filename)));
    }
}
