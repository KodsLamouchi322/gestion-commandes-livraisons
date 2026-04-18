package com.gestion.commandes.repository;

import com.gestion.commandes.entity.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
