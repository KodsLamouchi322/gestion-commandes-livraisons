package com.gestion.commandes.controller;

import com.gestion.commandes.entity.Produit;
import com.gestion.commandes.service.ProduitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller Produit - Gère les requêtes HTTP pour les produits
 * @RestController = @Controller + @ResponseBody (retourne du JSON automatiquement)
 * @RequestMapping = préfixe commun pour toutes les routes de ce controller
 */
@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    // Injection du service Produit
    @Autowired
    private ProduitService service;

    /**
     * GET /api/produits - Récupère tous les produits (avec filtres optionnels)
     * @return Liste des produits avec statut HTTP 200
     */
    @GetMapping
    public ResponseEntity<List<Produit>> getAll(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer categorieId) {
        List<Produit> produits = service.chercherAvecFiltres(q, categorieId);
        return ResponseEntity.ok(produits);
    }

    /**
     * GET /api/produits/{id} - Récupère un produit par son ID
     * @param id L'identifiant du produit (extrait de l'URL)
     * @return Le produit trouvé avec statut HTTP 200
     */
    @GetMapping("/{id}")
    public ResponseEntity<Produit> getById(@PathVariable Integer id) {
        Produit produit = service.chercherParId(id);
        return ResponseEntity.ok(produit);
    }

    /**
     * GET /api/produits/recherche?nom=xxx - Recherche des produits par nom
     * @param nom Le début du nom à rechercher (paramètre de requête)
     * @return Liste des produits correspondants avec statut HTTP 200
     */
    @GetMapping("/recherche")
    public ResponseEntity<List<Produit>> rechercherParNom(@RequestParam String nom) {
        List<Produit> produits = service.chercherParNom(nom);
        return ResponseEntity.ok(produits);
    }

    /**
     * GET /api/produits/categorie/{categorieId} - Récupère les produits d'une catégorie
     * @param categorieId L'ID de la catégorie
     * @return Liste des produits de cette catégorie avec statut HTTP 200
     */
    @GetMapping("/categorie/{categorieId}")
    public ResponseEntity<List<Produit>> getByCategorie(@PathVariable Integer categorieId) {
        List<Produit> produits = service.chercherParCategorie(categorieId);
        return ResponseEntity.ok(produits);
    }

    /**
     * POST /api/produits - Crée un nouveau produit
     * @param p Le produit à créer (extrait du corps de la requête JSON)
     * @return Le produit créé avec son ID généré et statut HTTP 200
     */
    @PostMapping
    public ResponseEntity<Produit> create(@RequestBody Produit p) {
        Produit nouveau = service.ajouter(p);
        return ResponseEntity.ok(nouveau);
    }

    /**
     * PUT /api/produits/{id} - Modifie un produit existant
     * @param id L'ID du produit à modifier
     * @param p Les nouvelles données du produit
     * @return Le produit modifié avec statut HTTP 200
     */
    @PutMapping("/{id}")
    public ResponseEntity<Produit> update(@PathVariable Integer id, @RequestBody Produit p) {
        Produit modifie = service.update(id, p);
        return ResponseEntity.ok(modifie);
    }

    /**
     * DELETE /api/produits/{id} - Supprime un produit
     * @param id L'ID du produit à supprimer
     * @return Message de confirmation avec statut HTTP 200
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Produit supprimé avec succès");
    }

    /**
     * POST /api/produits/{id}/image - Upload une image pour un produit
     * @param id L'ID du produit
     * @param file Le fichier image à uploader
     * @return Le produit mis à jour avec l'URL de l'image
     */
    @PostMapping("/{id}/image")
    public ResponseEntity<Produit> uploadImage(@PathVariable Integer id, 
                                               @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            Produit produit = service.uploadImage(id, file);
            return ResponseEntity.ok(produit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
