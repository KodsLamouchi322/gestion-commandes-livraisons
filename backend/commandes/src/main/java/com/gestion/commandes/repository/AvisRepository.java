package com.gestion.commandes.repository;

import com.gestion.commandes.entity.Avis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Avis
 */
@Repository
public interface AvisRepository extends JpaRepository<Avis, Integer> {
    List<Avis> findByProduitId(Integer produitId);
    List<Avis> findByClientId(Integer clientId);
    
    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.produit.id = :produitId")
    Double findNoteMoyenneByProduitId(@Param("produitId") Integer produitId);
}
