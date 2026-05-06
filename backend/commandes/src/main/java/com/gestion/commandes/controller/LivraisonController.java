package com.gestion.commandes.controller;

import com.gestion.commandes.dto.LivraisonDTO;
import com.gestion.commandes.entity.Livraison;
import com.gestion.commandes.service.LivraisonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Livraison - Gère les requêtes HTTP pour les livraisons
 *
 * Toutes les réponses sont des LivraisonDTO.
 */
@RestController
@RequestMapping("/api/livraisons")
public class LivraisonController {

    @Autowired
    private LivraisonService service;

    /**
     * GET /api/livraisons
     * Récupère toutes les livraisons
     */
    @GetMapping
    public ResponseEntity<List<LivraisonDTO>> getAll() {
        return ResponseEntity.ok(service.chercherTout());
    }

    /**
     * GET /api/livraisons/{id}
     * Récupère une livraison par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<LivraisonDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.chercherParId(id));
    }

    /**
     * GET /api/livraisons/commande/{commandeId}
     * Récupère la livraison d'une commande
     */
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<LivraisonDTO> getByCommande(@PathVariable Integer commandeId) {
        return ResponseEntity.ok(service.chercherParCommande(commandeId));
    }

    /**
     * GET /api/livraisons/transporteur/{transporteurId}
     * Récupère les livraisons d'un transporteur
     */
    @GetMapping("/transporteur/{transporteurId}")
    public ResponseEntity<List<LivraisonDTO>> getByTransporteur(@PathVariable Integer transporteurId) {
        return ResponseEntity.ok(service.chercherParTransporteur(transporteurId));
    }

    /**
     * POST /api/livraisons
     * Crée une nouvelle livraison
     */
    @PostMapping
    public ResponseEntity<LivraisonDTO> create(@Valid @RequestBody Livraison l) {
        return ResponseEntity.ok(service.ajouter(l));
    }

    /**
     * PUT /api/livraisons/{id}/statut/{statut}
     * Change le statut d'une livraison
     * EN_PREPARATION → EN_TRANSIT → LIVREE (+ mise à jour stock automatique)
     */
    @PutMapping("/{id}/statut/{statut}")
    public ResponseEntity<LivraisonDTO> changerStatut(
            @PathVariable Integer id,
            @PathVariable Livraison.StatutLivraison statut) {
        return ResponseEntity.ok(service.changerStatut(id, statut));
    }

    /**
     * DELETE /api/livraisons/{id}
     * Supprime une livraison
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Livraison supprimee avec succes");
    }
}
