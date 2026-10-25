package com.bassim.tp2_securPharmacie.web;

import com.bassim.tp2_securPharmacie.models.Role;
import com.bassim.tp2_securPharmacie.models.RolUser;
import com.bassim.tp2_securPharmacie.models.User;
import com.bassim.tp2_securPharmacie.repositories.ProduitRepository;
import com.bassim.tp2_securPharmacie.repositories.RoleRepository;
import com.bassim.tp2_securPharmacie.repositories.UserRepository;
import com.bassim.tp2_securPharmacie.repositories.VenteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProduitRepository produitRepository;
    private final VenteRepository venteRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository,
                           RoleRepository roleRepository,
                           ProduitRepository produitRepository,
                           VenteRepository venteRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.produitRepository = produitRepository;
        this.venteRepository = venteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String admin(Model model) {
        List<User> users = userRepository.findAll(Sort.by("id"));

        model.addAttribute("users", users);
        model.addAttribute("roles", RolUser.values());
        model.addAttribute("userCount", users.size());
        model.addAttribute("adminCount", userRepository.countByRole(RolUser.ADMIN));
        model.addAttribute("produitCount", produitRepository.count());
        model.addAttribute("ventesJour", venteRepository.countByDateVenteAfter(
                LocalDate.now().atStartOfDay()));
        model.addAttribute("activeNav", "admin");
        return "admin";
    }

    @PostMapping("/users")
    public String creerUtilisateur(@RequestParam(defaultValue = "") String username,
                                   @RequestParam(defaultValue = "") String password,
                                   @RequestParam RolUser role,
                                   RedirectAttributes redirectAttributes) {
        String nom = username.trim();

        if (nom.length() < 3) {
            redirectAttributes.addFlashAttribute("error",
                    "Le nom d'utilisateur doit contenir au moins 3 caractères.");
            return "redirect:/admin";
        }
        if (password.length() < 6) {
            redirectAttributes.addFlashAttribute("error",
                    "Le mot de passe doit contenir au moins 6 caractères.");
            return "redirect:/admin";
        }
        if (userRepository.findUserByUsername(nom).isPresent()) {
            redirectAttributes.addFlashAttribute("error",
                    "L'utilisateur « " + nom + " » existe déjà.");
            return "redirect:/admin";
        }

        User user = new User();
        user.setUsername(nom);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(new HashSet<>(Set.of(roleEntity(role))));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success",
                "Utilisateur « " + nom + " » créé avec le rôle " + role + ".");
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/role")
    public String changerRole(@PathVariable Long id,
                              @RequestParam RolUser role,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Utilisateur introuvable.");
            return "redirect:/admin";
        }
        if (user.getUsername().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error",
                    "Vous ne pouvez pas modifier votre propre rôle.");
            return "redirect:/admin";
        }
        if (hasRole(user, RolUser.ADMIN) && role != RolUser.ADMIN
                && userRepository.countByRole(RolUser.ADMIN) <= 1) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de retirer le dernier administrateur.");
            return "redirect:/admin";
        }

        user.setRoles(new HashSet<>(Set.of(roleEntity(role))));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success",
                "Le rôle de « " + user.getUsername() + " » est maintenant " + role + ".");
        return "redirect:/admin";
    }

    @PostMapping("/users/{id}/delete")
    public String supprimerUtilisateur(@PathVariable Long id,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error",
                    "Utilisateur introuvable.");
            return "redirect:/admin";
        }
        if (user.getUsername().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error",
                    "Vous ne pouvez pas supprimer votre propre compte.");
            return "redirect:/admin";
        }
        if (hasRole(user, RolUser.ADMIN)
                && userRepository.countByRole(RolUser.ADMIN) <= 1) {
            redirectAttributes.addFlashAttribute("error",
                    "Impossible de supprimer le dernier administrateur.");
            return "redirect:/admin";
        }

        userRepository.delete(user);
        redirectAttributes.addFlashAttribute("success",
                "Utilisateur « " + user.getUsername() + " » supprimé.");
        return "redirect:/admin";
    }

    private Role roleEntity(RolUser rol) {
        return roleRepository.findByRol(rol).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setRol(rol);
            return roleRepository.save(newRole);
        });
    }

    private boolean hasRole(User user, RolUser rol) {
        return user.getRoles() != null
                && user.getRoles().stream().anyMatch(r -> r.getRol() == rol);
    }
}
