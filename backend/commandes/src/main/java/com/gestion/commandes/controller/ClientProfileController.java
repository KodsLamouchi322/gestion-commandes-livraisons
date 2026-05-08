package com.gestion.commandes.controller;

import com.gestion.commandes.dto.ClientDTO;
import com.gestion.commandes.dto.ClientProfileUpdateRequest;
import com.gestion.commandes.entity.Client;
import com.gestion.commandes.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller Client Profile - Gestion du profil client (simplifié)
 */
@RestController
@RequestMapping("/api/clients")
public class ClientProfileController {

    @Autowired
    private ClientService clientService;

    /**
     * GET /api/clients/me - Profil de l'utilisateur (simplifié, utilise l'ID en paramètre)
     */
    @GetMapping("/me")
    public ResponseEntity<ClientDTO> getMe(@RequestParam Integer id) {
        return ResponseEntity.ok(clientService.chercherParId(id));
    }

    /**
     * PUT /api/clients/me - Mettre à jour son profil
     */
    @PutMapping("/me")
    public ResponseEntity<ClientDTO> updateMe(@RequestParam Integer id, @RequestBody ClientProfileUpdateRequest request) {
        // Créer un client avec les nouvelles données
        Client client = new Client();
        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setEmail(request.getEmail());
        client.setTelephone(request.getTelephone());
        client.setAdresse(request.getAdresse());
        client.setMotDePasse(request.getMotDePasse());
        
        return ResponseEntity.ok(clientService.update(id, client));
    }
}
