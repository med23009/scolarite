package com.esp.scolarite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private boolean passwordChanged;
    
    // Constructor with just token for backward compatibility
    public AuthResponse(String token) {
        this.token = token;
        this.passwordChanged = false;
    }
}
