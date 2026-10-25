package com.bassim.tp2_securPharmacie.repositories;

import com.bassim.tp2_securPharmacie.models.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    long countByStockGreaterThan(Integer stock);

    long countByStockBetween(Integer min, Integer max);

    long countByDateExpirationBefore(LocalDate date);
}
