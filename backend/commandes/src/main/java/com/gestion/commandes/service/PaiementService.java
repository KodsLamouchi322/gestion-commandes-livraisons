package com.gestion.commandes.service;

import com.gestion.commandes.converter.EntityConverter;
import com.gestion.commandes.dto.PaiementDTO;
import com.gestion.commandes.entity.Commande;
import com.gestion.commandes.entity.Paiement;
import com.gestion.commandes.repository.CommandeRepository;
import com.gestion.commandes.repository.PaiementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service Paiement - Gère la logique métier des paiements
 *
 * Quand un paiement est validé (statut → VALIDE),
 * la commande associée passe automatiquement à VALIDEE.
 */
@Service
public class PaiementService {

    @Autowired
    private PaiementRepository rep;

    @Autowired
    private CommandeRepository commandeRepository;

    // Converter : entité → DTO
    @Autowired
    private EntityConverter converter;

    // ============================================================
    // LECTURE
    // ============================================================

    /**
     * Récupère tous les paiements sous forme de DTOs
     */
    public List<PaiementDTO> chercherTout() {
        return rep.findAll()
                  .stream()
                  .map(converter::toPaiementDTO)
                  .collect(Collectors.toList());
    }

    /**
     * Récupère un paiement par son ID (retourne un DTO)
     */
    public PaiementDTO chercherParId(Integer id) {
        Paiement paiement = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paiement introuvable"));
        return converter.toPaiementDTO(paiement);
    }

    /**
     * Récupère le paiement d'une commande (retourne un DTO)
     */
    public PaiementDTO chercherParCommande(Integer commandeId) {
        Paiement paiement = rep.findByCommandeId(commandeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paiement introuvable pour cette commande"));
        return converter.toPaiementDTO(paiement);
    }

    // ============================================================
    // CRÉATION
    // ============================================================

    /**
     * Crée un nouveau paiement - retourne un DTO
     * Le montant est automatiquement aligné sur le montant total de la commande
     */
    @Transactional
    public PaiementDTO ajouter(Paiement p) {
        if (p.getCommande() == null || p.getCommande().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Commande obligatoire pour le paiement");
        }

        Commande commande = commandeRepository.findById(p.getCommande().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Commande introuvable"));

        if (commande.getStatut() == Commande.StatutCommande.ANNULEE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Paiement impossible sur une commande annulee");
        }

        if (rep.findByCommandeId(commande.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Un paiement existe deja pour cette commande");
        }

        p.setCommande(commande);

        // Date du paiement : maintenant si non fournie
        if (p.getDatePaiement() == null) {
            p.setDatePaiement(LocalDateTime.now());
        }

        // Statut initial : EN_ATTENTE
        if (p.getStatut() == null) {
            p.setStatut(Paiement.StatutPaiement.EN_ATTENTE);
        }

        // Montant = montant total de la commande (automatique)
        p.setMontant(commande.getMontantTotal());

        Paiement saved = rep.save(p);
        return converter.toPaiementDTO(saved);
    }

    // ============================================================
    // CHANGEMENT DE STATUT
    // ============================================================

    /**
     * Change le statut d'un paiement - retourne un DTO
     *
     * Logique métier : si le paiement est VALIDE,
     * la commande passe automatiquement à VALIDEE
     */
    @Transactional
    public PaiementDTO changerStatut(Integer id, Paiement.StatutPaiement statut) {
        Paiement paiement = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Paiement introuvable"));

        if (paiement.getStatut() == Paiement.StatutPaiement.VALIDE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un paiement valide ne peut plus etre modifie");
        }

        paiement.setStatut(statut);
        Paiement saved = rep.save(paiement);

        // Validation du paiement → commande automatiquement validée
        if (statut == Paiement.StatutPaiement.VALIDE) {
            Commande commande = saved.getCommande();
            commande.setStatut(Commande.StatutCommande.VALIDEE);
            commandeRepository.save(commande);
        }

        return converter.toPaiementDTO(saved);
    }

    // ============================================================
    // SUPPRESSION
    // ============================================================

    /**
     * Supprime un paiement par son ID
     */
    @Transactional
    public void delete(Integer id) {
        if (!rep.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Paiement introuvable");
        }
        rep.deleteById(id);
    }
}
