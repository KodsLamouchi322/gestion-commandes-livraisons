package com.gestion.commandes.repository;

import com.gestion.commandes.entity.LignePanier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LignePanierRepository extends JpaRepository<LignePanier, Long> {
    Optional<LignePanier> findByPanierIdAndProduitId(Long panierId, Long produitId);
    java.util.List<LignePanier> findByProduitId(Long produitId);
}
