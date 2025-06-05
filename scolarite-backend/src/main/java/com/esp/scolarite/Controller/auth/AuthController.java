package com.esp.scolarite.Controller.auth;

import java.util.Map;

import com.esp.scolarite.exception.NoRoleSelectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import com.esp.scolarite.Service.auth.AuthService;
import com.esp.scolarite.dto.AuthRequest;
import com.esp.scolarite.dto.AuthResponse;
import com.esp.scolarite.dto.ChangePasswordRequest;
import com.esp.scolarite.dto.UserDto;
import com.esp.scolarite.entity.User;




@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", allowCredentials = "true")
@RestController

@RequestMapping("/api/auth")
public class AuthController {

    
    private final AuthService authService;

    @Autowired
    public AuthController( AuthService authService) {
        this.authService = authService;
    }

    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserDto request) {

        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (NoRoleSelectedException e) {
            throw new RuntimeException(e);
        }
        }
        
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        System.out.println("hello from authcontroller ");
        return ResponseEntity.ok(authService.authenticate(authRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<Object> getCurrentUser() {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ResponseEntity.ok(authentication.getPrincipal());
        }
        return ResponseEntity.ok(authentication.getName() + " is authenticated with roles: " + authentication.getAuthorities());
    }

    @PostMapping("/change-password")
public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
    try {
        // Since we can't rely on the authentication object, get the username from the request
        String email = request.getEmail(); // Make sure to add this field to your ChangePasswordRequest

        boolean success = authService.changePassword(email, request.getCurrentPassword(), request.getNewPassword());
        
        if (success) {
            return ResponseEntity.ok().body(Map.of("message", "Password changed successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Current password is incorrect"));
        }
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", "An error occurred: " + e.getMessage()));
    }
}
   
}