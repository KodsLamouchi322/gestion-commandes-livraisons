package com.gestion.commandes.controller;

import com.gestion.commandes.entity.LigneCommande;
import com.gestion.commandes.service.LigneCommandeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller LigneCommande - Gestion des lignes de commande
 */
@RestController
@RequestMapping("/api/lignes-commande")
public class LigneCommandeController {

    @Autowired
    private LigneCommandeService ligneCommandeService;

    /**
     * GET /api/lignes-commande/commande/{commandeId} - Lignes d'une commande
     */
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<List<LigneCommande>> getLignesByCommande(@PathVariable Integer commandeId) {
        return ResponseEntity.ok(ligneCommandeService.getLignesByCommande(commandeId));
    }

    /**
     * POST /api/lignes-commande - Créer une ligne de commande
     */
    @PostMapping
    public ResponseEntity<LigneCommande> createLigne(@RequestBody LigneCommande ligne) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ligneCommandeService.createLigne(ligne));
    }

    /**
     * DELETE /api/lignes-commande/{id} - Supprimer une ligne
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLigne(@PathVariable Integer id) {
        ligneCommandeService.deleteLigne(id);
        return ResponseEntity.noContent().build();
    }
}
