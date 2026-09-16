package com.catconnect.model;

public class CatBreed {

    private final String name;
    private final String tagline;
    private final String description;

    private final String personality;
    private final String grooming;
    private final String activity;
    private final String vocality;

    private final String funFact;
    private final String imageUrl;

    public CatBreed(
            String name,
            String tagline,
            String description,
            String personality,
            String grooming,
            String activity,
            String vocality,
            String funFact,
            String imageUrl
    ) {
        this.name = name;
        this.tagline = tagline;
        this.description = description;
        this.personality = personality;
        this.grooming = grooming;
        this.activity = activity;
        this.vocality = vocality;
        this.funFact = funFact;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getTagline() {
        return tagline;
    }

    public String getDescription() {
        return description;
    }

    public String getPersonality() {
        return personality;
    }

    public String getGrooming() {
        return grooming;
    }

    public String getActivity() {
        return activity;
    }

    public String getVocality() {
        return vocality;
    }

    public String getFunFact() {
        return funFact;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
