package com.esp.scolarite.Service.admin;

import com.esp.scolarite.dto.AuthRequest;
import com.esp.scolarite.dto.AuthResponse;
import com.esp.scolarite.dto.UserDto;
import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.Pole;
import com.esp.scolarite.entity.Role;
import com.esp.scolarite.entity.Semestre;
import com.esp.scolarite.entity.User;
import com.esp.scolarite.exception.NoRoleSelectedException;
import com.esp.scolarite.repository.DepartementRepository;
import com.esp.scolarite.repository.PoleRepository;
import com.esp.scolarite.repository.SemestreRepository;
import com.esp.scolarite.repository.UserRepository;
import com.esp.scolarite.Config.JwtService;
import com.esp.scolarite.entity.ActionHistorique.TypeAction;

// import lombok.RequiredArgsConstructor; // Non utilisé
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.esp.scolarite.entity.Role.*;

@Service
public class AdminService {

    private UserRepository userRepository;
    private DepartementRepository departementRepository;
    private PoleRepository poleRepository;
    private SemestreRepository semestreRepository;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private BCryptPasswordEncoder passwordEncoder;
    private HistoriqueService historiqueService;
    
    public AdminService(UserRepository userRepository,
                       DepartementRepository departementRepository,
                       PoleRepository poleRepository,
                       SemestreRepository semestreRepository,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       BCryptPasswordEncoder passwordEncoder,
                       HistoriqueService historiqueService) {
        this.userRepository = userRepository;
        this.departementRepository = departementRepository;
        this.poleRepository = poleRepository;
        this.semestreRepository = semestreRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.historiqueService = historiqueService;
    }
    
    private Logger LOGGER = LoggerFactory.getLogger(AdminService.class);

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername(); // This should be the email
            } else {
                return principal.toString();
            }
        }
        return "system"; // Default if no authenticated user is found or if it's an anonymous user
    }

    // ==== AUTHENTICATION ====
    
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
        String jwtToken = jwtService.generateToken(extraClaims, user);
        LOGGER.info("Token is successfully generated, The user role is : {}", extraClaims.get("role_membre"));
        return new AuthResponse(jwtToken);
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

    // ==== USERS CRUD ====
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

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
        
        // Sauvegarder l'utilisateur d'abord
        User savedUser = userRepository.save(user);
        System.out.println("[AdminService] REGISTER - Utilisateur sauvegardé avec ID: " + savedUser.getIdMembre());
        
        // Si c'est un chef de département, associer au département correspondant
        if (savedUser.getRole() == Role.CHEF_DEPT) {
            // Vérifier d'abord departementId, puis departement si departementId est null
            Long departementId = request.getDepartementId();
            
            // Si departementId est null mais que departement ne l'est pas, utiliser l'ID du département
            if (departementId == null && request.getDepartement() != null && request.getDepartement().getIdDep() != null) {
                departementId = request.getDepartement().getIdDep();
                System.out.println("[AdminService] REGISTER - Utilisateur avec rôle CHEF_DEPT, ID du département extrait de l'objet departement: " + departementId);
            } else if (departementId != null) {
                System.out.println("[AdminService] REGISTER - Utilisateur avec rôle CHEF_DEPT, ID du département: " + departementId);
            }
            
            if (departementId != null) {
                departementRepository.findById(departementId).ifPresent(departement -> {
                    System.out.println("[AdminService] REGISTER - Département trouvé: " + departement.getIntitule() + " (ID: " + departement.getIdDep() + ")");
                    
                    // Associer l'utilisateur au département
                    savedUser.setDepartement(departement);
                    System.out.println("[AdminService] REGISTER - Utilisateur associé au département");
                    
                    // Définir l'utilisateur comme responsable du département (les deux champs)
                    System.out.println("[AdminService] REGISTER - Ancien responsable: " + 
                        (departement.getResponsable() != null ? departement.getResponsable().getPrenom() + " " + departement.getResponsable().getNom() : "null") + 
                        ", Ancien responsableDepartement: " + 
                        (departement.getResponsableDepartement() != null ? departement.getResponsableDepartement().getPrenom() + " " + departement.getResponsableDepartement().getNom() : "null"));
                    
                    departement.setResponsableDepartement(savedUser);
                    departement.setResponsable(savedUser);
                    
                    // Mettre à jour le nom du responsable avec le nom complet de l'utilisateur
                    String nomComplet = savedUser.getPrenom() + " " + savedUser.getNom();
                    departement.setNom_responsable(nomComplet);
                    System.out.println("[AdminService] REGISTER - Utilisateur défini comme responsable du département: " + nomComplet);
                    
                    // Sauvegarder les modifications du département
                    departementRepository.save(departement);
                    System.out.println("[AdminService] REGISTER - Département mis à jour et sauvegardé");
                    
                    // Mettre à jour l'utilisateur sauvegardé
                    userRepository.save(savedUser);
                    System.out.println("[AdminService] REGISTER - Utilisateur mis à jour avec l'association au département");
                });
            } else {
                System.out.println("[AdminService] REGISTER - Utilisateur avec rôle CHEF_DEPT mais aucun ID de département fourni!");
            }
        }
        
        // Si c'est un chef de pôle, associer au pôle correspondant
        else if (savedUser.getRole() == Role.CHEF_POLE) {
            // Vérifier d'abord poleId, puis pole si poleId est null
            Long poleId = request.getPoleId();
            
            // Si poleId est null mais que pole ne l'est pas, utiliser l'ID du pôle
            if (poleId == null && request.getPole() != null && request.getPole().getIdPole() != null) {
                poleId = request.getPole().getIdPole();
                System.out.println("[AdminService] REGISTER - Utilisateur avec rôle CHEF_POLE, ID du pôle extrait de l'objet pole: " + poleId);
            } else if (poleId != null) {
                System.out.println("[AdminService] REGISTER - Utilisateur avec rôle CHEF_POLE, ID du pôle: " + poleId);
            }
            
            if (poleId != null) {
                poleRepository.findById(poleId).ifPresent(pole -> {
                    System.out.println("[AdminService] REGISTER - Pôle trouvé: " + pole.getIntitule() + " (ID: " + pole.getIdPole() + ")");
                    
                    // Associer l'utilisateur au pôle
                    savedUser.setPole(pole);
                    System.out.println("[AdminService] REGISTER - Utilisateur associé au pôle");
                    
                    // Définir l'utilisateur comme responsable du pôle
                    System.out.println("[AdminService] REGISTER - Ancien responsable: " + 
                        (pole.getResponsable() != null ? pole.getResponsable().getPrenom() + " " + pole.getResponsable().getNom() : "null"));
                    
                    pole.setResponsable(savedUser);
                    
                    // Mettre à jour le nom du responsable avec le nom complet de l'utilisateur
                    String nomComplet = savedUser.getPrenom() + " " + savedUser.getNom();
                    pole.setNom_responsable(nomComplet);
                    System.out.println("[AdminService] REGISTER - Utilisateur défini comme responsable du pôle: " + nomComplet);
                    
                    // Sauvegarder les modifications du pôle
                    poleRepository.save(pole);
                    System.out.println("[AdminService] REGISTER - Pôle mis à jour et sauvegardé");
                    
                    // Mettre à jour l'utilisateur sauvegardé
                    userRepository.save(savedUser);
                    System.out.println("[AdminService] REGISTER - Utilisateur mis à jour avec l'association au pôle");
                });
            } else {
                System.out.println("[AdminService] REGISTER - Utilisateur avec rôle CHEF_POLE mais aucun ID de pôle fourni!");
            }
        }
        
        String jwtToken = jwtService.generateToken(savedUser);

        // Log user creation
        historiqueService.enregistrerAction(
            getCurrentUsername(),
            TypeAction.AJOUT,
            "Ajout utilisateur",
            "Ajout de l'utilisateur: " + savedUser.getEmail() + " (ID: " + savedUser.getIdMembre() + ")"
        );

        // Return the authentication response
        return new AuthResponse(jwtToken);
    }

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
        if (userDto.getRole() != null) {
            user.setRole(roleMatcher(userDto.getRole()));
            
            // Si le rôle est changé à CHEF_DEPT
            if (user.getRole() == Role.CHEF_DEPT) {
                System.out.println("[AdminService] UPDATE - Utilisateur ID=" + user.getIdMembre() + " avec rôle CHEF_DEPT");
                
                // Vérifier d'abord departementId, puis departement si departementId est null
                Long departementId = userDto.getDepartementId();
                
                // Si departementId est null mais que departement ne l'est pas, utiliser l'ID du département
                if (departementId == null && userDto.getDepartement() != null && userDto.getDepartement().getIdDep() != null) {
                    departementId = userDto.getDepartement().getIdDep();
                    System.out.println("[AdminService] UPDATE - ID du département extrait de l'objet departement: " + departementId);
                } else if (departementId != null) {
                    System.out.println("[AdminService] UPDATE - ID du département: " + departementId);
                }
                
                if (departementId != null) {
                    departementRepository.findById(departementId).ifPresent(departement -> {
                        System.out.println("[AdminService] UPDATE - Département trouvé: " + departement.getIntitule() + " (ID: " + departement.getIdDep() + ")");
                        
                        // Associer l'utilisateur au département
                        user.setDepartement(departement);
                        System.out.println("[AdminService] UPDATE - Utilisateur associé au département");
                        
                        // Définir l'utilisateur comme responsable du département (les deux champs)
                        System.out.println("[AdminService] UPDATE - Ancien responsable: " + 
                            (departement.getResponsable() != null ? departement.getResponsable().getPrenom() + " " + departement.getResponsable().getNom() : "null") + 
                            ", Ancien responsableDepartement: " + 
                            (departement.getResponsableDepartement() != null ? departement.getResponsableDepartement().getPrenom() + " " + departement.getResponsableDepartement().getNom() : "null"));
                        
                        departement.setResponsableDepartement(user);
                        departement.setResponsable(user);
                        
                        // Mettre à jour le nom du responsable avec le nom complet de l'utilisateur
                        String nomComplet = user.getPrenom() + " " + user.getNom();
                        departement.setNom_responsable(nomComplet);
                        System.out.println("[AdminService] UPDATE - Utilisateur défini comme responsable du département: " + nomComplet);
                        
                        // Sauvegarder les modifications du département
                        departementRepository.save(departement);
                        System.out.println("[AdminService] UPDATE - Département mis à jour et sauvegardé");
                    });
                } else {
                    System.out.println("[AdminService] UPDATE - Utilisateur avec rôle CHEF_DEPT mais aucun ID de département fourni!");
                }
            } else if (user.getRole() == Role.CHEF_POLE) {
                System.out.println("[AdminService] UPDATE - Utilisateur ID=" + user.getIdMembre() + " avec rôle CHEF_POLE");
                
                // Vérifier d'abord poleId, puis pole si poleId est null
                Long poleId = userDto.getPoleId();
                
                // Si poleId est null mais que pole ne l'est pas, utiliser l'ID du pôle
                if (poleId == null && userDto.getPole() != null && userDto.getPole().getIdPole() != null) {
                    poleId = userDto.getPole().getIdPole();
                    System.out.println("[AdminService] UPDATE - ID du pôle extrait de l'objet pole: " + poleId);
                } else if (poleId != null) {
                    System.out.println("[AdminService] UPDATE - ID du pôle: " + poleId);
                }
                
                if (poleId != null) {
                    poleRepository.findById(poleId).ifPresent(pole -> {
                        System.out.println("[AdminService] UPDATE - Pôle trouvé: " + pole.getIntitule() + " (ID: " + pole.getIdPole() + ")");
                        
                        // Associer l'utilisateur au pôle
                        user.setPole(pole);
                        System.out.println("[AdminService] UPDATE - Utilisateur associé au pôle");
                        
                        // Définir l'utilisateur comme responsable du pôle
                        System.out.println("[AdminService] UPDATE - Ancien responsable: " + 
                            (pole.getResponsable() != null ? pole.getResponsable().getPrenom() + " " + pole.getResponsable().getNom() : "null"));
                        
                        pole.setResponsable(user);
                        
                        // Mettre à jour le nom du responsable avec le nom complet de l'utilisateur
                        String nomComplet = user.getPrenom() + " " + user.getNom();
                        pole.setNom_responsable(nomComplet);
                        System.out.println("[AdminService] UPDATE - Utilisateur défini comme responsable du pôle: " + nomComplet);
                        
                        // Sauvegarder les modifications du pôle
                        poleRepository.save(pole);
                        System.out.println("[AdminService] UPDATE - Pôle mis à jour et sauvegardé");
                    });
                } else {
                    System.out.println("[AdminService] UPDATE - Utilisateur avec rôle CHEF_POLE mais aucun ID de pôle fourni!");
                }
            } else if (user.getRole() != Role.CHEF_DEPT && user.getRole() != Role.CHEF_POLE) {
                // Si le rôle n'est plus CHEF_DEPT ou CHEF_POLE, supprimer les associations appropriées
                System.out.println("[AdminService] UPDATE - Utilisateur ID=" + user.getIdMembre() + " n'est plus CHEF_DEPT ou CHEF_POLE, suppression des associations");
                
                // Gérer l'association avec le département si elle existe
                if (user.getDepartement() != null) {
                    Departement departement = user.getDepartement();
                    System.out.println("[AdminService] UPDATE - Département associé trouvé: " + departement.getIntitule() + " (ID: " + departement.getIdDep() + ")");
                    
                    if (departement.getResponsable() != null && departement.getResponsable().getIdMembre().equals(user.getIdMembre())) {
                        System.out.println("[AdminService] UPDATE - Suppression de l'utilisateur comme responsable du département");
                        departement.setResponsable(null);
                    }
                    
                    if (departement.getResponsableDepartement() != null && departement.getResponsableDepartement().getIdMembre().equals(user.getIdMembre())) {
                        System.out.println("[AdminService] UPDATE - Suppression de l'utilisateur comme responsableDepartement");
                        departement.setResponsableDepartement(null);
                    }
                    
                    departementRepository.save(departement);
                    System.out.println("[AdminService] UPDATE - Département mis à jour après suppression des associations");
                    
                    user.setDepartement(null);
                    System.out.println("[AdminService] UPDATE - Association de l'utilisateur avec le département supprimée");
                }
                
                // Gérer l'association avec le pôle si elle existe
                if (user.getPole() != null) {
                    Pole pole = user.getPole();
                    System.out.println("[AdminService] UPDATE - Pôle associé trouvé: " + pole.getIntitule() + " (ID: " + pole.getIdPole() + ")");
                    
                    if (pole.getResponsable() != null && pole.getResponsable().getIdMembre().equals(user.getIdMembre())) {
                        System.out.println("[AdminService] UPDATE - Suppression de l'utilisateur comme responsable du pôle");
                        pole.setResponsable(null);
                    }
                    
                    poleRepository.save(pole);
                    System.out.println("[AdminService] UPDATE - Pôle mis à jour après suppression des associations");
                    
                    user.setPole(null);
                    System.out.println("[AdminService] UPDATE - Association de l'utilisateur avec le pôle supprimée");
                }
            }
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
        // Nous gérons déjà le département dans la section du rôle
        if (userDto.getPole() != null) {
            user.setPole(userDto.getPole());
        }

        // Save the modifications
        User updatedUser = userRepository.save(user);

        // Log user update
        historiqueService.enregistrerAction(
            getCurrentUsername(),
            TypeAction.MODIFICATION,
            "Modification utilisateur",
            "Modification de l'utilisateur ID: " + updatedUser.getIdMembre() + " (" + updatedUser.getEmail() + ")"
        );

        // Return the updated user
        return ResponseEntity.ok(updatedUser);
    }

    public ResponseEntity<String> deleteUser(Long id) {
        // Check if the user exists
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User userToDelete = userOptional.get();

        // Delete the user
        userRepository.deleteById(id);

        // Log user deletion
        historiqueService.enregistrerAction(
            getCurrentUsername(),
            TypeAction.SUPPRESSION,
            "Suppression utilisateur",
            "Suppression de l'utilisateur ID: " + userToDelete.getIdMembre() + " (" + userToDelete.getEmail() + ")"
        );

        // Return success response
        return ResponseEntity.ok("User deleted successfully");
    }
    
    public boolean changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Check if current password matches
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }

        // Set new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return true;
    }

    // ==== DEPARTEMENTS CRUD ====
    
    public List<Departement> getAllDepartements() {
        return departementRepository.findAll();
    }

    public Optional<Departement> getDepartementById(Long id) {
        return departementRepository.findById(id);
    }

    public Departement saveDepartement(Departement departement) {
        // Log pour le débogage
        System.out.println("=== Débogage saveDepartement ====");
        System.out.println("Sauvegarde du département: " + departement.getIntitule());
        System.out.println("Responsable reçu: " + (departement.getResponsable() != null ? 
                          "ID=" + departement.getResponsable().getIdMembre() : "null"));
        
        boolean isUpdate = departement.getIdDep() != null && departementRepository.existsById(departement.getIdDep());

        try {
            // Vérifiez si le responsable est déjà associé (par son ID)
            if (departement.getResponsable() != null && departement.getResponsable().getIdMembre() != null) {
                Long userId = departement.getResponsable().getIdMembre();
                System.out.println("Recherche de l'utilisateur avec ID: " + userId);
                
                // Récupérer l'utilisateur complet
                Optional<User> userOpt = userRepository.findById(userId);
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    System.out.println("Utilisateur trouvé: " + user.getPrenom() + " " + user.getNom());
                    
                    // Mettre à jour le responsable avec l'utilisateur complet
                    departement.setResponsable(user);
                    
                    // Mettre à jour le nom du responsable avec le nom complet de l'utilisateur
                    if (departement.getNom_responsable() == null || departement.getNom_responsable().isEmpty()) {
                        departement.setNom_responsable(user.getPrenom() + " " + user.getNom());
                    }
                    
                    System.out.println("Responsable assigné avec succès: " + user.getIdMembre());
                } else {
                    System.out.println("ERREUR: Utilisateur avec ID " + userId + " non trouvé");
                }
            } else {
                System.out.println("Aucun responsable spécifié ou ID invalide");
            }
            
            // Sauvegarde du département
            Departement savedDepartement = departementRepository.save(departement);
            System.out.println("Département sauvegardé avec ID: " + savedDepartement.getIdDep());
            
            // Log action
            TypeAction actionType;
            String actionTitle;
            String actionDetails;

            if (isUpdate) {
                actionType = TypeAction.MODIFICATION;
                actionTitle = "Modification département";
                actionDetails = "Département modifié: " + savedDepartement.getIntitule() + " (ID: " + savedDepartement.getIdDep() + ")";
            } else {
                actionType = TypeAction.AJOUT;
                actionTitle = "Ajout département";
                actionDetails = "Nouveau département: " + savedDepartement.getIntitule() + " (ID: " + savedDepartement.getIdDep() + ")";
            }
            historiqueService.enregistrerAction(
                getCurrentUsername(),
                actionType,
                actionTitle,
                actionDetails
            );
            
            System.out.println("Responsable final: " + (savedDepartement.getResponsable() != null ? 
                              "ID=" + savedDepartement.getResponsable().getIdMembre() : "null"));
            
            return savedDepartement;
        } catch (Exception e) {
            System.out.println("ERREUR lors de la sauvegarde du département: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

  public void deleteDepartement(Long id) {
        // Vérification si le département existe
        Departement departement = departementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Département non trouvé"));

        // Enregistrement de la suppression dans l'historique
        historiqueService.enregistrerAction(
            "system", // Utilisateur système par défaut
            TypeAction.SUPPRESSION,
            "Suppression département",
            "Département supprimé: " + departement.getCodeDep() + " - " + departement.getIntitule()
        );

        // Supprimer le département
        departementRepository.deleteById(id);
    }

  public List<Departement> importDepartementsFromCSV(MultipartFile file) throws Exception {
    List<Departement> importedDepartements = new ArrayList<>();
    
    try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        boolean firstLine = true;
        
        while ((line = br.readLine()) != null) {
            if (firstLine) {
                firstLine = false;
                continue; // Ignorer l'en-tête
            }
            
            String[] data = line.split(",");
            if (data.length < 2) continue; // Ligne invalide
            
            String codeDep = data[0].trim();
            String intitule = data[1].trim();
            String description = data.length > 2 ? data[2].trim() : "";
            
            // Vérifier si le département existe déjà par son code
            Optional<Departement> existingDept = departementRepository.findByCodeDep(codeDep);
            
            Departement departement;
            if (existingDept.isPresent()) {
                // Mettre à jour le département existant
                departement = existingDept.get();
                departement.setIntitule(intitule);
                if (!description.isEmpty()) {
                    departement.setDescription(description);
                }
                System.out.println("Mise à jour du département existant: " + codeDep);
            } else {
                // Créer un nouveau département
                departement = new Departement();
                departement.setCodeDep(codeDep);
                departement.setIntitule(intitule);
                departement.setDescription(description);
                System.out.println("Création d'un nouveau département: " + codeDep);
            }
            
            importedDepartements.add(departementRepository.save(departement));
        }
    }
    
    // Enregistrer l'importation dans l'historique
    historiqueService.enregistrerAction(
        "system", // Utilisateur système par défaut
        TypeAction.IMPORT,
        "Import départements",
        "Importation de " + importedDepartements.size() + " départements depuis un fichier système"
    );
    
    return importedDepartements;
  }

    public Optional<Departement> getDepartementByUserEmail(String email) {
        // Find all departments
        List<Departement> allDepartements = departementRepository.findAll();
        
        // Find the department where the user is responsible
        return allDepartements.stream()
                .filter(dept -> {
                    // Check via responsableDepartement field
                    boolean isResponsibleViaField = dept.getResponsableDepartement() != null && 
                        email.equals(dept.getResponsableDepartement().getEmail());
                    
                    // Check via responsable field
                    boolean isResponsibleViaId = dept.getResponsable() != null && 
                        email.equals(dept.getResponsable().getEmail());
                    
                    return isResponsibleViaField || isResponsibleViaId;
                })
                .findFirst();
    }

    // ==== POLES CRUD ====
    
    public List<Pole> getAllPoles() {
        return poleRepository.findAll();
    }

    public Optional<Pole> getPoleById(Long id) {
        return poleRepository.findById(id);
    }

    public Pole savePole(Pole pole) {
        // Vérifier si le pôle existe déjà (mise à jour)
        boolean isUpdate = pole.getIdPole() != null && poleRepository.existsById(pole.getIdPole());
        
        // Vérifier si le code du pôle est unique
        if (!isUpdate) {
            Optional<Pole> existingPoleByCode = poleRepository.findByCodePole(pole.getCodePole());
            if (existingPoleByCode.isPresent()) {
                throw new RuntimeException("Un pôle avec ce code existe déjà");
            }
        }
        
        // Si c'est une mise à jour, récupérer les informations existantes
        if (isUpdate) {
            Pole existingPole = poleRepository.findById(pole.getIdPole())
                .orElseThrow(() -> new RuntimeException("Pôle non trouvé"));
            
            // Conserver les associations existantes
            pole.setUniteEnseignement(existingPole.getUniteEnseignement());
            
            // Enregistrer la modification dans l'historique
            historiqueService.enregistrerAction(
                "system", // Utilisateur système par défaut, à remplacer par l'utilisateur réel si disponible
                TypeAction.MODIFICATION,
                "Modification pôle",
                "Pôle modifié: " + pole.getCodePole() + " - " + pole.getIntitule()
            );
        } else {
            // Enregistrer l'ajout dans l'historique
            historiqueService.enregistrerAction(
                "system", // Utilisateur système par défaut, à remplacer par l'utilisateur réel si disponible
                TypeAction.AJOUT,
                "Ajout pôle",
                "Nouveau pôle: " + pole.getCodePole() + " - " + pole.getIntitule()
            );
        }
        
        // Vérifiez si le responsable est déjà associé (par son ID)
        if (pole.getResponsable() != null && pole.getResponsable().getIdMembre() != null) {
            // Récupérer l'utilisateur complet
            userRepository.findById(pole.getResponsable().getIdMembre())
                .ifPresent(user -> {
                    // Mettre à jour le responsable avec l'utilisateur complet
                    pole.setResponsable(user);
                    
                    // Mettre à jour le nom du responsable avec le nom complet de l'utilisateur
                    if (pole.getNom_responsable() == null || pole.getNom_responsable().isEmpty()) {
                        pole.setNom_responsable(user.getPrenom() + " " + user.getNom());
                    }
                });
        }
        
        return poleRepository.save(pole);
    }

    public void deletePole(Long id) {
        // Récupérer les informations du pôle avant suppression
        Pole pole = poleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pôle non trouvé"));
        
        // Enregistrer la suppression dans l'historique
        historiqueService.enregistrerAction(
            "system", // Utilisateur système par défaut, à remplacer par l'utilisateur réel si disponible
            TypeAction.SUPPRESSION,
            "Suppression pôle",
            "Pôle supprimé: " + pole.getCodePole() + " - " + pole.getIntitule()
        );
        
        poleRepository.deleteById(id);
    }


    // Surcharge pour InputStream
    public List<Pole> importPolesFromCSV(InputStream inputStream) throws Exception {
        List<Pole> importedPoles = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Ignorer l'en-tête
                }
                String[] values = line.split(",");
                if (values.length < 3) {
                    throw new Exception("Format CSV invalide, attendu: codePole,intitule,description");
                }
                String codePole = values[0].trim();
                String intitule = values[1].trim();
                String description = values[2].trim();
                String nomResponsable = values.length > 3 ? values[3].trim() : "";
                Pole pole = new Pole();
                pole.setCodePole(codePole);
                pole.setIntitule(intitule);
                pole.setDescription(description);
                pole.setNom_responsable(nomResponsable);
                importedPoles.add(poleRepository.save(pole));
            }
        }
        // Enregistrer l'importation dans l'historique
        historiqueService.enregistrerAction(
            "system",
            TypeAction.IMPORT,
            "Import pôles",
            "Importation de " + importedPoles.size() + " pôles depuis un fichier système"
        );
        return importedPoles;
    }

    public List<Pole> importPolesFromCSV(MultipartFile file) {
        try {
            return importPolesFromCSV(file.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'importation des pôles: " + e.getMessage(), e);
        }
    }

    // ==== SEMESTRES CRUD ====
    
    public List<Semestre> getAllSemestres() {
        return semestreRepository.findAll();
    }

    public Optional<Semestre> getSemestreById(Long id) {
        return semestreRepository.findById(id);
    }

    public Semestre saveSemestre(Semestre semestre) {
        // Vérifier si le semestre existe déjà (mise à jour)
        boolean isUpdate = semestre.getIdSemestre() != null && semestreRepository.existsById(semestre.getIdSemestre());
        
        if (isUpdate) {
            // Enregistrer la modification dans l'historique
            historiqueService.enregistrerAction(
                "system", // Utilisateur système par défaut, à remplacer par l'utilisateur réel si disponible
                TypeAction.MODIFICATION,
                "Modification semestre",
                "Semestre modifié: " + semestre.getSemestre() + " de l'année " + semestre.getAnnee()
            );
        } else {
            // Enregistrer l'ajout dans l'historique
            historiqueService.enregistrerAction(
                "system", // Utilisateur système par défaut, à remplacer par l'utilisateur réel si disponible
                TypeAction.AJOUT,
                "Ajout semestre",
                "Nouveau semestre: " + semestre.getSemestre() + " de l'année " + semestre.getAnnee()
            );
        }
        
        return semestreRepository.save(semestre);
    }

    public void deleteSemestre(Long id) {
        // Récupérer les informations du semestre avant suppression
        Semestre semestre = semestreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Semestre non trouvé"));
        
        // Enregistrer la suppression dans l'historique
        historiqueService.enregistrerAction(
            "system", // Utilisateur système par défaut, à remplacer par l'utilisateur réel si disponible
            TypeAction.SUPPRESSION,
            "Suppression semestre",
            "Semestre supprimé: " + semestre.getSemestre() + " de l'année " + semestre.getAnnee()
        );
        
        semestreRepository.deleteById(id);
    }



        

    // Overloaded method for importing departements from an InputStream
    public List<Departement> importDepartementsFromCSV(InputStream inputStream) throws Exception {
        List<Departement> importedDepartements = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Ignorer l'en-tête
                }
                
                String[] values = line.split(",");
                if (values.length < 3) {
                    throw new Exception("Format CSV invalide, attendu: code,intitule,description");
                }
                
                String codeDep = values[0].trim();
                String intitule = values[1].trim();
                String description = values[2].trim();
                String nomResponsable = values.length > 3 ? values[3].trim() : "";
                
                Departement departement = new Departement(codeDep, intitule, description, nomResponsable);
                importedDepartements.add(departementRepository.save(departement));
            }
        }
        // Enregistrer l'importation dans l'historique
        historiqueService.enregistrerAction(
            "system", // Utilisateur système par défaut
            TypeAction.IMPORT,
            "Import départements",
            "Importation de " + importedDepartements.size() + " départements depuis un fichier système"
        );
        
        return importedDepartements;
    }
    
    // Méthode pour importer des semestres depuis un fichier MultipartFile
    public List<Semestre> importSemestresFromCSV(MultipartFile file) {
        try {
            return importSemestresFromCSV(file.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'importation des semestres: " + e.getMessage(), e);
        }
    }
    
    // Méthode surchargée pour importer des semestres depuis un InputStream
    public List<Semestre> importSemestresFromCSV(InputStream inputStream) throws Exception {
        List<Semestre> importedSemestres = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Ignorer l'en-tête
                }
                
                String[] values = line.split(",");
                if (values.length < 5) {
                    throw new Exception("Format CSV invalide, attendu: nombreSemaines,dateDebut,dateFin,annee,semestre,creditSpecialite,creditHE,creditST");
                }
                
                int nombreSemaines = Integer.parseInt(values[0].trim());
                Date dateDebut = dateFormat.parse(values[1].trim());
                Date dateFin = dateFormat.parse(values[2].trim());
                int annee = Integer.parseInt(values[3].trim());
                String semestre = values[4].trim();
                
                // Récupération des crédits si présents
                int creditSpecialite = values.length > 5 ? (int)Double.parseDouble(values[5].trim()) : 0;
                int creditHE = values.length > 6 ? (int)Double.parseDouble(values[6].trim()) : 0;
                int creditST = values.length > 7 ? (int)Double.parseDouble(values[7].trim()) : 0;
                
                Semestre semestreObj = new Semestre();
                semestreObj.setNombreSemaines(nombreSemaines);
                semestreObj.setDateDebut(dateDebut);
                semestreObj.setDateFin(dateFin);
                semestreObj.setAnnee(annee);
                semestreObj.setSemestre(semestre);
                semestreObj.setCreditSpecialite(creditSpecialite);
                semestreObj.setCreditHE(creditHE);
                semestreObj.setCreditST(creditST);
                
                importedSemestres.add(semestreRepository.save(semestreObj));
            }
        }
        
        // Enregistrer l'importation dans l'historique
        historiqueService.enregistrerAction(
            "system", // Utilisateur système par défaut
            TypeAction.IMPORT,
            "Import semestres",
            "Importation de " + importedSemestres.size() + " semestres depuis un fichier système"
        );
        
        return importedSemestres;
    }
}