package com.catconnect.controller;

import com.catconnect.dto.UploadResponse;
import com.catconnect.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public UploadResponse upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(defaultValue = "general") String category) throws Exception {
        String url = fileStorageService.store(file, category);
        return new UploadResponse(url);
    }

    @GetMapping("/files/{category}/{filename}")
    public ResponseEntity<Resource> serveFile(@PathVariable String category,
                                              @PathVariable String filename) throws Exception {
        Resource resource = fileStorageService.loadAsResource(category, filename);
        MediaType mediaType = MediaType.IMAGE_JPEG;
        try {
            Path filePath = fileStorageService.resolvePath(category, filename);
            String probed = Files.probeContentType(filePath);
            if (probed != null) {
                mediaType = MediaType.parseMediaType(probed);
            }
        } catch (Exception ignored) {
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .body(resource);
    }
}
