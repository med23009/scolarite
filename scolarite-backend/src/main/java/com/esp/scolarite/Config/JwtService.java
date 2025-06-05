package com.esp.scolarite.Config;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.esp.scolarite.entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String SECRET_KEY = "pFR/4gKqQWIdGIO+dE37DCthVlCbcI1bGtprGDW18+M=";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        // Try to get role_membre first, then fall back to role if available
        String role = (String) claims.get("role_membre");
        if (role == null) {
            role = (String) claims.get("role");
        }
        return role;
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // Get the authority from UserDetails
        String authority = userDetails.getAuthorities().iterator().next().getAuthority();
        
        // Store the raw role (without ROLE_ prefix) as role_membre
        String rawRole = authority.replace("ROLE_", "");
        claims.put("role_membre", rawRole);
        
        // Also store the full authority for Spring Security
        claims.put("role", authority);
        
        // If it's our User class, add additional details
        if (userDetails instanceof User) {
            User user = (User) userDetails;
            claims.put("userId", user.getIdMembre());
            claims.put("nom", user.getNom());
            claims.put("prenom", user.getPrenom());
            claims.put("email", user.getEmail());
        }
        
        System.out.println("Generating token with claims: " + claims);
        return generateToken(claims, userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean hasRoleFromToken(Authentication authentication, String role) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        String token = authentication.getCredentials().toString();
        Claims claims = extractAllClaims(token);
        String roleMembre = claims.get("role_membre", String.class);
        
        return role.equals(roleMembre);
    }
}