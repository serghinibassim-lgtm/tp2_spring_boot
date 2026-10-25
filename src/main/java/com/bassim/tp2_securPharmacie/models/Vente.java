package com.bassim.tp2_securPharmacie.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prix_unitaire", precision = 10, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_vente", nullable = false)
    private LocalDateTime dateVente;

    @Column(nullable = false, length = 100)
    private String vendeur;

    @Transient
    public String getDateVenteFormatted() {
        return dateVente == null
                ? "—"
                : dateVente.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
