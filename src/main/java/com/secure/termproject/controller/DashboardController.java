package com.secure.termproject.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

// controller to load dashboard from templates so everyone won't be able to access from static folder by everyone but be authenticated
@Controller
public class DashboardController {
    @GetMapping("/dashboard")
    public ResponseEntity<String> dashBoard(HttpSession session){
        String userRole=(String)session.getAttribute("role");
        if(userRole==null){
            HttpHeaders redirectHeader = new HttpHeaders();
            redirectHeader.add("Location","/mainPage.html");
            return new ResponseEntity<>(redirectHeader,HttpStatus.FOUND);
        }
        try{
            // Load dashboard.html from the templates folder
            Resource resource = new ClassPathResource("templates/dashboard.html");
            String content= new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);

            // set content type to HTML
            HttpHeaders headers= new HttpHeaders();
            headers.add("Content-Type","text/html; charset=UTF-8");
            return new ResponseEntity<>(content,headers,HttpStatus.OK);
        }catch(IOException e){
            return new ResponseEntity<>("Error loading dashboard",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
