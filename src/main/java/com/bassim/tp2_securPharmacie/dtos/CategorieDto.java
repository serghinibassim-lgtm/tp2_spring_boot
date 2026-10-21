package com.bassim.tp2_securPharmacie.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorieDto {
    Long id;
    String nom;
    String description;
}
