package com.gestion.commandes.controller;

import com.gestion.commandes.dto.PaiementDTO;
import com.gestion.commandes.entity.Paiement;
import com.gestion.commandes.service.PaiementService;
import com.gestion.commandes.service.StripeService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Controller Paiement - Gère les requêtes HTTP pour les paiements
 *
 * Toutes les réponses sont des PaiementDTO.
 */
@RestController
@RequestMapping("/api/paiements")
public class PaiementController {

    @Autowired
    private PaiementService service;

    @Autowired
    private StripeService stripeService;

    /**
     * GET /api/paiements
     * Récupère tous les paiements
     */
    @GetMapping
    public ResponseEntity<List<PaiementDTO>> getAll() {
        return ResponseEntity.ok(service.chercherTout());
    }

    /**
     * GET /api/paiements/{id}
     * Récupère un paiement par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaiementDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.chercherParId(id));
    }

    /**
     * GET /api/paiements/commande/{commandeId}
     * Récupère le paiement d'une commande
     */
    @GetMapping("/commande/{commandeId}")
    public ResponseEntity<PaiementDTO> getByCommande(@PathVariable Integer commandeId) {
        return ResponseEntity.ok(service.chercherParCommande(commandeId));
    }

    /**
     * POST /api/paiements
     * Crée un nouveau paiement
     * Le montant est automatiquement aligné sur le montant de la commande
     */
    @PostMapping
    public ResponseEntity<PaiementDTO> create(@Valid @RequestBody Paiement p) {
        return ResponseEntity.ok(service.ajouter(p));
    }

    /**
     * PUT /api/paiements/{id}/statut/{statut}
     * Change le statut d'un paiement
     * Si VALIDE → la commande passe automatiquement à VALIDEE
     */
    @PutMapping("/{id}/statut/{statut}")
    public ResponseEntity<PaiementDTO> changerStatut(
            @PathVariable Integer id,
            @PathVariable Paiement.StatutPaiement statut) {
        return ResponseEntity.ok(service.changerStatut(id, statut));
    }

    /**
     * POST /api/paiements/{id}/confirmer
     * Confirms a payment (validates method and delivery status)
     * Convenience endpoint that calls changerStatut(id, VALIDE)
     */
    @PostMapping("/{id}/confirmer")
    public ResponseEntity<PaiementDTO> confirmerPaiement(@PathVariable Integer id) {
        return ResponseEntity.ok(service.confirmerPaiement(id));
    }

    /**
     * DELETE /api/paiements/{id}
     * Supprime un paiement
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Paiement supprime avec succes");
    }

    // ============================================================
    // STRIPE CHECKOUT
    // ============================================================

    /**
     * POST /api/paiements/stripe/create-checkout-session
     * Crée une session de paiement Stripe Checkout
     */
    @PostMapping("/stripe/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody Map<String, Integer> request) {
        try {
            Integer commandeId = request.get("commandeId");
            if (commandeId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "commandeId est requis"));
            }
            String checkoutUrl = stripeService.createCheckoutSession(commandeId);
            return ResponseEntity.ok(Map.of("url", checkoutUrl));
        } catch (ResponseStatusException e) {
            HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
            if (status == null) status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("error", e.getReason() != null ? e.getReason() : "Erreur Stripe"));
        } catch (StripeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Erreur Stripe";
            // Cas fréquent: clé manquante → message StripeException "No API key provided"
            if (msg.toLowerCase().contains("no api key")) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Stripe n'est pas configuré. Configurez STRIPE_SECRET_KEY (sk_test_...)"));
            }
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }
    }

    /**
     * GET /api/paiements/stripe/verify-session
     * Vérifie une session Stripe et crée le paiement
     */
    @GetMapping("/stripe/verify-session")
    public ResponseEntity<Paiement> verifySession(@RequestParam("session_id") String sessionId) {
        try {
            Paiement paiement = stripeService.verifySession(sessionId);
            return ResponseEntity.ok(paiement);
        } catch (StripeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

