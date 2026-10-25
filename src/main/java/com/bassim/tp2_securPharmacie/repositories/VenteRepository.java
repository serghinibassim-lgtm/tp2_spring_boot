package com.bassim.tp2_securPharmacie.repositories;

import com.bassim.tp2_securPharmacie.models.Vente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VenteRepository extends JpaRepository<Vente, Long> {

    long countByDateVenteAfter(LocalDateTime date);

    List<Vente> findTop5ByOrderByDateVenteDesc();

    boolean existsByProduitId(Long produitId);
}
