package com.gestion.commandes.controller;

import com.gestion.commandes.dto.CommandeDTO;
import com.gestion.commandes.entity.Commande;
import com.gestion.commandes.service.CommandeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller Commande - Gère les requêtes HTTP pour les commandes
 *
 * Toutes les réponses sont des CommandeDTO (jamais l'entité directement)
 * pour éviter d'exposer des données sensibles ou des boucles JSON.
 */
@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    @Autowired
    private CommandeService service;

    // ============================================================
    // ROUTES DE LECTURE
    // ============================================================

    /**
     * GET /api/commandes
     * Récupère toutes les commandes
     */
    @GetMapping
    public ResponseEntity<List<CommandeDTO>> getAll() {
        return ResponseEntity.ok(service.chercherTout());
    }

    /**
     * GET /api/commandes/{id}
     * Récupère une commande par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CommandeDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.chercherParId(id));
    }

    /**
     * GET /api/commandes/client/{clientId}
     * Récupère les commandes d'un client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<CommandeDTO>> getByClient(@PathVariable Integer clientId) {
        return ResponseEntity.ok(service.chercherParClient(clientId));
    }

    /**
     * GET /api/commandes/client/{clientId}/historique
     * Historique des commandes d'un client (triées par date, la plus récente en premier)
     */
    @GetMapping("/client/{clientId}/historique")
    public ResponseEntity<List<CommandeDTO>> getHistoriqueParClient(@PathVariable Integer clientId) {
        return ResponseEntity.ok(service.historiqueParClient(clientId));
    }

    /**
     * GET /api/commandes/statut/{statut}
     * Récupère les commandes par statut
     */
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<CommandeDTO>> getByStatut(@PathVariable Commande.StatutCommande statut) {
        return ResponseEntity.ok(service.chercherParStatut(statut));
    }

    /**
     * GET /api/commandes/stats
     * Nombre de commandes par statut : { "EN_ATTENTE": 5, "VALIDEE": 3, ... }
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(service.compterParStatut());
    }

    /**
     * GET /api/commandes/chiffre-affaires
     * Chiffre d'affaires total (somme des commandes LIVREES)
     */
    @GetMapping("/chiffre-affaires")
    public ResponseEntity<Double> getChiffreAffaires() {
        return ResponseEntity.ok(service.chiffresAffaires());
    }

    // ============================================================
    // ROUTES DE CRÉATION / MODIFICATION
    // ============================================================

    /**
     * POST /api/commandes
     * Crée une nouvelle commande
     */
    @PostMapping
    public ResponseEntity<CommandeDTO> create(@Valid @RequestBody Commande c) {
        return ResponseEntity.ok(service.ajouter(c));
    }

    /**
     * PUT /api/commandes/{id}
     * Modifie une commande existante
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommandeDTO> update(@PathVariable Integer id, @Valid @RequestBody Commande c) {
        return ResponseEntity.ok(service.update(id, c));
    }

    /**
     * PUT /api/commandes/{id}/statut/{statut}
     * Change le statut d'une commande (suit les règles de transition)
     */
    @PutMapping("/{id}/statut/{statut}")
    public ResponseEntity<CommandeDTO> changerStatut(
            @PathVariable Integer id,
            @PathVariable Commande.StatutCommande statut) {
        return ResponseEntity.ok(service.changerStatut(id, statut));
    }

    // ============================================================
    // ROUTE DE SUPPRESSION
    // ============================================================

    /**
     * DELETE /api/commandes/{id}
     * Supprime une commande
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Commande supprimee avec succes");
    }
}
