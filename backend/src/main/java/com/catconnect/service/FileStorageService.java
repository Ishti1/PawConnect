package com.catconnect.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path rootPath;

    public FileStorageService(@Value("${app.storage.path}") String storagePath) throws IOException {
        this.rootPath = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(rootPath);
    }

    public String store(MultipartFile file, String category) throws IOException {
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + ext;
        Path categoryDir = rootPath.resolve(sanitize(category));
        Files.createDirectories(categoryDir);
        Path target = categoryDir.resolve(filename);
        Files.copy(file.getInputStream(), target);
        return "/api/files/" + sanitize(category) + "/" + filename;
    }

    public Path resolvePath(String category, String filename) throws IOException {
        Path file = rootPath.resolve(sanitize(category)).resolve(filename).normalize();
        if (!file.startsWith(rootPath)) {
            throw new IOException("Invalid file path");
        }
        return file;
    }

    public Resource loadAsResource(String category, String filename) throws IOException {
        Path file = resolvePath(category, filename);
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("File not found");
        }
        return resource;
    }

    private String sanitize(String input) {
        return input.replaceAll("[^a-zA-Z0-9_-]", "");
    }

    private String getExtension(String name) {
        if (name == null || !name.contains(".")) {
            return ".jpg";
        }
        return name.substring(name.lastIndexOf('.'));
    }
}
