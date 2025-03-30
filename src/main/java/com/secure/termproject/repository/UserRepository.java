package com.secure.termproject.repository;


import com.secure.termproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByResetToken(String resetToken);


}

