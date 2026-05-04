package com.gestion.commandes.controller;

import com.gestion.commandes.dto.ClientDTO;
import com.gestion.commandes.entity.Client;
import com.gestion.commandes.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Client - Gère les requêtes HTTP pour les clients
 *
 * Toutes les réponses sont des ClientDTO : le mot de passe
 * n'est JAMAIS envoyé au frontend.
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService service;

    /**
     * GET /api/clients
     * Récupère tous les clients (sans mot de passe)
     */
    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAll() {
        return ResponseEntity.ok(service.chercherTout());
    }

    /**
     * GET /api/clients/{id}
     * Récupère un client par son ID (sans mot de passe)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.chercherParId(id));
    }

    /**
     * GET /api/clients/email/{email}
     * Récupère un client par son email (sans mot de passe)
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ClientDTO> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(service.chercherParEmail(email));
    }

    /**
     * GET /api/clients/search?nom=...
     * Recherche des clients par nom
     */
    @GetMapping("/search")
    public ResponseEntity<List<ClientDTO>> searchByNom(@RequestParam String nom) {
        return ResponseEntity.ok(service.chercherParNom(nom));
    }

    /**
     * POST /api/clients
     * Crée un nouveau client
     * Le mot de passe est hashé automatiquement (BCrypt)
     */
    @PostMapping
    public ResponseEntity<ClientDTO> create(@Valid @RequestBody Client c) {
        return ResponseEntity.ok(service.ajouter(c));
    }

    /**
     * PUT /api/clients/{id}
     * Modifie un client existant
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> update(@PathVariable Integer id, @Valid @RequestBody Client c) {
        return ResponseEntity.ok(service.update(id, c));
    }

    /**
     * DELETE /api/clients/{id}
     * Supprime un client (interdit si admin ou si commandes existantes)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Client supprime avec succes");
    }
}
