package com.gestion.commandes.repository;

import com.gestion.commandes.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository LigneCommande - Gère l'accès aux données des lignes de commande
 */
@Repository
public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Integer> {

    // Méthode dérivée : Trouve toutes les lignes d'une commande
    List<LigneCommande> findByCommandeId(Integer commandeId);

    // Méthode dérivée : Trouve toutes les lignes contenant un produit donné
    List<LigneCommande> findByProduitId(Integer produitId);

    /**
     * Le client a commandé ce produit dans une commande au moins validée (payée / expédiée / livrée).
     */
    @Query("SELECT CASE WHEN COUNT(lc) > 0 THEN true ELSE false END FROM LigneCommande lc "
            + "WHERE lc.produit.id = :produitId AND lc.commande.client.id = :clientId "
            + "AND lc.commande.statut IN ('VALIDEE', 'EXPEDIEE', 'LIVREE')")
    boolean existsCommandeValideeAvecProduit(
            @Param("clientId") Integer clientId,
            @Param("produitId") Integer produitId);
}
