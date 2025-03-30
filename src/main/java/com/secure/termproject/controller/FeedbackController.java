package com.secure.termproject.controller;


import com.secure.termproject.exception.UnauthorizedAccessException;
import com.secure.termproject.model.Feedback;
import com.secure.termproject.dto.FeedbackRequest;
import com.secure.termproject.model.User;
import com.secure.termproject.repository.FeedbackRepository;
import com.secure.termproject.repository.UserRepository;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;

import java.util.List;
import java.util.Map;


@RestController
public class FeedbackController {

    private FeedbackRepository feedbackRepository;
    @Autowired
    public void myService4(FeedbackRepository feedbackRepository){
        this.feedbackRepository=feedbackRepository;
    }
    private UserRepository userRepository;
    @Autowired
    public void MyService5(UserRepository userRepository){
        this.userRepository=userRepository;
    }


    @PostMapping("/submit-feedback")
    public ResponseEntity<Object> submitFeedback(@RequestBody FeedbackRequest feedbackRequest, HttpSession session) {
        String username= feedbackRequest.getUsername().toLowerCase();
        String message= feedbackRequest.getMessage();
        String email= feedbackRequest.getEmail().toLowerCase();
        LocalDateTime timestamp= feedbackRequest.getTimestamp();

        // get role of user
        String userRole =(String) session.getAttribute("role");

        if (userRole == null) {
            return new ResponseEntity<>(new ErrorResponse("Please log in first"), HttpStatus.FORBIDDEN);
        }

        // check if user exist
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(new ErrorResponse("User not found"), HttpStatus.NOT_FOUND);
        }
        // check if email exists
        if(!user.getEmail().equals(email)){
            return new ResponseEntity<>(new ErrorResponse("User email not found"), HttpStatus.NOT_FOUND);
        }
        // set timestamp for message
        if(feedbackRequest.getTimestamp()==null){
            feedbackRequest.setTimestamp(LocalDateTime.now());
        }
        // encode the message before inserting in database
        String encodedMessage= Jsoup.clean(message, Safelist.basic());
        // if whole message is empty mean there was nothing in the message that was a valid message
        if(encodedMessage.isEmpty()){
            return new ResponseEntity<>(new SuccessResponse("Message not submitted due to unsafe content"),HttpStatus.FORBIDDEN);
        }
        Feedback feedback = new Feedback();
        feedback.setUser(user);
        feedback.setMessage(encodedMessage);
        feedback.setEmail(email);
        feedback.setTimestamp(timestamp);
        feedbackRepository.save(feedback);
        return new ResponseEntity<>(new SuccessResponse("Feedback submitted Successfully!"),HttpStatus.CREATED);
    }
    @GetMapping("/check-session")
    public ResponseEntity<Object> checkSession(HttpSession session){
        if(session.getAttribute("user")==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session Expired");
        }
        return ResponseEntity.ok("Session Active");
    }

    @GetMapping("/get-user-role")
    public ResponseEntity<Object>getUserRole(HttpSession session) {
        String userRole= (String) session.getAttribute("role");
        String username=(String) session.getAttribute("username");
        String userEmail=(String) session.getAttribute("email");
        // security measure if no role of email in session they are not logged in or session lost or expired
        if(userRole==null || userEmail==null){
            return ResponseEntity.badRequest().body(new ErrorResponse("User not logged in"));
        }
        Map<String,String>data=new HashMap<>();
        data.put("role",userRole);
        data.put("username",username);
        data.put("email",userEmail);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/view-feedback")
    public ResponseEntity<Object>getAllFeedback(HttpSession session) {
        // get role from session
        String userRole =(String) session.getAttribute("role");
        if(userRole==null){
            throw new UnauthorizedAccessException("You need to log in first");
        }
        // if they are not an admin they are rejected
        if(!userRole.equals("admin")){
            throw new UnauthorizedAccessException("You are not authorized to access this content");
        }
        // list all information
        List<Feedback> feedbackList= feedbackRepository.findAll();
        return new ResponseEntity<>(feedbackList,HttpStatus.OK);
    }
    @GetMapping("/view-users")
    public ResponseEntity<Object>getAllUsers(HttpSession session) {
        // get role from session
        String userRole =(String) session.getAttribute("role");
        if(userRole==null){
            return ResponseEntity.badRequest().body(new UnauthorizedAccessException("You need to log in first!"));
        }
        if(!userRole.equals("admin")){
            return ResponseEntity.badRequest().body(new UnauthorizedAccessException("You are not authorized to access this content"));
        }
        List<User> userList= userRepository.findAll();

        for(User user: userList){
            // clean everything before displaying
            String encodedText1=Jsoup.clean(user.getUsername(), Safelist.none());
            String encodedText2=Jsoup.clean(user.getEmail(), Safelist.none());
            String encodedText3=Jsoup.clean(user.getRole(),Safelist.none());
            user.setUsername(encodedText1);
            user.setEmail(encodedText2);
            user.setRole(encodedText3);
        }
        return new ResponseEntity<>(userList,HttpStatus.OK);
    }
}
