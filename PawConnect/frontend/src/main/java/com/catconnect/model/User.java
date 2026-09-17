package com.catconnect.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

    private Long id;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String city;

    @JsonProperty("isAdmin")
    private Boolean isAdmin;

    @JsonProperty("admin")
    private Boolean admin;

    public Boolean getIsAdmin() {
        return Boolean.TRUE.equals(isAdmin) || Boolean.TRUE.equals(admin);
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public boolean isAdmin() {
        return Boolean.TRUE.equals(getIsAdmin());
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }




}