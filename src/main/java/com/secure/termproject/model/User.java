package com.secure.termproject.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    @NotBlank(message="Username cannot be blank")
    @NotNull(message="Username cannot be null")
    @Size(min=5,max=20, message="Username must be between 5 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
            message = "Username can only contain letters, numbers and underscores")
    private String username;


    @NotBlank(message="Password cannot be blank")
    @Size(min=8, message="Password must contain at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    private String password;

    private String role;

    @Email(message="Invalid Email Format")
    @NotBlank(message="Email cannot be blank")
    @NotNull(message="Email cannot be null")
    @Size(max=50, message="Email must be at most 50 characters")
    @Column(unique = true)
    private String email;

    private String resetToken;
    private LocalDateTime tokenExpirationTime;


    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username.toLowerCase();
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role =role ;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email.toLowerCase();
    }


    public void setResetToken(String resetToken) {
        this.resetToken=resetToken;
    }

    public void setTokenExpirationTime(LocalDateTime tokenExpirationTime) {
        this.tokenExpirationTime = tokenExpirationTime;
    }

    public LocalDateTime getTokenExpirationTime() {
        return tokenExpirationTime;
    }

}
