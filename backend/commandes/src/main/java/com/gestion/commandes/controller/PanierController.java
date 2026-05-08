package com.gestion.commandes.controller;

import com.gestion.commandes.entity.Panier;
import com.gestion.commandes.service.PanierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Panier - Gère les requêtes HTTP pour le panier
 */
@RestController
@RequestMapping("/api/panier")
public class PanierController {

    @Autowired
    private PanierService service;

    /**
     * GET /api/panier/client/{clientId} - Récupère le panier d'un client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<Panier> getPanierClient(@PathVariable Integer clientId) {
        return ResponseEntity.ok(service.getPanierClient(clientId));
    }

    /**
     * POST /api/panier/client/{clientId}/produit/{produitId} - Ajoute un produit au panier
     */
    @PostMapping("/client/{clientId}/produit/{produitId}")
    public ResponseEntity<Panier> ajouterProduit(@PathVariable Integer clientId,
                                                  @PathVariable Integer produitId,
                                                  @RequestParam(defaultValue = "1") Integer quantite) {
        return ResponseEntity.ok(service.ajouterProduit(clientId, produitId, quantite));
    }

    /**
     * DELETE /api/panier/client/{clientId}/produit/{produitId} - Supprime un produit du panier
     */
    @DeleteMapping("/client/{clientId}/produit/{produitId}")
    public ResponseEntity<Panier> supprimerProduit(@PathVariable Integer clientId,
                                                   @PathVariable Integer produitId) {
        return ResponseEntity.ok(service.supprimerProduit(clientId, produitId));
    }

    /**
     * DELETE /api/panier/client/{clientId} - Vide le panier d'un client
     */
    @DeleteMapping("/client/{clientId}")
    public ResponseEntity<String> viderPanier(@PathVariable Integer clientId) {
        service.viderPanier(clientId);
        return ResponseEntity.ok("Panier vidé avec succès");
    }
}
