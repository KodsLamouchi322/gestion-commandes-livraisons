package com.gestion.commandes.repository;

import com.gestion.commandes.entity.Transporteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Transporteur
 */
@Repository
public interface TransporteurRepository extends JpaRepository<Transporteur, Integer> {
    List<Transporteur> findByNomStartingWith(String nom);
}
