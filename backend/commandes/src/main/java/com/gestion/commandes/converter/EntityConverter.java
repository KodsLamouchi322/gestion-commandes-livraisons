package com.gestion.commandes.converter;

import com.gestion.commandes.dto.*;
import com.gestion.commandes.entity.*;
import com.gestion.commandes.repository.LivraisonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * EntityConverter - Convertit les entités JPA vers les DTOs
 *
 * Pourquoi ce dossier converter ?
 * - Séparer la logique de conversion du code métier (services)
 * - Éviter les boucles infinies JSON (entité → entité → ...)
 * - Ne jamais exposer des champs sensibles (ex: motDePasse)
 *
 * Injection par @Autowired (style utilisé dans ce projet)
 */
@Component
public class EntityConverter {

    // Injecté pour calculer le nombre de livraisons d'un transporteur
    @Autowired
    private LivraisonRepository livraisonRepository;

    // Injecté pour récupérer le paiement d'une commande
    @Autowired
    private com.gestion.commandes.repository.PaiementRepository paiementRepository;

    // ============================================================
    // CLIENT
    // ============================================================

    /**
     * Convertit un Client en ClientDTO
     * Le motDePasse est EXCLU : il n'apparaît jamais dans la réponse frontend
     */
    public ClientDTO toClientDTO(Client client) {
        if (client == null) return null;

        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setNom(client.getNom());
        dto.setPrenom(client.getPrenom());
        dto.setEmail(client.getEmail());
        dto.setTelephone(client.getTelephone());
        dto.setAdresse(client.getAdresse());
        dto.setRole(client.getRole());
        // motDePasse volontairement omis
        return dto;
    }

    // ============================================================
    // COMMANDE
    // ============================================================

    /**
     * Convertit une Commande en CommandeDTO
     * Inclut les infos du client (sans mot de passe) et les lignes de commande
     */
    public CommandeDTO toCommandeDTO(Commande commande) {
        if (commande == null) return null;

        CommandeDTO dto = new CommandeDTO();
        dto.setId(commande.getId());
        dto.setDateCommande(commande.getDateCommande());
        dto.setStatut(commande.getStatut());
        dto.setMontantTotal(commande.getMontantTotal());
        dto.setAdresseLivraison(commande.getAdresseLivraison());

        // Infos client : seulement id, nom, email (pas l'objet Client entier)
        if (commande.getClient() != null) {
            dto.setClientId(commande.getClient().getId());
            dto.setClientNom(commande.getClient().getNom());
            dto.setClientEmail(commande.getClient().getEmail());
        }

        // Lignes de commande
        List<LigneCommande> lignes = commande.getLignesCommande();
        if (lignes != null && !lignes.isEmpty()) {
            dto.setLignesCommande(
                lignes.stream()
                      .map(this::toLigneCommandeDTO)
                      .collect(Collectors.toList())
            );
        } else {
            dto.setLignesCommande(Collections.emptyList());
        }

        // Paiement (si existe)
        if (commande.getId() != null) {
            paiementRepository.findByCommandeId(commande.getId())
                .ifPresent(paiement -> dto.setPaiement(toPaiementDTO(paiement)));
        }

        return dto;
    }

    // ============================================================
    // LIGNE COMMANDE
    // ============================================================

    /**
     * Convertit une LigneCommande en LigneCommandeDTO
     * Inclut les infos du produit (id, nom, prix) sans boucle infinie
     */
    public LigneCommandeDTO toLigneCommandeDTO(LigneCommande ligne) {
        if (ligne == null) return null;

        LigneCommandeDTO dto = new LigneCommandeDTO();
        dto.setId(ligne.getId());
        dto.setQuantite(ligne.getQuantite());
        dto.setPrixUnitaire(ligne.getPrixUnitaire());
        dto.setSousTotal(ligne.getSousTotal());

        // Infos produit : seulement id, nom, prix
        if (ligne.getProduit() != null) {
            dto.setProduitId(ligne.getProduit().getId());
            dto.setProduitNom(ligne.getProduit().getNom());
            dto.setProduitPrix(ligne.getProduit().getPrixUnitaire());
        }

        return dto;
    }

    // ============================================================
    // LIVRAISON
    // ============================================================

    /**
     * Convertit une Livraison en LivraisonDTO
     * Inclut commandeId et les infos du transporteur
     */
    public LivraisonDTO toLivraisonDTO(Livraison livraison) {
        if (livraison == null) return null;

        LivraisonDTO dto = new LivraisonDTO();
        dto.setId(livraison.getId());
        dto.setDateLivraison(livraison.getDateLivraison());
        dto.setAdresse(livraison.getAdresse());
        dto.setCout(livraison.getCout());
        dto.setStatut(livraison.getStatut());

        // Infos commande : seulement l'id
        if (livraison.getCommande() != null) {
            dto.setCommandeId(livraison.getCommande().getId());
        }

        // Infos transporteur : id et nom
        if (livraison.getTransporteur() != null) {
            dto.setTransporteurId(livraison.getTransporteur().getId());
            dto.setTransporteurNom(livraison.getTransporteur().getNom());
        }

        return dto;
    }

    // ============================================================
    // PAIEMENT
    // ============================================================

    /**
     * Convertit un Paiement en PaiementDTO
     */
    public PaiementDTO toPaiementDTO(Paiement paiement) {
        if (paiement == null) return null;

        PaiementDTO dto = new PaiementDTO();
        dto.setId(paiement.getId());
        dto.setDatePaiement(paiement.getDatePaiement());
        dto.setMontant(paiement.getMontant());
        dto.setMethodePaiement(paiement.getMethodePaiement());
        dto.setStatut(paiement.getStatut());

        // Infos commande : seulement l'id
        if (paiement.getCommande() != null) {
            dto.setCommandeId(paiement.getCommande().getId());
        }

        return dto;
    }

    // ============================================================
    // TRANSPORTEUR
    // ============================================================

    /**
     * Convertit un Transporteur en TransporteurDTO
     * Compte ses livraisons via le repository
     */
    public TransporteurDTO toTransporteurDTO(Transporteur transporteur) {
        if (transporteur == null) return null;

        TransporteurDTO dto = new TransporteurDTO();
        dto.setId(transporteur.getId());
        dto.setNom(transporteur.getNom());
        dto.setTelephone(transporteur.getTelephone());
        dto.setEmail(transporteur.getEmail());
        dto.setNote(transporteur.getNote());

        // Nombre de livraisons assignées à ce transporteur
        dto.setNombreLivraisons(livraisonRepository.countByTransporteurId(transporteur.getId()));

        return dto;
    }
}
