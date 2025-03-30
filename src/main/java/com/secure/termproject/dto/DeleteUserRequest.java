package com.secure.termproject.dto;

// class to handle delete user request to avoid an attacker gaining access to other sensitive user data variables
public class DeleteUserRequest {
    private String username;

    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username=username;
    }
}
