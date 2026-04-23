package com.authorbooksystem.crud.controller;

import com.authorbooksystem.crud.dto.request.LoginDTO;
import com.authorbooksystem.crud.dto.request.RegisterRequest;
import com.authorbooksystem.crud.entity.User;
import com.authorbooksystem.crud.repository.UserRepository;
import com.authorbooksystem.crud.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController // Marks this class as a REST API controller (returns JSON/text instead of views)
@RequestMapping("/auth") // Base URL → all endpoints will start with /auth (e.g., /auth/login)
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
//    Used to compare raw password with encrypted (hashed) password stored in DB
    @PostMapping("/login")
    public String login(@RequestBody LoginDTO req){
        User user=userRepository.findByUsername(req.getUsername())
                .orElseThrow(()->new RuntimeException("User not Found"));
     if(!passwordEncoder.matches(req.getPassword(),user.getPassword())) {
         throw new RuntimeException("Invalid password");
     }
//        Generate JWT token after successful login
        // Token contains user identity (username) and role (authorization info)
     return JwtUtil.generateToken(user.getUsername(),user.getRole());
    }

    @PostMapping("/register") // Handles HTTP POST request for user registration
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {
        // @RequestBody → converts incoming JSON to RegisterRequest object
        // @Valid → triggers validation annotations present in RegisterRequest DTO

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        // Create a new User entity object
        User user = new User();

        // Set username from request DTO to entity
        user.setUsername(request.getUsername());

        //  Encode (hash) the password before saving
        // This ensures password is NOT stored in plain text (important for security)
        user.setPassword(passwordEncoder.encode(request.getPassword()));


        user.setRole(request.getRole());

        userRepository.save(user);

        // Return success response with HTTP 200 status
        return ResponseEntity.ok("User registered successfully");
    }
}
