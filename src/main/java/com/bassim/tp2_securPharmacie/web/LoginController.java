package com.bassim.tp2_securPharmacie.web;

import com.bassim.tp2_securPharmacie.repositories.ProduitRepository;
import com.bassim.tp2_securPharmacie.repositories.UserRepository;
import com.bassim.tp2_securPharmacie.repositories.VenteRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class LoginController {

    private final ProduitRepository produitRepository;
    private final VenteRepository venteRepository;
    private final UserRepository userRepository;

    public LoginController(ProduitRepository produitRepository,
                           VenteRepository venteRepository,
                           UserRepository userRepository) {
        this.produitRepository = produitRepository;
        this.venteRepository = venteRepository;
        this.userRepository = userRepository;
    }

    @GetMapping({"/login", "/login.html"})
    public String login(Authentication authentication) {
        if (authentication != null
                && authentication.getPrincipal() instanceof UserDetails) {
            return "redirect:/";
        }
        return "login";
    }

    @GetMapping({"/", "/home", "/home.html"})
    public String home(Model model) {
        model.addAttribute("activeNav", "home");
        model.addAttribute("produitCount", produitRepository.count());
        model.addAttribute("availableCount",
                produitRepository.countByStockGreaterThan(0));
        model.addAttribute("lowStockCount",
                produitRepository.countByStockBetween(1, 4));
        model.addAttribute("expiredCount",
                produitRepository.countByDateExpirationBefore(LocalDate.now()));
        model.addAttribute("dernieresVentes",
                venteRepository.findTop5ByOrderByDateVenteDesc());
        model.addAttribute("userCount", userRepository.count());
        return "home";
    }

    @GetMapping("/access-denied")
    public String accessDenied(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return "access-denied";
    }
}
