package com.secure.termproject.dto;

// class to handle delete user request to avoid an attacker gaining access to other sensitive user data variables
public class RegisterRequest {
    private String role;
    private String email;
    private String username;
    private String password;

    public RegisterRequest(){}

    public RegisterRequest(String email, String username, String password, String role){
        this.username=username;
        this.role=role;
        this.email=email;
        this.password=password;

    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getEmail() {
        return email;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getRole() {
        return role;
    }
}
