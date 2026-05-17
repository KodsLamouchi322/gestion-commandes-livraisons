package com.gestion.commandes.service;

import com.gestion.commandes.converter.EntityConverter;
import com.gestion.commandes.dto.LivraisonDTO;
import com.gestion.commandes.entity.Commande;
import com.gestion.commandes.entity.Livraison;
import com.gestion.commandes.entity.Paiement;
import com.gestion.commandes.entity.Transporteur;
import com.gestion.commandes.repository.CommandeRepository;
import com.gestion.commandes.repository.LivraisonRepository;
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
@SuppressWarnings("null")
@Service
public class LivraisonService {

    @Autowired
    private LivraisonRepository rep;

    @Autowired
    private CommandeRepository commandeRepository;

    @Autowired
    private TransporteurRepository transporteurRepository;



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
     * Récupère la livraison d'une commande (retourne un DTO ou null si pas de livraison)
     */
    public LivraisonDTO chercherParCommande(Integer commandeId) {
        return rep.findByCommandeId(commandeId)
                .map(converter::toLivraisonDTO)
                .orElse(null);
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

    /**
     * Crée une livraison depuis une commande validée
     * 
     * @param commandeId ID de la commande
     * @param cout Coût de la livraison (défaut 0.0 si null)
     * @param transporteurId ID du transporteur (optionnel)
     * @return DTO de la livraison créée
     * 
     * Validations:
     * - La commande doit exister
     * - Statut commande : EN_ATTENTE ou VALIDEE (création livraison possible avant ou après validation admin)
     * - Aucune livraison ne doit déjà exister pour cette commande
     * - Le transporteur doit exister (si fourni)
     * 
     * Initialisation:
     * - Adresse: copiée depuis la commande
     * - Statut: EN_PREPARATION
     * - Coût: valeur fournie ou 0.0 par défaut
     * - Date de livraison estimée: maintenant + 3 jours
     */
    @Transactional
    public LivraisonDTO creerDepuisCommande(Integer commandeId, Double cout, Integer transporteurId) {
        // 1. Valider que la commande existe
        Commande commande = commandeRepository.findById(commandeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Commande introuvable"));

        // 2. Statuts autorisés : EN_ATTENTE ou VALIDEE (l'admin peut créer la livraison avant ou après « Valider » la commande).
        //    Refus si commande annulée, déjà livrée au client, ou déjà marquée expédiée (cohérence logistique).
        if (commande.getStatut() == Commande.StatutCommande.ANNULEE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Impossible de créer une livraison pour une commande annulée");
        }
        if (commande.getStatut() == Commande.StatutCommande.LIVREE
                || commande.getStatut() == Commande.StatutCommande.EXPEDIEE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Impossible de créer une livraison : la commande est déjà expédiée ou livrée");
        }
        if (commande.getStatut() != Commande.StatutCommande.EN_ATTENTE
                && commande.getStatut() != Commande.StatutCommande.VALIDEE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Statut de commande incompatible avec la création d'une livraison");
        }

        // 3. Valider qu'aucune livraison n'existe déjà pour cette commande
        if (rep.existsByCommandeId(commandeId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, 
                    "Une livraison existe déjà pour cette commande");
        }

        // 4. Valider le transporteur (si fourni)
        Transporteur transporteur = null;
        if (transporteurId != null) {
            transporteur = transporteurRepository.findById(transporteurId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Transporteur introuvable"));
        }

        // 5. Créer la livraison avec les valeurs initiales
        Livraison livraison = new Livraison();
        livraison.setCommande(commande);
        livraison.setAdresse(commande.getAdresseLivraison());
        livraison.setStatut(Livraison.StatutLivraison.EN_PREPARATION);
        livraison.setCout(cout != null ? cout : 0.0);
        livraison.setDateLivraison(LocalDateTime.now().plusDays(3));
        livraison.setTransporteur(transporteur);

        // 6. Sauvegarder et retourner le DTO
        Livraison saved = rep.save(livraison);
        return converter.toLivraisonDTO(saved);
    }

    // ============================================================
    // ASSIGNATION DE TRANSPORTEUR
    // ============================================================

    /**
     * Assigne un transporteur à une livraison
     * 
     * @param livraisonId ID de la livraison
     * @param transporteurId ID du transporteur à assigner
     * @return DTO de la livraison mise à jour
     * 
     * Validations:
     * - La livraison doit exister
     * - La livraison doit avoir le statut EN_PREPARATION
     * - Le transporteur doit exister
     * 
     * Comportement:
     * - Assigne le transporteur à la livraison
     * - Maintient le statut actuel de la livraison
     */
    @Transactional
    public LivraisonDTO assignerTransporteur(Integer livraisonId, Integer transporteurId) {
        // 1. Charger la livraison et valider qu'elle existe
        Livraison livraison = rep.findById(livraisonId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Livraison introuvable"));

        // 2. Valider que la livraison est en statut EN_PREPARATION
        if (livraison.getStatut() != Livraison.StatutLivraison.EN_PREPARATION) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Le transporteur ne peut être assigné qu'aux livraisons en préparation");
        }

        // 3. Charger le transporteur et valider qu'il existe
        Transporteur transporteur = transporteurRepository.findById(transporteurId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Transporteur introuvable"));

        // 4. Assigner le transporteur à la livraison
        livraison.setTransporteur(transporteur);

        // 5. Sauvegarder et retourner le DTO
        Livraison saved = rep.save(livraison);
        return converter.toLivraisonDTO(saved);
    }

    // ============================================================
    // CHANGEMENT DE STATUT
    // ============================================================

    /**
     * Change le statut d'une livraison - retourne un DTO
     *
     * Logique métier automatique :
     * - EXPEDIEE    → met la commande à EXPEDIEE
     * - LIVREE      → met la commande à LIVREE + met à jour les stocks des produits
     * 
     * Règles de transition :
     * - EN_PREPARATION → EXPEDIEE (uniquement)
     * - EXPEDIEE → LIVREE (uniquement)
     * - LIVREE → aucune transition autorisée
     * 
     * @param id ID de la livraison
     * @param statut Nouveau statut de la livraison
     * @return DTO de la livraison mise à jour
     * 
     * Validations:
     * - La livraison doit exister
     * - La transition de statut doit être valide
     * - La commande ne doit pas être annulée
     * 
     * Comportement transactionnel:
     * - Met à jour le statut de la livraison
     * - Met à jour le timestamp de la livraison
     * - Synchronise le statut de la commande associée
     * - Toutes les modifications sont atomiques (même transaction)
     */
    @Transactional
    public LivraisonDTO changerStatut(Integer id, Livraison.StatutLivraison statut) {
        // 1. Charger la livraison et valider qu'elle existe
        Livraison livraison = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Livraison introuvable"));

        // 2. Charger la commande associée
        Commande commande = livraison.getCommande();
        
        // 3. Valider que la commande n'est pas annulée
        if (commande.getStatut() == Commande.StatutCommande.ANNULEE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "Impossible de modifier le statut d'une livraison pour une commande annulée");
        }

        // 4. Valider la transition de statut
        validerTransitionStatut(livraison.getStatut(), statut);
        
        // 5. Mettre à jour le statut et le timestamp de la livraison
        livraison.setStatut(statut);
        livraison.setDateLivraison(LocalDateTime.now());
        Livraison saved = rep.save(livraison);

        // 6. Synchroniser le statut de la commande selon le statut de livraison
        if (statut == Livraison.StatutLivraison.EXPEDIEE) {
            // Quand la livraison est expédiée → commande passe à EXPEDIEE
            boolean isEspeces = commande.getPaiement() != null && commande.getPaiement().getMethodePaiement() == Paiement.MethodePaiement.ESPECES;
            if (commande.getStatut() == Commande.StatutCommande.VALIDEE || (commande.getStatut() == Commande.StatutCommande.EN_ATTENTE && isEspeces)) {
                commande.setStatut(Commande.StatutCommande.EXPEDIEE);
                commandeRepository.save(commande);
            }
        } else if (statut == Livraison.StatutLivraison.LIVREE) {
            // Quand la livraison arrive → commande passe à LIVREE
            commande.setStatut(Commande.StatutCommande.LIVREE);
            commandeRepository.save(commande);
            // NB: Le stock a déjà été déduit lors de la réservation (CommandeService), pas besoin de le mettre à jour ici.
        }

        return converter.toLivraisonDTO(saved);
    }

    // Suppression de mettreAJourStocks() car redondant et génère un bug sur les stocks clients

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
     * Règles : EN_PREPARATION → EXPEDIEE → LIVREE (sens unique)
     * 
     * Transitions valides:
     * - EN_PREPARATION → EXPEDIEE
     * - EXPEDIEE → LIVREE
     * - LIVREE → aucune transition
     */
    private void validerTransitionStatut(Livraison.StatutLivraison actuel, Livraison.StatutLivraison cible) {
        if (actuel == cible) return;

        boolean valide =
                (actuel == Livraison.StatutLivraison.EN_PREPARATION
                    && cible == Livraison.StatutLivraison.EXPEDIEE)
             || (actuel == Livraison.StatutLivraison.EXPEDIEE
                    && cible == Livraison.StatutLivraison.LIVREE);

        if (!valide) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Transition de statut de livraison non autorisee : " + actuel + " -> " + cible);
        }
    }
}
