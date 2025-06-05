package com.esp.scolarite.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAutentificationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);

        System.out.println("Extracted username: " + username); // Log extracted username

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.validateToken(jwt, userDetails)) {
                    // Enhanced debugging
                    System.out.println("JWT Filter - Username: " + username);
                    System.out.println("JWT Filter - User authorities: " + userDetails.getAuthorities());
                    System.out.println("JWT Filter - Request URL: " + request.getRequestURI());
                    
                    // Extract claims from token for debugging
                    try {
                        String[] chunks = jwt.split("\\.");
                        String payload = new String(java.util.Base64.getDecoder().decode(chunks[1]));
                        System.out.println("JWT Filter - Token payload: " + payload);
                    } catch (Exception e) {
                        System.out.println("JWT Filter - Error decoding token: " + e.getMessage());
                    }
                    
                    // Extract role directly from token and create authorities
                    List<GrantedAuthority> modifiedAuthorities = new ArrayList<>();
                    String role = jwtService.extractRole(jwt);
                    if (role != null) {
                        // Always add ROLE_ prefix (remove any existing prefix first to avoid ROLE_ROLE_)
                        String cleanRole = role.startsWith("ROLE_") ? role.substring(5) : role;
                        modifiedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + cleanRole));
                        System.out.println("JWT Filter - Added authority: ROLE_" + cleanRole);
                    } else {
                        // Fallback to userDetails authorities if role extraction fails
                        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                        for (GrantedAuthority auth : authorities) {
                            String authority = auth.getAuthority();
                            // Ensure consistent ROLE_ prefix
                            String cleanAuthority = authority.startsWith("ROLE_") ? authority.substring(5) : authority;
                            modifiedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + cleanAuthority));
                            System.out.println("JWT Filter - Added fallback authority: ROLE_" + cleanAuthority);
                        }
                        System.out.println("JWT Filter - Fallback authorities: " + modifiedAuthorities);
                    }
                    
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, modifiedAuthorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    // Log the final authentication object
                    System.out.println("JWT Filter - Authentication set: " + SecurityContextHolder.getContext().getAuthentication());
                }
            } catch (UsernameNotFoundException e) {
                System.out.println("User not found: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
