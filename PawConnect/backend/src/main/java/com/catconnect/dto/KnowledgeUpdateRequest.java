package com.catconnect.dto;

import lombok.Data;

@Data
public class KnowledgeUpdateRequest {
    private String title;
    private String category;
    private String content;
    private String author;
    private String imageUrl;
}
