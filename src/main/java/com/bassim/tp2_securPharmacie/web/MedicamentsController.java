package com.bassim.tp2_securPharmacie.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur MVC chargé d'afficher la page Thymeleaf des médicaments.
 * Les opérations POST / PUT / DELETE sont appelées en JavaScript sur l'API REST.
 */
@Controller
public class MedicamentsController {

    @GetMapping({"/", "/medicaments", "/medicaments.html"})
    public String pageMedicaments() {
        return "medicaments";
    }
}
