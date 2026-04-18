package com.gestion.commandes.repository;

import com.gestion.commandes.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Categorie - Interface pour accéder aux données des catégories
 */
@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Integer> {

    /**
     * Méthode dérivée automatique
     * Vérifie si une catégorie avec ce nom existe déjà
     * @param nom Le nom à vérifier
     * @return true si une catégorie avec ce nom existe, false sinon
     */
    boolean existsByNom(String nom);

    /**
     * Méthode dérivée automatique
     * Trouve les catégories dont le nom commence par une chaîne donnée
     * @param nom Le début du nom à rechercher
     * @return Liste des catégories correspondantes
     */
    List<Categorie> findByNomStartingWith(String nom);
}
