package com.esp.scolarite.Service;

import com.esp.scolarite.entity.Departement;
import com.esp.scolarite.entity.User;
import com.esp.scolarite.repository.DepartementRepository;
import com.esp.scolarite.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartementRepository departementRepository;

    /**
     * Get the department for which the user is the chef
     * @param email Email of the chef de département
     * @return Department or null if not found
     */
    public Departement getDepartementByChefEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            return null;
        }
        
        User user = userOpt.get();
        
        // Check if the user's role is CHEF_DEPT
        if (user.getRole() != null && user.getRole().name().equals("CHEF_DEPT")) {
            return departementRepository.findByResponsable(user);
        }
        
        return null;
    }
}
