package com.esp.scolarite.Service.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.esp.scolarite.entity.Role;
import com.esp.scolarite.exception.NoRoleSelectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.esp.scolarite.Config.JwtService;
import com.esp.scolarite.Service.admin.HistoriqueService;
import com.esp.scolarite.dto.AuthRequest;
import com.esp.scolarite.dto.AuthResponse;
import com.esp.scolarite.dto.UserDto;
import com.esp.scolarite.entity.ActionHistorique.TypeAction;
import com.esp.scolarite.entity.User;
import com.esp.scolarite.repository.UserRepository;

import static com.esp.scolarite.entity.Role.*;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;
    private final HistoriqueService historiqueService;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    
    public AuthService(UserRepository userRepository, JwtService jwtService, 
                      AuthenticationManager authenticationManager, BCryptPasswordEncoder passwordEncoder,
                      HistoriqueService historiqueService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.historiqueService = historiqueService;
    }

    // Authenticate a user and return a JWT token
    public AuthResponse authenticate(AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(), authRequest.getPassword()));
            System.out.println("Authentication successful");
        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            throw new RuntimeException("Authentication failed", e);
        }

        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role_membre", user.getMemberRole());
        // Add passwordChanged to the JWT claims
        extraClaims.put("password_changed", user.isPasswordChanged());
        String jwtToken = jwtService.generateToken(extraClaims, user);
        LOGGER.info("Token is successfully generated, The user role is : {}, Password changed: {}", 
                extraClaims.get("role_membre"), user.isPasswordChanged());
                
        // Enregistrer la connexion dans l'historique
        historiqueService.enregistrerAction(
            user.getEmail(),
            TypeAction.LOGIN,
            "Connexion",
            "Utilisateur connecté: " + user.getNom() + " " + user.getPrenom() + " (" + user.getRole() + ")"
        );
        
        return new AuthResponse(jwtToken, user.isPasswordChanged());
    }

    private Role roleMatcher(int roleCode) throws NoRoleSelectedException {
        return switch (roleCode) {
            case 0 -> ADMIN;
            case 1 -> CHEF_DEPT;
            case 2 -> CHEF_POLE;
            case 3 -> DE;
            case 4 -> RS;
            default -> throw new NoRoleSelectedException("No role has been selected for this user");
        };
    }

    // Register a new user and return a JWT token
    public AuthResponse register(UserDto request) throws NoRoleSelectedException {
        // Check if email is already in use
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new AuthResponse("");
        }

        // Validate required fields
        if (request.getNom() == null || request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Name, email, and password are required");
        }

        // Create a new user
        User user = new User();
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setDateNaissance(request.getDateNaissance());
        user.setCompteBancaire(request.getCompteBancaire());
        user.setTelephone(request.getTelephone());
        user.setEmail(request.getEmail());
        user.setNationalite(request.getNationalite());
        user.setNNI(request.getNNI());
        user.setNomBanque(request.getNomBanque());
        user.setGenre(request.getGenre());
        user.setSpecialiste(request.getSpecialiste());
        user.setLieuDeNaissance(request.getLieuDeNaissance());
        user.setRole(roleMatcher(request.getRole()));
        // Encode the password before saving
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);

        // Enregistrer la création de compte dans l'historique
        historiqueService.enregistrerAction(
            user.getEmail(),
            TypeAction.AJOUT,
            "Création de compte",
            "Nouveau compte créé pour: " + user.getNom() + " " + user.getPrenom() + " (" + user.getRole() + ")"
        );
        
        // Return the authentication response
        return new AuthResponse(jwtToken);
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public ResponseEntity<User> getUserById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(userOptional.get());
    }

    // Delete a user
    public ResponseEntity<String> deleteUser(Long id) {
        // Check if the user exists
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // Récupérer les informations de l'utilisateur avant suppression
        User user = userOptional.get();
        String userInfo = user.getNom() + " " + user.getPrenom() + " (" + user.getEmail() + ")";
        
        // Delete the user
        userRepository.deleteById(id);
        
        // Enregistrer la suppression dans l'historique
        historiqueService.enregistrerAction(
            "system", // Utiliser "system" car l'utilisateur est supprimé
            TypeAction.SUPPRESSION,
            "Suppression d'utilisateur",
            "Utilisateur supprimé: " + userInfo
        );

        // Return success response
        return ResponseEntity.ok("User deleted successfully");
    }

    // Update an existing user
    public ResponseEntity<User> updateUser(Long id, UserDto userDto) throws NoRoleSelectedException {
        // Check if the user exists
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Get the existing user
        User user = userOptional.get();

        // Update fields provided in userDto
        if (userDto.getNom() != null) {
            user.setNom(userDto.getNom());
        }
        if (userDto.getPrenom() != null) {
            user.setPrenom(userDto.getPrenom());
        }
        if (userDto.getDateNaissance() != null) {
            user.setDateNaissance(userDto.getDateNaissance());
        }
        if (userDto.getCompteBancaire() != null) {
            user.setCompteBancaire(userDto.getCompteBancaire());
        }
        if (userDto.getTelephone() != null) {
            user.setTelephone(userDto.getTelephone());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getNationalite() != null) {
            user.setNationalite(userDto.getNationalite());
        }
        if (userDto.getNNI() != null) {
            user.setNNI(userDto.getNNI());
        }
        if (userDto.getNomBanque() != null) {
            user.setNomBanque(userDto.getNomBanque());
        }
        if (userDto.getRole() != (Integer) null && !roleMatcher(userDto.getRole()).equals(user.getRole())) {
            user.setRole(roleMatcher(userDto.getRole()));
        }
        if (userDto.getGenre() != null) {
            user.setGenre(userDto.getGenre());
        }
        if (userDto.getSpecialiste() != null) {
            user.setSpecialiste(userDto.getSpecialiste());
        }
        if (userDto.getLieuDeNaissance() != null) {
            user.setLieuDeNaissance(userDto.getLieuDeNaissance());
        }
        if (userDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword())); // Encode the password
        }

        // Save the modifications
        User updatedUser = userRepository.save(user);
        
        // Enregistrer la modification dans l'historique
        historiqueService.enregistrerAction(
            updatedUser.getEmail(),
            TypeAction.MODIFICATION,
            "Modification d'utilisateur",
            "Profil utilisateur modifié: " + updatedUser.getNom() + " " + updatedUser.getPrenom()
        );

        // Return the updated user
        return ResponseEntity.ok(updatedUser);
    }

    public boolean changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Check if current password matches
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        // Set new password and mark as changed
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(true);
        userRepository.save(user);
        
        LOGGER.info("Password changed successfully for user: {}", email);
        
        // Enregistrer le changement de mot de passe dans l'historique
        historiqueService.enregistrerAction(
            email,
            TypeAction.MODIFICATION,
            "Changement de mot de passe",
            "Mot de passe modifié pour l'utilisateur: " + email
        );

        return true;
    }
}