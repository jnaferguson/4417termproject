package com.secure.termproject.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class SecurityConfig {
        @Bean
        // Encryption algo based off blowfish cipher algorithm
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}