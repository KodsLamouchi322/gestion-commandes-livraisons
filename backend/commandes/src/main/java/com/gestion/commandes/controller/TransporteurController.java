package com.gestion.commandes.controller;

import com.gestion.commandes.dto.TransporteurDTO;
import com.gestion.commandes.entity.Transporteur;
import com.gestion.commandes.service.TransporteurService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Transporteur - Gère les requêtes HTTP pour les transporteurs
 *
 * Toutes les réponses sont des TransporteurDTO.
 */
@RestController
@RequestMapping("/api/transporteurs")
public class TransporteurController {

    @Autowired
    private TransporteurService service;

    /**
     * GET /api/transporteurs
     * Récupère tous les transporteurs avec leur nombre de livraisons
     */
    @GetMapping
    public ResponseEntity<List<TransporteurDTO>> getAll() {
        return ResponseEntity.ok(service.chercherTout());
    }

    /**
     * GET /api/transporteurs/{id}
     * Récupère un transporteur par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransporteurDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.chercherParId(id));
    }

    /**
     * GET /api/transporteurs/search?nom=...
     * Recherche des transporteurs par nom
     */
    @GetMapping("/search")
    public ResponseEntity<List<TransporteurDTO>> searchByNom(@RequestParam String nom) {
        return ResponseEntity.ok(service.chercherParNom(nom));
    }

    /**
     * POST /api/transporteurs
     * Crée un nouveau transporteur
     */
    @PostMapping
    public ResponseEntity<TransporteurDTO> create(@Valid @RequestBody Transporteur t) {
        return ResponseEntity.ok(service.ajouter(t));
    }

    /**
     * PUT /api/transporteurs/{id}
     * Modifie un transporteur existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransporteurDTO> update(@PathVariable Integer id, @Valid @RequestBody Transporteur t) {
        return ResponseEntity.ok(service.update(id, t));
    }

    /**
     * DELETE /api/transporteurs/{id}
     * Supprime un transporteur (interdit s'il a des livraisons)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Transporteur supprime avec succes");
    }
}
