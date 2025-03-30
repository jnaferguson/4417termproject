package com.secure.termproject.dto;

// class to handle delete user request to avoid an attacker gaining access to other sensitive user data variables
public class ForgotPasswordRequest {
    private String email;

    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email=email;
    }
}
