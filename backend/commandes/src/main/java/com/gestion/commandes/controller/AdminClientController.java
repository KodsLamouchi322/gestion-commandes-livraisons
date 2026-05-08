package com.gestion.commandes.controller;

import com.gestion.commandes.dto.ClientDTO;
import com.gestion.commandes.entity.Client;
import com.gestion.commandes.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Admin Client - Gestion des clients par l'administrateur
 */
@RestController
@RequestMapping("/api/admin/clients")
public class AdminClientController {

    @Autowired
    private ClientService clientService;

    /**
     * GET /api/admin/clients - Liste tous les clients
     */
    @GetMapping
    public ResponseEntity<List<ClientDTO>> getAll() {
        return ResponseEntity.ok(clientService.chercherTout());
    }

    /**
     * GET /api/admin/clients/{id} - Détail d'un client
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(clientService.chercherParId(id));
    }

    /**
     * POST /api/admin/clients - Créer un client
     */
    @PostMapping
    public ResponseEntity<ClientDTO> create(@RequestBody Client client) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.ajouter(client));
    }

    /**
     * PUT /api/admin/clients/{id} - Modifier un client
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO> update(@PathVariable Integer id, @RequestBody Client client) {
        return ResponseEntity.ok(clientService.update(id, client));
    }

    /**
     * DELETE /api/admin/clients/{id} - Supprimer un client
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
