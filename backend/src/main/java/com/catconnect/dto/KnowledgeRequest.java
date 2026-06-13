package com.catconnect.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KnowledgeRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String category;
    @NotBlank
    private String content;
    private String author;
    private String imageUrl;
}
