package com.secure.termproject.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

// class to handle delete user request to avoid an attacker gaining access to other sensitive user data variables
public class FeedbackRequest {
    @NotBlank
    private String message;
    private String email;
    private String username;
    private LocalDateTime timestamp;

    public FeedbackRequest(){}

    public FeedbackRequest(String username, String message, String email){
        this.username=username;
        this.message=message;
        this.email=email;

    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
