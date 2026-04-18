package com.gestion.commandes.repository;

import com.gestion.commandes.entity.Fournisseur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Fournisseur
 */
@Repository
public interface FournisseurRepository extends JpaRepository<Fournisseur, Integer> {
    List<Fournisseur> findByNomStartingWith(String nom);
    boolean existsByEmail(String email);
}
