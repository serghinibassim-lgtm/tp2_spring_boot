package com.bassim.tp2_securPharmacie.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Produit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String nom;
    String description;
    BigDecimal prix;
    String codeBarre;
    LocalDate dateExpiration;

    @OneToOne(cascade = CascadeType.ALL)
    DetailProduit detailProduit;

    @ManyToMany
    @JoinTable(name = "produit_tag", joinColumns = @JoinColumn(name = "produit_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    List<Tag> tags;

    @ManyToOne
    @JoinColumn(name = "fourniseur_id")
    Fournisseure fournisseur;

    @ManyToOne
    @JoinColumn(name = "categorie_id")
    Categorie categorie;
}
