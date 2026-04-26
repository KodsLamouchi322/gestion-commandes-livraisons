package com.gestion.commandes.service;

import com.gestion.commandes.converter.EntityConverter;
import com.gestion.commandes.dto.LivraisonDTO;
import com.gestion.commandes.entity.Commande;
import com.gestion.commandes.entity.LigneCommande;
import com.gestion.commandes.entity.Livraison;
import com.gestion.commandes.entity.Produit;
import com.gestion.commandes.entity.Transporteur;
import com.gestion.commandes.repository.CommandeRepository;
import com.gestion.commandes.repository.LigneCommandeRepository;
import com.gestion.commandes.repository.LivraisonRepository;
import com.gestion.commandes.repository.ProduitRepository;
import com.gestion.commandes.repository.TransporteurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Livraison - Gère la logique métier des livraisons
 *
 * Fonctionnalité clé : quand une livraison passe au statut LIVREE,
 * le stock de chaque produit de la commande est automatiquement mis à jour.
 */
@Service
public class LivraisonService {

    @Autowired
    private LivraisonRepository rep;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private TransporteurRepository transporteurRepository;

    // Injectés pour la mise à jour automatique du stock
    @Autowired
    private LigneCommandeRepository ligneCommandeRepository;

    @Autowired
    private ProduitRepository produitRepository;

    // Converter : entité → DTO
    @Autowired
    private EntityConverter converter;

    // ============================================================
    // LECTURE
    // ============================================================

    /**
     * Récupère toutes les livraisons sous forme de DTOs
     */
    public List<LivraisonDTO> chercherTout() {
        return rep.findAll()
                  .stream()
                  .map(converter::toLivraisonDTO)
                  .collect(Collectors.toList());
    }

    /**
     * Récupère une livraison par son ID (retourne un DTO)
     */
    public LivraisonDTO chercherParId(Integer id) {
        Livraison livraison = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Livraison introuvable"));
        return converter.toLivraisonDTO(livraison);
    }

    /**
     * Récupère la livraison d'une commande (retourne un DTO)
     */
    public LivraisonDTO chercherParCommande(Integer commandeId) {
        Livraison livraison = rep.findByCommandeId(commandeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Livraison introuvable pour cette commande"));
        return converter.toLivraisonDTO(livraison);
    }

    /**
     * Récupère les livraisons d'un transporteur (retourne des DTOs)
     */
    public List<LivraisonDTO> chercherParTransporteur(Integer transporteurId) {
        return rep.findByTransporteurId(transporteurId)
                  .stream()
                  .map(converter::toLivraisonDTO)
                  .collect(Collectors.toList());
    }

    // ============================================================
    // CRÉATION
    // ============================================================

    /**
     * Crée une nouvelle livraison - retourne un DTO
     */
    @Transactional
    public LivraisonDTO ajouter(Livraison l) {
        if (l.getCommande() == null || l.getCommande().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Commande obligatoire");
        }
        if (rep.existsByCommandeId(l.getCommande().getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une livraison existe deja pour cette commande");
        }
        if (l.getAdresse() == null || l.getAdresse().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adresse de livraison obligatoire");
        }
        if (l.getCout() == null || l.getCout() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cout de livraison invalide");
        }

        // Vérifier que la commande existe
        Commande commande = commandeRepository.findById(l.getCommande().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Commande introuvable"));
        l.setCommande(commande);

        // Vérifier que le transporteur existe (s'il est fourni)
        if (l.getTransporteur() != null && l.getTransporteur().getId() != null) {
            Transporteur transporteur = transporteurRepository.findById(l.getTransporteur().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Transporteur introuvable"));
            l.setTransporteur(transporteur);
        }

        // Date par défaut : dans 3 jours
        if (l.getDateLivraison() == null) {
            l.setDateLivraison(LocalDateTime.now().plusDays(3));
        }

        l.setAdresse(l.getAdresse().trim());

        if (l.getStatut() == null) {
            l.setStatut(Livraison.StatutLivraison.EN_PREPARATION);
        }

        Livraison saved = rep.save(l);
        return converter.toLivraisonDTO(saved);
    }

    // ============================================================
    // CHANGEMENT DE STATUT
    // ============================================================

    /**
     * Change le statut d'une livraison - retourne un DTO
     *
     * Logique métier automatique :
     * - EN_TRANSIT  → met la commande à EXPEDIEE
     * - LIVREE      → met la commande à LIVREE + met à jour les stocks des produits
     */
    @Transactional
    public LivraisonDTO changerStatut(Integer id, Livraison.StatutLivraison statut) {
        Livraison livraison = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Livraison introuvable"));

        validerTransitionStatut(livraison.getStatut(), statut);
        livraison.setStatut(statut);
        Livraison saved = rep.save(livraison);

        Commande commande = saved.getCommande();

        // Quand la livraison part → commande passe à EXPEDIEE
        if (statut == Livraison.StatutLivraison.EN_TRANSIT
                && commande.getStatut() == Commande.StatutCommande.VALIDEE) {
            commande.setStatut(Commande.StatutCommande.EXPEDIEE);
            commandeRepository.save(commande);
        }

        // Quand la livraison arrive → commande passe à LIVREE + mise à jour des stocks
        if (statut == Livraison.StatutLivraison.LIVREE
                && commande.getStatut() != Commande.StatutCommande.ANNULEE) {
            commande.setStatut(Commande.StatutCommande.LIVREE);
            commandeRepository.save(commande);

            // Mise à jour automatique des stocks à la réception
            mettreAJourStocks(commande.getId());
        }

        return converter.toLivraisonDTO(saved);
    }

    /**
     * Met à jour le stock de chaque produit présent dans les lignes de la commande
     * Stock = stock actuel + quantité commandée (réception de marchandise)
     */
    private void mettreAJourStocks(Integer commandeId) {
        List<LigneCommande> lignes = ligneCommandeRepository.findByCommandeId(commandeId);
        for (LigneCommande ligne : lignes) {
            Produit produit = ligne.getProduit();
            int ancienStock = produit.getQuantiteEnStock() != null ? produit.getQuantiteEnStock() : 0;
            produit.setQuantiteEnStock(ancienStock + ligne.getQuantite());
            produitRepository.save(produit);
        }
    }

    // ============================================================
    // SUPPRESSION
    // ============================================================

    /**
     * Supprime une livraison par son ID
     */
    @Transactional
    public void delete(Integer id) {
        if (!rep.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livraison introuvable");
        }
        rep.deleteById(id);
    }

    // ============================================================
    // VALIDATION INTERNE
    // ============================================================

    /**
     * Valide qu'une transition de statut de livraison est autorisée
     * Règles : EN_PREPARATION → EN_TRANSIT → LIVREE (sens unique)
     */
    private void validerTransitionStatut(Livraison.StatutLivraison actuel, Livraison.StatutLivraison cible) {
        if (actuel == cible) return;

        boolean valide =
                (actuel == Livraison.StatutLivraison.EN_PREPARATION
                    && cible == Livraison.StatutLivraison.EN_TRANSIT)
             || (actuel == Livraison.StatutLivraison.EN_TRANSIT
                    && cible == Livraison.StatutLivraison.LIVREE);

        if (!valide) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Transition de statut de livraison non autorisee");
        }
    }
}
