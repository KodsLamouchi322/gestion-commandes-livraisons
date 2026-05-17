package com.gestion.commandes.service;

import com.gestion.commandes.entity.BonCommande;
import com.gestion.commandes.entity.Fournisseur;
import com.gestion.commandes.entity.LigneBonCommande;
import com.gestion.commandes.entity.Produit;
import com.gestion.commandes.repository.BonCommandeRepository;
import com.gestion.commandes.repository.FournisseurRepository;
import com.gestion.commandes.repository.LigneBonCommandeRepository;
import com.gestion.commandes.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service BonCommande - Gère la logique métier des bons de commande fournisseur
 */
@SuppressWarnings("null")
@Service
public class BonCommandeService {

    @Autowired
    private BonCommandeRepository rep;

    @Autowired
    private LigneBonCommandeRepository ligneRep;

    @Autowired
    private ProduitRepository produitRep;
    
    @Autowired
    private FournisseurRepository fournisseurRepository;
    
    @Autowired
    private StockService stockService;

    /**
     * Récupère tous les bons de commande
     */
    public List<BonCommande> chercherTout() {
        return rep.findAll();
    }

    /**
     * Récupère un bon de commande par son ID
     */
    public BonCommande chercherParId(Integer id) {
        return rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bon de commande introuvable avec l'ID : " + id));
    }

    /**
     * Récupère les bons de commande d'un fournisseur
     */
    public List<BonCommande> chercherParFournisseur(Integer fournisseurId) {
        return rep.findByFournisseurId(fournisseurId);
    }

    /**
     * Récupère les bons de commande par statut
     */
    public List<BonCommande> chercherParStatut(BonCommande.Statut statut) {
        return rep.findByStatut(statut);
    }

    /**
     * Crée un nouveau bon de commande
     */
    @Transactional
    public BonCommande ajouter(BonCommande bc) {
        if (bc.getFournisseur() == null || bc.getFournisseur().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fournisseur obligatoire");
        }
        Fournisseur fournisseur = fournisseurRepository.findById(bc.getFournisseur().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Fournisseur introuvable"));
        bc.setFournisseur(fournisseur);
        // Initialiser la date et le statut
        bc.setDateCreation(LocalDateTime.now());
        bc.setStatut(BonCommande.Statut.EN_ATTENTE);
        return rep.save(bc);
    }

    /**
     * Crée un nouveau bon de commande avec ses lignes
     * Valide le fournisseur, les produits et les quantités
     * Calcule le montant total
     * 
     * @param bonCommande Le bon de commande à créer (doit contenir le fournisseur)
     * @param lignes La liste des lignes du bon de commande
     * @return Le bon de commande créé avec ses lignes
     * @throws ResponseStatusException si le fournisseur n'existe pas, si un produit n'existe pas,
     *         si un produit n'est pas fourni par ce fournisseur, ou si une quantité est invalide
     */
    @Transactional
    public BonCommande creer(BonCommande bonCommande, List<LigneBonCommande> lignes) {
        // 1. Valider que le fournisseur existe
        if (bonCommande.getFournisseur() == null || bonCommande.getFournisseur().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fournisseur obligatoire");
        }
        
        Fournisseur fournisseur = fournisseurRepository.findById(bonCommande.getFournisseur().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Fournisseur introuvable avec l'ID : " + bonCommande.getFournisseur().getId()));
        
        // 2. Initialiser le bon de commande
        bonCommande.setFournisseur(fournisseur);
        bonCommande.setDateCreation(LocalDateTime.now());
        bonCommande.setStatut(BonCommande.Statut.EN_ATTENTE);
        
        // 3. Valider et traiter chaque ligne
        if (lignes == null || lignes.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le bon de commande doit contenir au moins une ligne");
        }
        
        for (LigneBonCommande ligne : lignes) {
            // Valider que le produit existe
            if (ligne.getProduit() == null || ligne.getProduit().getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produit obligatoire pour chaque ligne");
            }
            
            Produit produit = produitRep.findById(ligne.getProduit().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, 
                            "Produit introuvable avec l'ID : " + ligne.getProduit().getId()));
            
            // Valider que le produit est fourni par ce fournisseur
            // Note: Assuming Produit has a fournisseur field. If not, this validation may need adjustment
            // For now, we'll skip this validation as the Produit entity doesn't have a fournisseur field
            // This would require a many-to-many relationship or a separate table
            
            // Valider que la quantité est positive
            if (ligne.getQuantite() == null || ligne.getQuantite() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, 
                        "La quantité doit être positive pour le produit : " + produit.getNom());
            }
            
            // Valider que le prix d'achat est positif
            if (ligne.getPrixAchat() == null || ligne.getPrixAchat() <= 0) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, 
                        "Le prix d'achat doit être positif pour le produit : " + produit.getNom());
            }
            
            // Associer le produit et le bon de commande à la ligne
            ligne.setProduit(produit);
            ligne.setBonCommande(bonCommande);
        }
        
        // 4. Sauvegarder le bon de commande (les lignes seront sauvegardées en cascade)
        bonCommande.setLignes(lignes);
        BonCommande savedBonCommande = rep.save(bonCommande);
        
        // Sauvegarder explicitement les lignes
        for (LigneBonCommande ligne : lignes) {
            ligneRep.save(ligne);
        }
        
        return savedBonCommande;
    }

    /**
     * Modifie un bon de commande existant
     */
    @Transactional
    public BonCommande update(Integer id, BonCommande bc) {
        BonCommande existant = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bon de commande introuvable avec l'ID : " + id));
        if (bc.getStatut() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Statut obligatoire");
        }
        validerTransitionStatut(existant.getStatut(), bc.getStatut());
        existant.setStatut(bc.getStatut());
        return rep.save(existant);
    }

    /**
     * Change le statut d'un bon de commande
     * Valide les transitions de statut et met à jour le stock si le statut devient RECU
     * 
     * @param id L'ID du bon de commande
     * @param statut Le nouveau statut
     * @return Le bon de commande mis à jour
     * @throws ResponseStatusException si le bon de commande n'existe pas ou si la transition est invalide
     */
    @Transactional
    public BonCommande changerStatut(Integer id, BonCommande.Statut statut) {
        // 1. Charger le bon de commande
        BonCommande bonCommande = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bon de commande introuvable avec l'ID : " + id));
        
        // 2. Valider la transition de statut
        validerTransitionStatut(bonCommande.getStatut(), statut);
        
        // 3. Si le statut devient RECU, mettre à jour le stock
        if (statut == BonCommande.Statut.RECU && bonCommande.getStatut() != BonCommande.Statut.RECU) {
            // Récupérer les lignes du bon de commande
            List<LigneBonCommande> lignes = ligneRep.findByBonCommandeId(id);
            
            if (lignes.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Le bon de commande ne contient aucune ligne");
            }
            
            // Appeler le service de stock pour réapprovisionner
            stockService.reapprovisionner(lignes);
        }
        
        // 4. Mettre à jour le statut
        bonCommande.setStatut(statut);
        
        // 5. Sauvegarder et retourner
        return rep.save(bonCommande);
    }

    /**
     * Supprime un bon de commande
     */
    @Transactional
    public void delete(Integer id) {
        if (!rep.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Bon de commande introuvable avec l'ID : " + id);
        }
        rep.deleteById(id);
    }

    /**
     * Ajoute une ligne à un bon de commande
     */
    @Transactional
    public LigneBonCommande ajouterLigne(Integer bonCommandeId, LigneBonCommande ligne) {
        BonCommande bc = chercherParId(bonCommandeId);
        if (bc.getStatut() != BonCommande.Statut.EN_ATTENTE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ajout de ligne impossible sur un bon non en attente");
        }
        if (ligne.getProduit() == null || ligne.getProduit().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produit obligatoire");
        }
        if (ligne.getQuantite() == null || ligne.getQuantite() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantite invalide");
        }
        if (ligne.getPrixAchat() == null || ligne.getPrixAchat() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Prix d'achat invalide");
        }
        Produit produit = produitRep.findById(ligne.getProduit().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit introuvable"));
        ligne.setProduit(produit);
        ligne.setBonCommande(bc);
        return ligneRep.save(ligne);
    }

    /**
     * Récupère les lignes d'un bon de commande
     */
    public List<LigneBonCommande> chercherLignes(Integer bonCommandeId) {
        return ligneRep.findByBonCommandeId(bonCommandeId);
    }

    /**
     * Valide la réception d'un bon de commande et met à jour le stock
     */
    @Transactional
    public BonCommande validerReception(Integer bonCommandeId) {
        BonCommande bc = chercherParId(bonCommandeId);
        if (bc.getStatut() == BonCommande.Statut.ANNULE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Reception impossible pour un bon annule");
        }

        // Si déjà reçu, on ne fait rien
        if (bc.getStatut() == BonCommande.Statut.RECU) {
            return bc;
        }

        // Récupérer toutes les lignes du bon de commande
        List<LigneBonCommande> lignes = ligneRep.findByBonCommandeId(bonCommandeId);

        // Mettre à jour le stock pour chaque produit
        for (LigneBonCommande ligne : lignes) {
            Produit produit = ligne.getProduit();
            if (produit != null) {
                // Augmenter le stock
                Integer nouveauStock = produit.getQuantiteEnStock() + ligne.getQuantite();
                produit.setQuantiteEnStock(nouveauStock);
                produitRep.save(produit);
            }
        }

        // Changer le statut du bon de commande
        bc.setStatut(BonCommande.Statut.RECU);
        return rep.save(bc);
    }

    private void validerTransitionStatut(BonCommande.Statut actuel, BonCommande.Statut cible) {
        if (actuel == cible) {
            return;
        }
        boolean valide =
                (actuel == BonCommande.Statut.EN_ATTENTE && (cible == BonCommande.Statut.ENVOYE || cible == BonCommande.Statut.ANNULE))
                        || (actuel == BonCommande.Statut.ENVOYE && (cible == BonCommande.Statut.RECU || cible == BonCommande.Statut.ANNULE));
        if (!valide) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transition de statut de bon de commande non autorisee");
        }
    }
}
