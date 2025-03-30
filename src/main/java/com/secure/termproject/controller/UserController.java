package com.secure.termproject.controller;


import com.secure.termproject.dto.ForgotPasswordRequest;
import com.secure.termproject.dto.DeleteUserRequest;
import com.secure.termproject.exception.UnauthorizedAccessException;
import com.secure.termproject.model.User;
import com.secure.termproject.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID; // For generating reset token
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import com.secure.termproject.exception.ValidationException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

// controller to handle user authentication and routes
@RestController
public class UserController {

    private UserRepository userRepository;
    @Autowired
    public void MyService(UserRepository userRepository){
        this.userRepository=userRepository;
    }
    private PasswordEncoder passwordEncoder;
    @Autowired
    public void MyService2(PasswordEncoder passwordEncoder){
        this.passwordEncoder=passwordEncoder;
    }
    private JavaMailSender mailSender;
    @Autowired
    public void MyService3(JavaMailSender mailSender){
        this.mailSender=mailSender;
    }


    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody User user, BindingResult result) {
        if(result.hasErrors()){
            List<String>errors=result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(new ErrorResponse(String.join(", ",errors)));
        }

        String sanitizedUsername= Jsoup.clean(user.getUsername(), Safelist.none());
        if(!sanitizedUsername.matches("^[a-zA-Z0-9_]+$")){
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid username: only letters, numbers, and underscores are allowed"));
        }
        user.setUsername(sanitizedUsername);
        // user already exists
        if ( userRepository.findByUsername(user.getUsername())!=null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Username not available"));
        }
        // account with email already exists
        if(userRepository.findByEmail(user.getEmail())!=null){
            return ResponseEntity.badRequest().body(new ErrorResponse("Account with email already exists"));
        }
        // clean password before putting in database
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("user");
        // encode email before putting in database
        String encodedEmail= Jsoup.clean(user.getEmail(),Safelist.none());
        user.setEmail(encodedEmail);
        userRepository.save(user);
        return ResponseEntity.ok(new SuccessResponse("User registered Successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody User user, HttpServletRequest request) {
        // invalidate any old session
        request.getSession().invalidate();
        // create a new session with a new ID
        HttpSession session= request.getSession(true);
        User existinUser = userRepository.findByUsername(user.getUsername().toLowerCase());
        if (existinUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message","Invalid Credentials"));
        }
        // set session attributes
        if (passwordEncoder.matches(user.getPassword(), existinUser.getPassword())) {
            session.setAttribute("role", existinUser.getRole());
            session.setAttribute("username", existinUser.getUsername());
            session.setAttribute("email", existinUser.getEmail());

            return ResponseEntity.ok(Map.of("success", true, "message","Login Successful"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message","Invalid Credentials"));
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest session) {
        // invalidate session
       session.getSession().invalidate();
       return ResponseEntity.ok(new SuccessResponse("Logged out Successfully"));
    }

    // Forgot password Endpoint
    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String email= request.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email);

        //check if user exists
        if (user == null) {
            return ResponseEntity.badRequest().body(new ValidationException("User with this email not found."));
        }
        // create a reset token for user
        String resetToken= UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setTokenExpirationTime(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        // send the reset email this ensures the person actually owns this account before changing any password
        try{
            sendResetEmail(email,resetToken);
        }catch(MessagingException e){
            return ResponseEntity.status(500).body(new ErrorResponse( "Failed to send reset email"));
        }
        return ResponseEntity.ok(new SuccessResponse("Password reset link sent to your email!"));
    }
    @PostMapping("/delete-user")
    public ResponseEntity<Object> deleteUser(@RequestBody DeleteUserRequest request, HttpSession session) {
        String userID= request.getUsername().toLowerCase();
        User user = userRepository.findByUsername(userID);

        //check if user exists
        if (user == null) {
            return ResponseEntity.badRequest().body(new ValidationException("User does not exists!"));
        }
       String currentUserRole= (String) session.getAttribute("role");
        // admin cannot delete himself in session
        if(userID.equals(session.getAttribute("username"))){
            return ResponseEntity.badRequest().body(new ValidationException("You cannot delete yourself"));
        }
        // if not a admin they are rejected from this
        if(!"admin".equals(currentUserRole)){
            return ResponseEntity.badRequest().body(new ValidationException("Unauthorized to delete a user"));
        }
        try{
            userRepository.delete(user);
            return ResponseEntity.ok(new SuccessResponse("User delete successfully!"));
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse( "Failed to delete user"));
        }
    }
    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody ResetPasswordRequest resetRequest) {
        User user = userRepository.findByResetToken(resetRequest.getResetToken());

        if(user== null){
            return ResponseEntity.badRequest().body(new ValidationException("Invalid or expired reset token."));
        }
        // check if token is expired already
        if (user.getTokenExpirationTime()!=null && user.getTokenExpirationTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(new ValidationException("Expired reset token!"));
        }
        if(!resetRequest.getNewPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$")){
            return ResponseEntity.badRequest().body(new ValidationException("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"));
        }
        user.setPassword(passwordEncoder.encode(resetRequest.getNewPassword()));
        user.setResetToken(null); // make reset token invalid
        user.setTokenExpirationTime(null);  //make expiration data invalid
        userRepository.save(user);
        return ResponseEntity.ok(new SuccessResponse("Password reset successfull!"));
    }

    private void sendResetEmail(String email,String resetToken) throws MessagingException{
        // send reset email
        String resetLink= "http://localhost:8080/mainPage.html?token="+ resetToken;
        MimeMessage message= mailSender.createMimeMessage();
        MimeMessageHelper helper= new MimeMessageHelper(message,true);
        helper.setTo(email);
        helper.setSubject("Password Reset Request");
        String emailContent="<p>To Reset your password,click the link below:</p>" +"<a href='" +resetLink + "'style='color: blue; font-weight:bold;'>Reset Password</a>";
        helper.setText(emailContent,true);
        mailSender.send(message);
    }
}
// class to handle reset password request to avoid hacker getting access to other sensitive variables
class ResetPasswordRequest {
    private String resetToken;

    private String newPassword;

    public ResetPasswordRequest(){
    }

    public ResetPasswordRequest(String resetToken, String newPassword){
        this.resetToken=resetToken;
        this.newPassword=newPassword;
    }
    public String getResetToken(){
        return resetToken;
    }

    public void setResetToken(String resetToken){
        this.resetToken=resetToken;
    }

    public String getNewPassword(){
         return newPassword;
    }
    public void setNewPassword(String newPassword){
        this.newPassword=newPassword;
    }

}
 class ErrorResponse{
    private String message;
    private boolean success;

    public ErrorResponse(String message){
        this.message=message;
        this.success=false;
    }
     public boolean isSuccess(){
         return success;
     }
     public void setSuccess(boolean success){
         this.success=success;
     }
    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
            this.message=message;
    }
}

class SuccessResponse {
        private String message;
        private boolean success;

        public SuccessResponse(String message){
            this.success=true;
            this.message=message;

        }
        public boolean isSuccess(){
            return success;
        }
        public void setSuccess(boolean success){
            this.success=success;
        }
        public String getMessage(){
            return message;
        }
        public void setMessage(String message){
            this.message=message;
        }
}
