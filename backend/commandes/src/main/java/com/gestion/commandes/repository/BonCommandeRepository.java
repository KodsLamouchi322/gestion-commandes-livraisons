package com.gestion.commandes.repository;

import com.gestion.commandes.entity.BonCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository BonCommande
 */
@Repository
public interface BonCommandeRepository extends JpaRepository<BonCommande, Integer> {
    List<BonCommande> findByFournisseurId(Integer fournisseurId);
    List<BonCommande> findByStatut(BonCommande.Statut statut);
    long countByFournisseurId(Integer fournisseurId);
}
