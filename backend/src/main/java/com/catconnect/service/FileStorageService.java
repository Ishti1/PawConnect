package com.catconnect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileStorageService {

    private final Cloudinary cloudinary;

    public FileStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String store(MultipartFile file, String category) throws IOException {
        String ext = getExtension(file.getOriginalFilename());
        // Upload to Cloudinary with category as folder
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", sanitize(category),
                "resource_type", "auto"
        ));
        
        // Return the secure URL provided by Cloudinary
        return uploadResult.get("secure_url").toString();
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
