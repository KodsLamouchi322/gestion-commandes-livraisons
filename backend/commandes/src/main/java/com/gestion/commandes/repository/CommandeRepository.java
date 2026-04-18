package com.gestion.commandes.repository;

import com.gestion.commandes.entity.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Commande - Interface pour accéder aux données des commandes
 */
@Repository
public interface CommandeRepository extends JpaRepository<Commande, Integer> {

    /**
     * Méthode dérivée automatique
     * Trouve toutes les commandes d'un client
     * @param clientId L'ID du client
     * @return Liste des commandes du client
     */
    List<Commande> findByClientId(Integer clientId);

    /**
     * Méthode dérivée automatique
     * Trouve les commandes par statut
     * @param statut Le statut à rechercher
     * @return Liste des commandes avec ce statut
     */
    List<Commande> findByStatut(Commande.StatutCommande statut);

    /**
     * Requête HQL personnalisée
     * Trouve les commandes d'un client par statut
     * @param clientId L'ID du client
     * @param statut Le statut à rechercher
     * @return Liste des commandes correspondantes
     */
    @Query("from Commande c where c.client.id = :clientId and c.statut = :statut")
    List<Commande> findByClientIdAndStatut(@Param("clientId") Integer clientId, 
                                            @Param("statut") Commande.StatutCommande statut);

    long countByClientId(Integer clientId);
}
