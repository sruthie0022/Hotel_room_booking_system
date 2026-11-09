package com.hotelbooking.service;

import com.hotelbooking.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
public class RoomImageStorageService {

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");

    private final Path uploadDirectory;

    public RoomImageStorageService(@Value("${hotel.upload-dir:./uploads}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile image) {
        String extension = EXTENSIONS.get(image.getContentType());
        if (extension == null) {
            throw new BusinessException("Upload a JPG, PNG, or WebP room image.");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("Room images must be 10 MB or smaller.");
        }
        String filename = UUID.randomUUID() + extension;
        try (var input = image.getInputStream()) {
            Files.createDirectories(uploadDirectory);
            Files.copy(input, uploadDirectory.resolve(filename));
        } catch (IOException exception) {
            throw new BusinessException("The room image could not be saved.");
        }
        return "/uploads/" + filename;
    }
}