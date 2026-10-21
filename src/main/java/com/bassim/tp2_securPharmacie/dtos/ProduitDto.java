package com.bassim.tp2_securPharmacie.dtos;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ProduitDto {
    Long id;
    String nom;
    String description;
    BigDecimal prix;
    String codeBarre;
    LocalDate dateExpiration;
    CategorieDto categorie;
    FournisseurDto fournisseur;
    DetailProduitDto detailProduit;
    List<TagDto> tags;
}
