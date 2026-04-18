package com.gestion.commandes.repository;

import com.gestion.commandes.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Client - Interface pour accéder aux données des clients
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {

    /**
     * Méthode dérivée automatique
     * Trouve un client par son email
     * @param email L'email du client
     * @return Optional contenant le client si trouvé
     */
    Optional<Client> findByEmail(String email);

    /**
     * Méthode dérivée automatique
     * Vérifie si un client avec cet email existe déjà
     * @param email L'email à vérifier
     * @return true si un client avec cet email existe, false sinon
     */
    boolean existsByEmail(String email);

    /**
     * Méthode dérivée automatique
     * Trouve les clients dont le nom commence par une chaîne donnée
     * @param nom Le début du nom à rechercher
     * @return Liste des clients correspondants
     */
    List<Client> findByNomStartingWith(String nom);

    /**
     * Requête HQL personnalisée
     * Trouve les clients par rôle (CLIENT ou ADMIN)
     * @param role Le rôle à rechercher
     * @return Liste des clients avec ce rôle
     */
    @Query("from Client c where c.role = :role")
    List<Client> findByRole(@Param("role") String role);
}
