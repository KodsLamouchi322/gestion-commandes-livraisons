package com.gestion.commandes.service;

import com.gestion.commandes.entity.LigneBonCommande;
import com.gestion.commandes.entity.LigneCommande;
import com.gestion.commandes.entity.Produit;
import com.gestion.commandes.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service Stock - Gère la logique métier de gestion du stock
 * Centralise toutes les opérations de stock pour garantir la cohérence
 */
@SuppressWarnings("null")
@Service
public class StockService {

    @Autowired
    private ProduitRepository produitRepository;

    /**
     * Réserve le stock pour une liste de lignes de commande
     * Valide que tous les produits ont un stock suffisant avant de diminuer les quantités
     * Si un produit a un stock insuffisant, lance une exception et annule toute l'opération
     * 
     * @param lignes Liste des lignes de commande pour lesquelles réserver le stock
     * @throws ResponseStatusException si un produit a un stock insuffisant
     */
    @Transactional
    public void reserverStock(List<LigneCommande> lignes) {
        // 1. Valider que tous les produits ont un stock suffisant AVANT de modifier quoi que ce soit
        for (LigneCommande ligne : lignes) {
            Produit produit = produitRepository.findById(ligne.getProduit().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Produit introuvable avec l'ID : " + ligne.getProduit().getId()));
            
            if (produit.getQuantiteEnStock() < ligne.getQuantite()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stock insuffisant pour le produit : " + produit.getNom() + 
                        " (demandé : " + ligne.getQuantite() + ", disponible : " + produit.getQuantiteEnStock() + ")");
            }
        }

        // 2. Si tous les produits ont un stock suffisant, diminuer les quantités
        for (LigneCommande ligne : lignes) {
            Produit produit = produitRepository.findById(ligne.getProduit().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Produit introuvable avec l'ID : " + ligne.getProduit().getId()));
            
            produit.setQuantiteEnStock(produit.getQuantiteEnStock() - ligne.getQuantite());
            produitRepository.save(produit);
        }
    }

    /**
     * Libère le stock pour une liste de lignes de commande
     * Augmente la quantité en stock pour chaque produit
     * Utilisé lors de l'annulation d'une commande
     * 
     * @param lignes Liste des lignes de commande pour lesquelles libérer le stock
     */
    @Transactional
    public void libererStock(List<LigneCommande> lignes) {
        for (LigneCommande ligne : lignes) {
            Produit produit = produitRepository.findById(ligne.getProduit().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Produit introuvable avec l'ID : " + ligne.getProduit().getId()));
            
            produit.setQuantiteEnStock(produit.getQuantiteEnStock() + ligne.getQuantite());
            produitRepository.save(produit);
        }
    }

    /**
     * Réapprovisionne le stock à partir d'un bon de commande reçu
     * Augmente la quantité en stock pour chaque produit du bon de commande
     * 
     * @param lignes Liste des lignes du bon de commande reçu
     */
    @Transactional
    public void reapprovisionner(List<LigneBonCommande> lignes) {
        for (LigneBonCommande ligne : lignes) {
            Produit produit = produitRepository.findById(ligne.getProduit().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Produit introuvable avec l'ID : " + ligne.getProduit().getId()));
            
            produit.setQuantiteEnStock(produit.getQuantiteEnStock() + ligne.getQuantite());
            produitRepository.save(produit);
        }
    }

    /**
     * Retourne la liste des produits avec un stock faible (< 10 unités)
     * 
     * @return Liste des produits avec quantiteEnStock < 10
     */
    public List<Produit> produitsStockFaible() {
        return produitRepository.findAll().stream()
                .filter(p -> p.getQuantiteEnStock() < 10)
                .toList();
    }

    /**
     * Vérifie si un produit a un stock suffisant pour une quantité donnée
     * 
     * @param produitId L'ID du produit à vérifier
     * @param quantite La quantité demandée
     * @return true si le stock est suffisant, false sinon
     */
    public boolean estStockSuffisant(Integer produitId, Integer quantite) {
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produit introuvable avec l'ID : " + produitId));
        
        return produit.getQuantiteEnStock() >= quantite;
    }
}
