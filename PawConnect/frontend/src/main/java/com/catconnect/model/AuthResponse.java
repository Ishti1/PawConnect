package com.catconnect.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {
    private String token;
    private User user;

    @JsonProperty("isAdmin")
    private Boolean isAdmin;

    @JsonProperty("admin")
    private Boolean admin;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Boolean getIsAdmin() {
        return Boolean.TRUE.equals(isAdmin) || Boolean.TRUE.equals(admin);
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
}