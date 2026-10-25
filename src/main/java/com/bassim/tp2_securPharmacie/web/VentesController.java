package com.bassim.tp2_securPharmacie.web;

import com.bassim.tp2_securPharmacie.Services.VenteService;
import com.bassim.tp2_securPharmacie.dtos.VenteDto;
import com.bassim.tp2_securPharmacie.dtos.VenteRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ventes")
public class VentesController {

    private final VenteService venteService;

    public VentesController(VenteService venteService) {
        this.venteService = venteService;
    }

    @PostMapping
    public VenteDto vendre(@RequestBody VenteRequest request,
                           Authentication authentication) {
        if (request == null || request.produitId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Le produit est obligatoire.");
        }

        return VenteDto.from(venteService.vendre(
                request.produitId(),
                request.quantite(),
                authentication.getName()));
    }
}
