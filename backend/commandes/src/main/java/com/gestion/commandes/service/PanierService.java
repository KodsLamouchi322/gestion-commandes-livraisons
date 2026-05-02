package com.gestion.commandes.service;

import com.gestion.commandes.entity.*;
import com.gestion.commandes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service Panier - Gère la logique métier du panier
 */
@Service
public class PanierService {

    @Autowired
    private PanierRepository rep;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private LignePanierRepository lignePanierRepository;

    /**
     * Récupère le panier d'un client (ou le crée s'il n'existe pas)
     */
    public Panier getPanierClient(Integer clientId) {
        return rep.findByClientId(clientId)
                .orElseGet(() -> {
                    Client client = clientRepository.findById(clientId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "Client introuvable"));
                    Panier panier = new Panier();
                    panier.setClient(client);
                    panier.setDateCreation(LocalDateTime.now());
                    return rep.save(panier);
                });
    }

    /**
     * Ajoute un produit au panier
     */
    @Transactional
    public Panier ajouterProduit(Integer clientId, Integer produitId, Integer quantite) {
        if (quantite == null || quantite <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La quantite doit etre superieure a 0");
        }
        Panier panier = getPanierClient(clientId);
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produit introuvable"));

        // Vérifier si le produit est déjà dans le panier
        LignePanier ligneExistante = panier.getLignes().stream()
                .filter(l -> l.getProduit().getId().equals(produitId))
                .findFirst()
                .orElse(null);

        if (ligneExistante != null) {
            int nouvelleQuantite = ligneExistante.getQuantite() + quantite;
            if (nouvelleQuantite > produit.getQuantiteEnStock()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stock insuffisant pour ce produit");
            }
            // Augmenter la quantité
            ligneExistante.setQuantite(nouvelleQuantite);
            lignePanierRepository.save(ligneExistante);
        } else {
            if (quantite > produit.getQuantiteEnStock()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stock insuffisant pour ce produit");
            }
            // Créer une nouvelle ligne
            LignePanier ligne = new LignePanier();
            ligne.setPanier(panier);
            ligne.setProduit(produit);
            ligne.setQuantite(quantite);
            ligne.setPrixUnitaire(produit.getPrixUnitaire());
            panier.getLignes().add(ligne);
        }

        return rep.save(panier);
    }

    /**
     * Supprime un produit du panier
     */
    @Transactional
    public Panier supprimerProduit(Integer clientId, Integer produitId) {
        Panier panier = getPanierClient(clientId);
        boolean removed = panier.getLignes().removeIf(l -> l.getProduit().getId().equals(produitId));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit absent du panier");
        }
        return rep.save(panier);
    }

    /**
     * Vide le panier d'un client
     */
    @Transactional
    public void viderPanier(Integer clientId) {
        Panier panier = getPanierClient(clientId);
        panier.getLignes().clear();
        rep.save(panier);
    }

    /**
     * Supprime le panier
     */
    @Transactional
    public void delete(Integer id) {
        rep.deleteById(id);
    }
}
