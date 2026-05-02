package com.gestion.commandes.service;

import com.gestion.commandes.entity.Categorie;
import com.gestion.commandes.repository.CategorieRepository;
import com.gestion.commandes.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Service Categorie - Gère la logique métier des catégories
 */
@Service
public class CategorieService {

    // Injection du repository Categorie
    @Autowired
    private CategorieRepository rep;
    @Autowired
    private ProduitRepository produitRepository;

    /**
     * Récupère toutes les catégories
     * @return Liste de toutes les catégories
     */
    public List<Categorie> chercherTout() {
        return rep.findAll();
    }

    /**
     * Récupère une catégorie par son ID
     * @param id L'identifiant de la catégorie
     * @return La catégorie trouvée
     * @throws ResponseStatusException si la catégorie n'existe pas
     */
    public Categorie chercherParId(Integer id) {
        return rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Catégorie introuvable avec l'ID : " + id));
    }

    /**
     * Ajoute une nouvelle catégorie
     * @param c La catégorie à ajouter
     * @return La catégorie ajoutée avec son ID généré
     * @throws ResponseStatusException si une catégorie avec ce nom existe déjà
     */
    @Transactional
    public Categorie ajouter(Categorie c) {
        if (c.getNom() == null || c.getNom().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom de categorie est obligatoire");
        }
        // Vérifier si une catégorie avec ce nom existe déjà
        if (rep.existsByNom(c.getNom().trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Une catégorie avec ce nom existe déjà");
        }
        c.setNom(c.getNom().trim());
        return rep.save(c);
    }

    /**
     * Modifie une catégorie existante
     * @param id L'ID de la catégorie à modifier
     * @param c Les nouvelles données de la catégorie
     * @return La catégorie modifiée
     * @throws ResponseStatusException si la catégorie n'existe pas
     */
    @Transactional
    public Categorie update(Integer id, Categorie c) {
        if (c.getNom() == null || c.getNom().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom de categorie est obligatoire");
        }
        // On récupère la catégorie existante
        Categorie existante = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Catégorie introuvable avec l'ID : " + id));

        if (!existante.getNom().equalsIgnoreCase(c.getNom().trim()) && rep.existsByNom(c.getNom().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une catégorie avec ce nom existe déjà");
        }
        // On met à jour les champs
        existante.setNom(c.getNom().trim());
        existante.setDescription(c.getDescription());

        // On sauvegarde les modifications
        return rep.save(existante);
    }

    /**
     * Supprime une catégorie
     * @param id L'ID de la catégorie à supprimer
     * @throws ResponseStatusException si la catégorie n'existe pas
     */
    @Transactional
    public void delete(Integer id) {
        // On vérifie que la catégorie existe avant de la supprimer
        if (!rep.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Catégorie introuvable avec l'ID : " + id);
        }
        if (produitRepository.countByCategorieId(id) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Suppression impossible: des produits sont encore lies a cette categorie");
        }
        rep.deleteById(id);
    }
}
