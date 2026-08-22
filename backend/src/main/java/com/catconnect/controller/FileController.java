package com.catconnect.controller;

import com.catconnect.dto.UploadResponse;
import com.catconnect.service.FileStorageService;
import lombok.RequiredArgsConstructor;
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


}
