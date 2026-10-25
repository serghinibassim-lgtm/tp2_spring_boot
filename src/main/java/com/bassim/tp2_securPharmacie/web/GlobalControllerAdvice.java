package com.bassim.tp2_securPharmacie.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ControllerAdvice
public class GlobalControllerAdvice {

    private static final List<String> ROLE_ORDER =
            List.of("ADMIN", "PHARMACIEN", "CAISSIER", "CLIENT");

    @ModelAttribute
    public void addRoleAttributes(Model model, Authentication authentication) {
        if (authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return;
        }

        Set<String> roles = new LinkedHashSet<>();
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String value = authority.getAuthority();
            if (value != null && value.startsWith("ROLE_")) {
                roles.add(value.substring(5));
            }
        }

        String primaryRole = ROLE_ORDER.stream()
                .filter(roles::contains)
                .findFirst()
                .orElse("UTILISATEUR");

        model.addAttribute("username", authentication.getName());
        model.addAttribute("roles", roles);
        model.addAttribute("primaryRole", primaryRole);
        model.addAttribute("roleLabel", roleLabel(primaryRole));
        model.addAttribute("roleDescription", roleDescription(primaryRole));
        model.addAttribute("isAdmin", roles.contains("ADMIN"));
        model.addAttribute("isPharmacien", roles.contains("PHARMACIEN"));
        model.addAttribute("isCaissier", roles.contains("CAISSIER"));
        model.addAttribute("isClient", roles.contains("CLIENT"));
    }

    private String roleLabel(String role) {
        return switch (role) {
            case "ADMIN" -> "Administrateur";
            case "PHARMACIEN" -> "Pharmacien";
            case "CAISSIER" -> "Caissier";
            case "CLIENT" -> "Client";
            default -> "Utilisateur";
        };
    }

    private String roleDescription(String role) {
        return switch (role) {
            case "ADMIN" -> "Vous gérez les utilisateurs, les médicaments et les stocks.";
            case "PHARMACIEN" ->
                    "Vous consultez et ajustez les stocks, et vous enregistrez les ventes.";
            case "CAISSIER" -> "Vous enregistrez les ventes de médicaments.";
            case "CLIENT" ->
                    "Vous consultez le catalogue des médicaments disponibles en lecture seule.";
            default -> "Bienvenue dans votre espace pharmacie.";
        };
    }
}
