package com.gestion.commandes.controller;

import com.gestion.commandes.dto.LivraisonDTO;
import com.gestion.commandes.entity.Livraison;
import com.gestion.commandes.service.LivraisonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * Retourne 204 No Content si aucune livraison n'existe pour cette commande
     */
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<LivraisonDTO> getByCommande(@PathVariable Integer commandeId) {
        LivraisonDTO livraison = service.chercherParCommande(commandeId);
        if (livraison == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(livraison);
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
     * EN_PREPARATION → EXPEDIEE → LIVREE (+ mise à jour stock automatique)
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

    /**
     * POST /api/livraisons/depuis-commande
     * Crée une livraison depuis une commande (EN_ATTENTE ou VALIDEE).
     *
     * Request body: { "commandeId": Integer, "cout": Double, "transporteurId": Integer (optional) }
     *
     * Validations:
     * - La commande doit exister ; statut EN_ATTENTE ou VALIDEE (pas annulée / expédiée / livrée)
     * - Aucune livraison ne doit déjà exister pour cette commande
     * - Le transporteur doit exister (si fourni)
     *
     * @param request Map contenant commandeId, cout (optionnel), et transporteurId (optionnel)
     * @return LivraisonDTO de la livraison créée
     */
    @PostMapping("/depuis-commande")
    public ResponseEntity<LivraisonDTO> creerDepuisCommande(@RequestBody Map<String, Object> request) {
        Integer commandeId = (Integer) request.get("commandeId");
        Double cout = request.containsKey("cout") ? ((Number) request.get("cout")).doubleValue() : null;
        Integer transporteurId = request.containsKey("transporteurId") ? (Integer) request.get("transporteurId") : null;
        
        LivraisonDTO livraison = service.creerDepuisCommande(commandeId, cout, transporteurId);
        return ResponseEntity.ok(livraison);
    }

    /**
     * PUT /api/livraisons/{id}/transporteur/{transporteurId}
     * Assigne un transporteur à une livraison
     * 
     * Validations:
     * - La livraison doit exister
     * - La livraison doit avoir le statut EN_PREPARATION
     * - Le transporteur doit exister
     * 
     * @param id ID de la livraison
     * @param transporteurId ID du transporteur à assigner
     * @return LivraisonDTO de la livraison mise à jour
     */
    @PutMapping("/{id}/transporteur/{transporteurId}")
    public ResponseEntity<LivraisonDTO> assignerTransporteur(
            @PathVariable Integer id,
            @PathVariable Integer transporteurId) {
        LivraisonDTO livraison = service.assignerTransporteur(id, transporteurId);
        return ResponseEntity.ok(livraison);
    }

    /**
     * PUT /api/livraisons/{id}/expedier
     * Marque une livraison comme expédiée
     * 
     * Validations:
     * - La livraison doit exister
     * - La livraison doit avoir le statut EN_PREPARATION
     * - La commande associée ne doit pas être annulée
     * 
     * Comportement:
     * - Change le statut de la livraison à EXPEDIEE
     * - Met à jour le statut de la commande associée à EXPEDIEE
     * - Met à jour le timestamp de la livraison
     * 
     * @param id ID de la livraison
     * @return LivraisonDTO de la livraison mise à jour
     */
    @PutMapping("/{id}/expedier")
    public ResponseEntity<LivraisonDTO> expedier(@PathVariable Integer id) {
        LivraisonDTO livraison = service.changerStatut(id, Livraison.StatutLivraison.EXPEDIEE);
        return ResponseEntity.ok(livraison);
    }

    /**
     * PUT /api/livraisons/{id}/livrer
     * Marque une livraison comme livrée
     * 
     * Validations:
     * - La livraison doit exister
     * - La livraison doit avoir le statut EXPEDIEE
     * - La commande associée ne doit pas être annulée
     * 
     * Comportement:
     * - Change le statut de la livraison à LIVREE
     * - Met à jour le statut de la commande associée à LIVREE
     * - Met à jour le timestamp de la livraison
     * - Met à jour automatiquement les stocks des produits de la commande
     * 
     * @param id ID de la livraison
     * @return LivraisonDTO de la livraison mise à jour
     */
    @PutMapping("/{id}/livrer")
    public ResponseEntity<LivraisonDTO> livrer(@PathVariable Integer id) {
        LivraisonDTO livraison = service.changerStatut(id, Livraison.StatutLivraison.LIVREE);
        return ResponseEntity.ok(livraison);
    }
}
