package com.gestion.commandes.repository;

import com.gestion.commandes.entity.Paiement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Paiement - Interface pour accéder aux données des paiements
 *
 * Spring Data JPA génère automatiquement les implémentations
 * des méthodes à partir de leur nom (méthodes dérivées).
 */
@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Integer> {

    /**
     * Trouve le paiement d'une commande (OneToOne → Optional)
     */
    Optional<Paiement> findByCommandeId(Integer commandeId);

    /**
     * Récupère tous les paiements d'un statut donné
     */
    List<Paiement> findByStatut(Paiement.StatutPaiement statut);

    /**
     * Vérifie si un paiement existe pour une commande
     */
    boolean existsByCommandeId(Integer commandeId);
}
