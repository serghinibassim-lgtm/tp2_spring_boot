package com.bassim.tp2_securPharmacie.Services;

import com.bassim.tp2_securPharmacie.models.Produit;
import com.bassim.tp2_securPharmacie.models.Vente;
import com.bassim.tp2_securPharmacie.repositories.ProduitRepository;
import com.bassim.tp2_securPharmacie.repositories.VenteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class VenteService {

    private final ProduitRepository produitRepository;
    private final VenteRepository venteRepository;

    public VenteService(ProduitRepository produitRepository,
                        VenteRepository venteRepository) {
        this.produitRepository = produitRepository;
        this.venteRepository = venteRepository;
    }

    @Transactional
    public Vente vendre(Long produitId, Integer quantite, String vendeur) {
        if (produitId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Le produit est obligatoire.");
        }
        if (quantite == null || quantite <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La quantité doit être supérieure à zéro.");
        }

        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produit introuvable : " + produitId));

        int stock = produit.getStock() == null ? 0 : produit.getStock();
        if (stock < quantite) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Stock insuffisant : " + stock + " unité(s) disponible(s).");
        }

        produit.setStock(stock - quantite);
        produitRepository.save(produit);

        BigDecimal prixUnitaire = produit.getPrix() == null
                ? BigDecimal.ZERO
                : produit.getPrix();

        Vente vente = new Vente();
        vente.setProduit(produit);
        vente.setQuantite(quantite);
        vente.setPrixUnitaire(prixUnitaire);
        vente.setMontant(prixUnitaire.multiply(BigDecimal.valueOf(quantite)));
        vente.setDateVente(LocalDateTime.now());
        vente.setVendeur(vendeur == null ? "inconnu" : vendeur);

        return venteRepository.save(vente);
    }
}
