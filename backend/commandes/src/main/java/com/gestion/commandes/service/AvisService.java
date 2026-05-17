package com.gestion.commandes.service;

import com.gestion.commandes.entity.Avis;
import com.gestion.commandes.entity.Client;
import com.gestion.commandes.entity.Produit;
import com.gestion.commandes.repository.AvisRepository;
import com.gestion.commandes.repository.ClientRepository;
import com.gestion.commandes.repository.ProduitRepository;
import com.gestion.commandes.security.ClientUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service Avis - Gère la logique métier des avis clients sur les produits
 */
@SuppressWarnings("null")
@Service
public class AvisService {

    @Autowired
    private AvisRepository rep;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProduitRepository produitRepository;

    /**
     * Récupère tous les avis (admin uniquement)
     */
    public List<Avis> chercherTout() {
        requireAdmin();
        return rep.findAll();
    }

    /**
     * Récupère un avis par son ID (admin ou auteur de l'avis)
     */
    public Avis chercherParId(Integer id) {
        Avis avis = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Avis introuvable"));
        assertCanReadSingleAvis(avis);
        return avis;
    }

    /**
     * Récupère les avis d'un produit (public, sans contrôle d'identité)
     */
    public List<Avis> chercherParProduit(Integer produitId) {
        return rep.findByProduitId(produitId);
    }

    /**
     * Récupère les avis d'un client (admin ou le client lui-même)
     */
    public List<Avis> chercherParClient(Integer clientId) {
        assertCanAccessClientAvisList(clientId);
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
     * Ajoute un nouvel avis (client authentifié uniquement ; client imposé par la session).
     */
    @Transactional
    public Avis ajouter(Avis a) {
        Client client = requireClientActor();
        if (a.getProduit() == null || a.getProduit().getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produit obligatoire");
        }
        Integer produitId = a.getProduit().getId();

        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Produit introuvable"));
        a.setProduit(produit);
        a.setClient(client);

        if (a.getDateAvis() == null) {
            a.setDateAvis(LocalDateTime.now());
        }
        if (a.getNote() < 1 || a.getNote() > 5) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La note doit être entre 1 et 5");
        }
        if (a.getCommentaire() != null && a.getCommentaire().length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Commentaire trop long (500 caracteres max)");
        }
        if (rep.existsByClient_IdAndProduit_Id(client.getId(), produitId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vous avez deja laisse un avis pour ce produit");
        }
        return rep.save(a);
    }

    /**
     * Supprime un avis (admin ou auteur)
     */
    @Transactional
    public void delete(Integer id) {
        Avis avis = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Avis introuvable"));
        assertCanDeleteAvis(avis);
        rep.deleteById(id);
    }

    private ClientUserDetails requireAuthenticatedDetails() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof String && "anonymousUser".equals(principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        if (!(principal instanceof ClientUserDetails details)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces refuse");
        }
        return details;
    }

    private boolean isAdmin(ClientUserDetails details) {
        for (GrantedAuthority ga : details.getAuthorities()) {
            if ("ROLE_ADMIN".equals(ga.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private void requireAdmin() {
        ClientUserDetails details = requireAuthenticatedDetails();
        if (!isAdmin(details)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces administrateur requis");
        }
    }

    /**
     * Client connecté (compte rôle CLIENT), pas un admin seul.
     */
    private Client requireClientActor() {
        ClientUserDetails details = requireAuthenticatedDetails();
        if (isAdmin(details)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Les avis sont publies depuis un compte client");
        }
        boolean clientRole = details.getAuthorities().stream()
                .anyMatch(a -> "ROLE_CLIENT".equals(a.getAuthority()));
        if (!clientRole) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seuls les clients peuvent publier un avis");
        }
        Integer id = details.getClient().getId();
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client introuvable"));
    }

    private void assertCanReadSingleAvis(Avis avis) {
        ClientUserDetails details = requireAuthenticatedDetails();
        if (isAdmin(details)) {
            return;
        }
        if (!avis.getClient().getId().equals(details.getClient().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces refuse");
        }
    }

    private void assertCanAccessClientAvisList(Integer clientId) {
        ClientUserDetails details = requireAuthenticatedDetails();
        if (isAdmin(details)) {
            return;
        }
        if (!details.getClient().getId().equals(clientId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acces refuse");
        }
    }

    private void assertCanDeleteAvis(Avis avis) {
        ClientUserDetails details = requireAuthenticatedDetails();
        if (isAdmin(details)) {
            return;
        }
        if (!avis.getClient().getId().equals(details.getClient().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous ne pouvez supprimer que vos propres avis");
        }
    }
}
