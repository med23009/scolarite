package com.esp.scolarite.Controller;

import com.esp.scolarite.Service.admin.AdminService;
import com.esp.scolarite.dto.AuthResponse;
import com.esp.scolarite.dto.UserDto;
import com.esp.scolarite.entity.User;
import com.esp.scolarite.exception.NoRoleSelectedException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AdminService adminService;

    @GetMapping
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = adminService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AuthResponse> createUser(@RequestBody UserDto dto) throws NoRoleSelectedException {
        System.out.println("[UserController] Création d'un utilisateur: " + dto.getEmail() + ", rôle: " + dto.getRole());
        return ResponseEntity.ok(adminService.register(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserDto dto) throws NoRoleSelectedException {
        System.out.println("[UserController] Mise à jour de l'utilisateur ID=" + id);
        return adminService.updateUser(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Utilisateur supprimé avec succès");
        return ResponseEntity.ok(response);
    }
}
