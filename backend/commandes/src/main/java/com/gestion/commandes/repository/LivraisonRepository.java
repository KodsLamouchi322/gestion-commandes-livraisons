package com.gestion.commandes.repository;

import com.gestion.commandes.entity.Livraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Livraison
 */
@Repository
public interface LivraisonRepository extends JpaRepository<Livraison, Integer> {
    Optional<Livraison> findByCommandeId(Integer commandeId);
    List<Livraison> findByTransporteurId(Integer transporteurId);
    boolean existsByCommandeId(Integer commandeId);
    long countByTransporteurId(Integer transporteurId);
}
