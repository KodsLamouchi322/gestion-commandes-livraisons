package com.gestion.commandes.repository;

import com.gestion.commandes.entity.LigneBonCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository LigneBonCommande - Gère l'accès aux données des lignes de bon de commande
 */
@Repository
public interface LigneBonCommandeRepository extends JpaRepository<LigneBonCommande, Integer> {
    
    // Méthode dérivée : Trouve toutes les lignes d'un bon de commande
    List<LigneBonCommande> findByBonCommandeId(Integer bonCommandeId);

    // Méthode dérivée : Trouve toutes les lignes contenant un produit donné
    List<LigneBonCommande> findByProduitId(Integer produitId);
}
