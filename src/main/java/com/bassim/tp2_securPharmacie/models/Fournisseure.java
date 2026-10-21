package com.bassim.tp2_securPharmacie.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Fournisseure {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String nom;
    String contact;
    String adresse;

    @OneToMany(mappedBy = "fournisseur")
    List<Produit> produitList;
}
