package com.gestion.commandes.service;

import com.gestion.commandes.converter.EntityConverter;
import com.gestion.commandes.dto.CommandeDTO;
import com.gestion.commandes.entity.Client;
import com.gestion.commandes.entity.Commande;
import com.gestion.commandes.repository.ClientRepository;
import com.gestion.commandes.repository.CommandeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service Commande - Gère la logique métier des commandes
 *
 * Utilise EntityConverter (dossier converter/) pour transformer
 * les entités JPA en DTOs avant de les envoyer au frontend.
 */
@Service
public class CommandeService {

    // Injection par @Autowired (style utilisé dans ce projet)
    @Autowired
    private CommandeRepository rep;

    @Autowired
    private ClientRepository clientRepository;

    // Converter injecté : convertit entité → DTO
    @Autowired
    private EntityConverter converter;

    // ============================================================
    // LECTURE
    // ============================================================

    /**
     * Récupère toutes les commandes sous forme de DTOs
     */
    public List<CommandeDTO> chercherTout() {
        return rep.findAll()
                  .stream()
                  .map(converter::toCommandeDTO)
                  .collect(Collectors.toList());
    }

    /**
     * Récupère une commande par son ID (retourne un DTO)
     */
    public CommandeDTO chercherParId(Integer id) {
        Commande commande = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Commande introuvable avec l'ID : " + id));
        return converter.toCommandeDTO(commande);
    }

    /**
     * Récupère les commandes d'un client sous forme de DTOs
     */
    public List<CommandeDTO> chercherParClient(Integer clientId) {
        return rep.findByClientId(clientId)
                  .stream()
                  .map(converter::toCommandeDTO)
                  .collect(Collectors.toList());
    }

    /**
     * Récupère les commandes par statut sous forme de DTOs
     */
    public List<CommandeDTO> chercherParStatut(Commande.StatutCommande statut) {
        return rep.findByStatut(statut)
                  .stream()
                  .map(converter::toCommandeDTO)
                  .collect(Collectors.toList());
    }

    /**
     * Historique des commandes d'un client, triées par date (la plus récente en premier)
     */
    public List<CommandeDTO> historiqueParClient(Integer clientId) {
        return rep.findByClientId(clientId)
                  .stream()
                  .sorted((c1, c2) -> c2.getDateCommande().compareTo(c1.getDateCommande()))
                  .map(converter::toCommandeDTO)
                  .collect(Collectors.toList());
    }

    // ============================================================
    // CRÉATION / MODIFICATION
    // ============================================================

    /**
     * Crée une nouvelle commande - retourne un DTO
     */
    @Transactional
    public CommandeDTO ajouter(Commande c) {
        if (c.getClient() == null || c.getClient().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client obligatoire");
        }
        // Vérifier que le client existe
        Client client = clientRepository.findById(c.getClient().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Client introuvable"));
        c.setClient(client);

        if (c.getMontantTotal() == null || c.getMontantTotal() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Montant de commande invalide");
        }
        if (c.getAdresseLivraison() == null || c.getAdresseLivraison().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adresse de livraison obligatoire");
        }

        // Initialiser la date et le statut par défaut
        if (c.getDateCommande() == null) {
            c.setDateCommande(LocalDateTime.now());
        }
        if (c.getStatut() == null) {
            c.setStatut(Commande.StatutCommande.EN_ATTENTE);
        }

        Commande saved = rep.save(c);
        return converter.toCommandeDTO(saved);
    }

    /**
     * Modifie une commande existante - retourne un DTO
     */
    @Transactional
    public CommandeDTO update(Integer id, Commande c) {
        Commande existante = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Commande introuvable avec l'ID : " + id));

        if (c.getStatut() != null) {
            existante.setStatut(c.getStatut());
        }
        if (c.getMontantTotal() != null) {
            existante.setMontantTotal(c.getMontantTotal());
        }
        if (c.getAdresseLivraison() != null) {
            existante.setAdresseLivraison(c.getAdresseLivraison());
        }

        Commande saved = rep.save(existante);
        return converter.toCommandeDTO(saved);
    }

    /**
     * Change le statut d'une commande - retourne un DTO
     * Valide la transition avant d'appliquer le changement
     */
    @Transactional
    public CommandeDTO changerStatut(Integer id, Commande.StatutCommande statut) {
        Commande commande = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Commande introuvable avec l'ID : " + id));
        validerTransitionStatut(commande.getStatut(), statut);
        commande.setStatut(statut);
        Commande saved = rep.save(commande);
        return converter.toCommandeDTO(saved);
    }

    /**
     * Valide qu'une transition de statut est autorisée
     * Règles métier :
     *   EN_ATTENTE → VALIDEE ou ANNULEE
     *   VALIDEE    → EXPEDIEE ou ANNULEE
     *   EXPEDIEE   → LIVREE
     *   LIVREE / ANNULEE → aucune transition possible
     */
    private void validerTransitionStatut(Commande.StatutCommande actuel, Commande.StatutCommande cible) {
        if (actuel == cible) return;

        if (actuel == Commande.StatutCommande.ANNULEE || actuel == Commande.StatutCommande.LIVREE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transition de statut non autorisee");
        }

        boolean transitionValide =
                (actuel == Commande.StatutCommande.EN_ATTENTE
                    && (cible == Commande.StatutCommande.VALIDEE || cible == Commande.StatutCommande.ANNULEE))
             || (actuel == Commande.StatutCommande.VALIDEE
                    && (cible == Commande.StatutCommande.EXPEDIEE || cible == Commande.StatutCommande.ANNULEE))
             || (actuel == Commande.StatutCommande.EXPEDIEE
                    && cible == Commande.StatutCommande.LIVREE);

        if (!transitionValide) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transition de statut non autorisee");
        }
    }

    // ============================================================
    // SUPPRESSION
    // ============================================================

    /**
     * Supprime une commande par son ID
     */
    @Transactional
    public void delete(Integer id) {
        if (!rep.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Commande introuvable avec l'ID : " + id);
        }
        rep.deleteById(id);
    }

    // ============================================================
    // STATISTIQUES
    // ============================================================

    /**
     * Compte les commandes par statut
     * Retourne une Map : { "EN_ATTENTE": 5, "VALIDEE": 3, ... }
     */
    public Map<String, Long> compterParStatut() {
        Map<String, Long> stats = new HashMap<>();
        for (Commande.StatutCommande statut : Commande.StatutCommande.values()) {
            long count = rep.findByStatut(statut).size();
            stats.put(statut.name(), count);
        }
        return stats;
    }

    /**
     * Calcule le chiffre d'affaires total (somme des commandes LIVREES)
     */
    public Double chiffresAffaires() {
        return rep.findByStatut(Commande.StatutCommande.LIVREE)
                  .stream()
                  .mapToDouble(Commande::getMontantTotal)
                  .sum();
    }
}
