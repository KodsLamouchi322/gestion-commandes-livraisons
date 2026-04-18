package com.gestion.commandes.repository;

import com.gestion.commandes.entity.Panier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Panier
 */
@Repository
public interface PanierRepository extends JpaRepository<Panier, Integer> {
    Optional<Panier> findByClientId(Integer clientId);
}
