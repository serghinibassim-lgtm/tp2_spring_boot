package com.bassim.tp2_securPharmacie.dtos;

import com.bassim.tp2_securPharmacie.models.Produit;
import com.bassim.tp2_securPharmacie.models.Vente;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VenteDto(
        Long id,
        Long produitId,
        String produitNom,
        Integer quantite,
        BigDecimal prixUnitaire,
        BigDecimal montant,
        LocalDateTime dateVente,
        String vendeur
) {

    public static VenteDto from(Vente vente) {
        Produit produit = vente.getProduit();
        return new VenteDto(
                vente.getId(),
                produit != null ? produit.getId() : null,
                produit != null ? produit.getNom() : null,
                vente.getQuantite(),
                vente.getPrixUnitaire(),
                vente.getMontant(),
                vente.getDateVente(),
                vente.getVendeur()
        );
    }
}
