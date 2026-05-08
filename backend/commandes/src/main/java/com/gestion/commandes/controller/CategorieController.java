package com.gestion.commandes.controller;

import com.gestion.commandes.entity.Categorie;
import com.gestion.commandes.service.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Categorie - Gère les requêtes HTTP pour les catégories
 */
@RestController
@RequestMapping("/api/categories")
public class CategorieController {

    // Injection du service Categorie
    @Autowired
    private CategorieService service;

    /**
     * GET /api/categories - Récupère toutes les catégories
     * @return Liste de toutes les catégories avec statut HTTP 200
     */
    @GetMapping
    public ResponseEntity<List<Categorie>> getAll() {
        List<Categorie> categories = service.chercherTout();
        return ResponseEntity.ok(categories);
    }

    /**
     * GET /api/categories/{id} - Récupère une catégorie par son ID
     * @param id L'identifiant de la catégorie
     * @return La catégorie trouvée avec statut HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<Categorie> getById(@PathVariable Integer id) {
        Categorie categorie = service.chercherParId(id);
        return ResponseEntity.ok(categorie);
    }

    /**
     * POST /api/categories - Crée une nouvelle catégorie
     * @param c La catégorie à créer
     * @return La catégorie créée avec son ID généré et statut HTTP 200
     */
    @PostMapping
    public ResponseEntity<Categorie> create(@RequestBody Categorie c) {
        Categorie nouvelle = service.ajouter(c);
        return ResponseEntity.ok(nouvelle);
    }

    /**
     * PUT /api/categories/{id} - Modifie une catégorie existante
     * @param id L'ID de la catégorie à modifier
     * @param c Les nouvelles données de la catégorie
     * @return La catégorie modifiée avec statut HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<Categorie> update(@PathVariable Integer id, @RequestBody Categorie c) {
        Categorie modifiee = service.update(id, c);
        return ResponseEntity.ok(modifiee);
    }

    /**
     * DELETE /api/categories/{id} - Supprime une catégorie
     * @param id L'ID de la catégorie à supprimer
     * @return Message de confirmation avec statut HTTP 200
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Catégorie supprimée avec succès");
    }
}
