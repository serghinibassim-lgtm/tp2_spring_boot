package com.bassim.tp2_securPharmacie.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FournisseurDto {
    Long id;
    String nom;
    String contact;
    String adresse;
}
