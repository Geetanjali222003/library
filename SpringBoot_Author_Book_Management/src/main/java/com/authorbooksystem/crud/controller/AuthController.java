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

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @PostMapping("/login")
    public String login(@RequestBody LoginDTO req){
        User user=userRepository.findByUsername(req.getUsername())
                .orElseThrow(()->new RuntimeException("User not Found"));
     if(!passwordEncoder.matches(req.getPassword(),user.getPassword())) {
         throw new RuntimeException("Invalid password");
     }
     return JwtUtil.generateToken(user.getUsername(),user.getRole());
    }
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid  @RequestBody RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());

        // 🔥 MOST IMPORTANT LINE
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setRole(request.getRole());
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }
}
