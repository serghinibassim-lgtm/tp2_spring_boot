package com.bassim.tp2_securPharmacie.Services;
import com.bassim.tp2_securPharmacie.models.Role;
import com.bassim.tp2_securPharmacie.models.RolUser;
import com.bassim.tp2_securPharmacie.models.User;
import com.bassim.tp2_securPharmacie.repositories.RoleRepository;
import com.bassim.tp2_securPharmacie.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class InitData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public InitData(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        createUserIfNotExists(
                "admin", "admin123", RolUser.ADMIN);

        createUserIfNotExists(
                "pharmacien", "pharma123", RolUser.PHARMACIEN);

        createUserIfNotExists(
                "caissier", "caissier123", RolUser.CAISSIER);

        createUserIfNotExists(
                "client", "client123", RolUser.CLIENT);
    }

    private void createUserIfNotExists(
            String username,
            String password,
            RolUser roleName) {

        Role role = roleRepository.findByRol(roleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setRol(roleName);
                    return roleRepository.save(newRole);
                });

        User user = userRepository.findUserByUsername(username).orElse(null);

        if (user == null) {
            user = new User();
            user.setUsername(username);
            System.out.println(
                    "Utilisateur créé : " + username
                            + " | rôle : " + roleName);
        } else {
            System.out.println(
                    "Utilisateur déjà existant : " + username);
        }

        // Comptes de démonstration : on garantit le rôle et le mot de passe
        // annoncés sur la page de connexion, même si la base existe déjà.
        user.setRoles(new HashSet<>(Set.of(role)));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            System.out.println(
                    "Mot de passe réinitialisé pour le compte de démonstration : "
                            + username);
        }

        userRepository.save(user);
    }
}
