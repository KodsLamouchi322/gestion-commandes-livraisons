package com.gestion.commandes.service;

import com.gestion.commandes.entity.Avis;
import com.gestion.commandes.entity.Client;
import com.gestion.commandes.entity.Produit;
import com.gestion.commandes.repository.AvisRepository;
import com.gestion.commandes.repository.ClientRepository;
import com.gestion.commandes.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service Avis - Gère la logique métier des avis clients sur les produits
 */
@Service
public class AvisService {

    @Autowired
    private AvisRepository rep;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProduitRepository produitRepository;

    /**
     * Récupère tous les avis
     */
    public List<Avis> chercherTout() {
        return rep.findAll();
    }

    /**
     * Récupère un avis par son ID
     */
    public Avis chercherParId(Integer id) {
        return rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Avis introuvable"));
    }

    /**
     * Récupère les avis d'un produit
     */
    public List<Avis> chercherParProduit(Integer produitId) {
        return rep.findByProduitId(produitId);
    }

    /**
     * Récupère les avis d'un client
     */
    public List<Avis> chercherParClient(Integer clientId) {
        return rep.findByClientId(clientId);
    }

    /**
     * Calcule la note moyenne d'un produit
     */
    public Double getNoteMoyenne(Integer produitId) {
        Double moyenne = rep.findNoteMoyenneByProduitId(produitId);
        return moyenne != null ? Math.round(moyenne * 10.0) / 10.0 : 0.0;
    }

    /**
     * Ajoute un nouvel avis
     */
    @Transactional
    public Avis ajouter(Avis a) {
        if (a.getClient() == null || a.getClient().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client obligatoire");
        }
        if (a.getProduit() == null || a.getProduit().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produit obligatoire");
        }
        // Vérifier que le client existe
        Client client = clientRepository.findById(a.getClient().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Client introuvable"));
        a.setClient(client);
        
        // Vérifier que le produit existe
        Produit produit = produitRepository.findById(a.getProduit().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produit introuvable"));
        a.setProduit(produit);
        
        // Initialiser la date
        if (a.getDateAvis() == null) {
            a.setDateAvis(LocalDateTime.now());
        }
        
        // Valider la note (entre 1 et 5)
        if (a.getNote() < 1 || a.getNote() > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La note doit être entre 1 et 5");
        }
        if (a.getCommentaire() != null && a.getCommentaire().length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Commentaire trop long (500 caracteres max)");
        }
        
        return rep.save(a);
    }

    /**
     * Supprime un avis
     */
    @Transactional
    public void delete(Integer id) {
        if (!rep.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis introuvable");
        }
        rep.deleteById(id);
    }
}
