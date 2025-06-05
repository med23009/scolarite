package com.esp.scolarite.Config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAutentificationFilter jwtAutentificationFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (no authentication required)
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/change-password").permitAll()
                        .requestMatchers("/api/de/**").hasAuthority("ROLE_DE")
                        .requestMatchers("/api/de/programmes/**").hasAuthority("ROLE_DE")

                        // Admin endpoints - removed permitAll to enforce role checking
                        .requestMatchers("/api/admin/departements/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DE","ROLE_RS")
                        .requestMatchers("/api/admin/poles/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DE")
                        .requestMatchers("/api/admin/semestres/**").permitAll()
                        .requestMatchers("/api/admin/users/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DE")
                        .requestMatchers("/api/admin/users/edit/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DE")
                        .requestMatchers("/api/admin/register").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/semestres/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // Admin-only endpoints
                        .requestMatchers("/api/auth/**").hasAuthority("ROLE_ADMIN")

                        // Department management
                        .requestMatchers("/api/departements/user")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_CHEF_DEPT", "ROLE_CHEF_POLE", "ROLE_RS", "ROLE_DE")
                        .requestMatchers("/api/departements/all")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_RS", "ROLE_DE", "ROLE_CHEF_DEPT", "ROLE_CHEF_POLE")
                        .requestMatchers("/api/departements/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_CHEF_DEPT", "ROLE_CHEF_POLE", "ROLE_RS", "ROLE_DE")
                        .requestMatchers("/api/bulletins/**").permitAll()

                        // Student management
                        .requestMatchers("/api/etudiants/**")
                        .hasAnyAuthority("ROLE_RS", "ROLE_DE", "ROLE_CHEF_DEPT", "ROLE_CHEF_POLE")
                        .requestMatchers("/api/etudiants/consult")
                        .hasAnyAuthority("ROLE_CHEF_POLE", "ROLE_CHEF_DEPT", "ROLE_RS")
                        .requestMatchers("/api/etudiants/search")
                        .hasAnyAuthority("ROLE_RS", "ROLE_DE", "ROLE_CHEF_DEPT", "ROLE_CHEF_POLE")

                        // Grade management
                        .requestMatchers("/api/notes/**").hasAnyAuthority("ROLE_CHEF_DEPT", "ROLE_CHEF_POLE")
                        .requestMatchers("/api/notes/import").hasAnyAuthority("ROLE_CHEF_DEPT", "ROLE_CHEF_POLE")

                        // Academic program management
                        .requestMatchers("/api/releve-notes/**")
                        .hasAnyAuthority("ROLE_RS", "ROLE_DE", "ROLE_CHEF_DEPT", "ROLE_CHEF_POLE")
                        .requestMatchers("/api/programmes/**")
                        .hasAnyAuthority("ROLE_CHEF_DEPT", "ROLE_CHEF_POLE", "ROLE_DE")
                        .requestMatchers("/api/chef-dept/programmes/**").hasAnyAuthority("ROLE_CHEF_DEPT", "ROLE_ADMIN")
                        .requestMatchers("/api/chef-pole/programmes/**").hasAnyAuthority("ROLE_CHEF_POLE", "ROLE_ADMIN")
                        .requestMatchers("/api/poles/all").hasAnyAuthority("ROLE_DE", "ROLE_ADMIN", "ROLE_CHEF_DEPT", "ROLE_CHEF_POLE")
                        .requestMatchers("/api/semestres/all").hasAnyAuthority("ROLE_DE", "ROLE_ADMIN", "ROLE_CHEF_DEPT", "ROLE_CHEF_POLE")

                        // Plan d'étude
                        .requestMatchers("/api/plan-etude/**")
                        .hasAnyAuthority("ROLE_RS", "ROLE_CHEF_DEPT", "ROLE_CHEF_POLE")

                        // Document export
                        .requestMatchers("/api/pv/**").hasAnyAuthority("ROLE_RS", "ROLE_CHEF_DEPT", "ROLE_CHEF_POLE")

                        // History tracking
                        .requestMatchers("/api/historique/**").hasAuthority("ROLE_ADMIN")

                        // Require authentication for all other endpoints
                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAutentificationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}