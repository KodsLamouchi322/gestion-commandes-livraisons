package com.gestion.commandes.service;

import com.gestion.commandes.entity.LigneCommande;
import com.gestion.commandes.repository.LigneCommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service LigneCommande - Gère la logique métier des lignes de commande
 */
@Service
public class LigneCommandeService {

    @Autowired
    private LigneCommandeRepository ligneCommandeRepository;

    /**
     * Récupère les lignes d'une commande
     */
    public List<LigneCommande> getLignesByCommande(Integer commandeId) {
        return ligneCommandeRepository.findByCommandeId(commandeId);
    }

    /**
     * Crée une nouvelle ligne de commande
     */
    @Transactional
    public LigneCommande createLigne(LigneCommande ligne) {
        return ligneCommandeRepository.save(ligne);
    }

    /**
     * Supprime une ligne de commande
     */
    @Transactional
    public void deleteLigne(Integer id) {
        if (!ligneCommandeRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ligne de commande introuvable avec l'ID : " + id);
        }
        ligneCommandeRepository.deleteById(id);
    }
}
