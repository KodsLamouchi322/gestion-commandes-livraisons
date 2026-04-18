package com.gestion.commandes.repository;

import com.gestion.commandes.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Produit - Interface pour accéder aux données des produits
 * Extends JpaRepository<Produit, Integer> :
 * - Produit = type de l'entité
 * - Integer = type de l'ID
 * Spring Data JPA génère automatiquement l'implémentation
 */
@Repository
public interface ProduitRepository extends JpaRepository<Produit, Integer> {

    /**
     * Méthode dérivée automatique (Spring Data naming convention)
     * Trouve les produits dont le nom commence par une chaîne donnée
     * @param nom Le début du nom à rechercher
     * @return Liste des produits correspondants
     */
    List<Produit> findByNomStartingWith(String nom);

    /**
     * Méthode dérivée automatique
     * Trouve les produits d'une catégorie donnée
     * @param categorieId L'ID de la catégorie
     * @return Liste des produits de cette catégorie
     */
    List<Produit> findByCategorieId(Integer categorieId);

    /**
     * Requête HQL personnalisée avec @Query
     * Trouve les produits dont le prix est inférieur à un montant donné
     * @param prix Le prix maximum
     * @return Liste des produits moins chers que le prix donné
     */
    @Query("from Produit p where p.prixUnitaire < :prix")
    List<Produit> findByPrixInferieur(@Param("prix") Double prix);

    /**
     * Requête HQL personnalisée
     * Trouve les produits en rupture de stock (quantité = 0)
     * @return Liste des produits en rupture
     */
    @Query("from Produit p where p.quantiteEnStock = 0")
    List<Produit> findProduitsEnRupture();

    long countByCategorieId(Integer categorieId);
}
