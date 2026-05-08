package com.gestion.commandes.controller;

import com.gestion.commandes.entity.BonCommande;
import com.gestion.commandes.entity.LigneBonCommande;
import com.gestion.commandes.service.BonCommandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller BonCommande - Gère les requêtes HTTP pour les bons de commande fournisseur
 */
@RestController
@RequestMapping("/api/bons-commande")
public class BonCommandeController {

    @Autowired
    private BonCommandeService service;

    /**
     * GET /api/bons-commande - Récupère tous les bons de commande
     */
    @GetMapping
    public ResponseEntity<List<BonCommande>> getAll() {
        List<BonCommande> bons = service.chercherTout();
        return ResponseEntity.ok(bons);
    }

    /**
     * GET /api/bons-commande/{id} - Récupère un bon de commande par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BonCommande> getById(@PathVariable Integer id) {
        BonCommande bon = service.chercherParId(id);
        return ResponseEntity.ok(bon);
    }

    /**
     * GET /api/bons-commande/fournisseur/{fournisseurId} - Récupère les bons d'un fournisseur
     */
    @GetMapping("/fournisseur/{fournisseurId}")
    public ResponseEntity<List<BonCommande>> getByFournisseur(@PathVariable Integer fournisseurId) {
        List<BonCommande> bons = service.chercherParFournisseur(fournisseurId);
        return ResponseEntity.ok(bons);
    }

    /**
     * GET /api/bons-commande/statut/{statut} - Récupère les bons par statut
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<BonCommande>> getByStatut(@PathVariable BonCommande.Statut statut) {
        List<BonCommande> bons = service.chercherParStatut(statut);
        return ResponseEntity.ok(bons);
    }

    /**
     * POST /api/bons-commande - Crée un nouveau bon de commande
     */
    @PostMapping
    public ResponseEntity<BonCommande> create(@RequestBody BonCommande bc) {
        BonCommande nouveau = service.ajouter(bc);
        return ResponseEntity.ok(nouveau);
    }

    /**
     * PUT /api/bons-commande/{id} - Modifie un bon de commande
     */
    @PutMapping("/{id}")
    public ResponseEntity<BonCommande> update(@PathVariable Integer id, @RequestBody BonCommande bc) {
        BonCommande modifie = service.update(id, bc);
        return ResponseEntity.ok(modifie);
    }

    /**
     * DELETE /api/bons-commande/{id} - Supprime un bon de commande
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Bon de commande supprimé avec succès");
    }

    /**
     * POST /api/bons-commande/{id}/lignes - Ajoute une ligne à un bon de commande
     */
    @PostMapping("/{id}/lignes")
    public ResponseEntity<LigneBonCommande> addLigne(@PathVariable Integer id, @RequestBody LigneBonCommande ligne) {
        LigneBonCommande nouvelleLigne = service.ajouterLigne(id, ligne);
        return ResponseEntity.ok(nouvelleLigne);
    }

    /**
     * GET /api/bons-commande/{id}/lignes - Récupère les lignes d'un bon de commande
     */
    @GetMapping("/{id}/lignes")
    public ResponseEntity<List<LigneBonCommande>> getLignes(@PathVariable Integer id) {
        List<LigneBonCommande> lignes = service.chercherLignes(id);
        return ResponseEntity.ok(lignes);
    }

    /**
     * PUT /api/bons-commande/{id}/receptionner - Valide la réception d'un bon de commande
     * Met à jour automatiquement le stock des produits
     */
    @PutMapping("/{id}/receptionner")
    public ResponseEntity<BonCommande> receptionner(@PathVariable Integer id) {
        BonCommande bon = service.validerReception(id);
        return ResponseEntity.ok(bon);
    }
}
