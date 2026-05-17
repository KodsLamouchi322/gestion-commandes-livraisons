package com.gestion.commandes.service;

import com.gestion.commandes.entity.Categorie;
import com.gestion.commandes.entity.Produit;
import com.gestion.commandes.repository.AvisRepository;
import com.gestion.commandes.repository.CategorieRepository;
import com.gestion.commandes.repository.LigneBonCommandeRepository;
import com.gestion.commandes.repository.LigneCommandeRepository;
import com.gestion.commandes.repository.LignePanierRepository;
import com.gestion.commandes.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service Produit - Gère la logique métier des produits
 * Utilise @Autowired pour l'injection de dépendances (style enseignant)
 * Toutes les méthodes sont publiques pour être appelées par le controller
 */
@SuppressWarnings("null")
@Service
public class ProduitService {

    // Injection du repository Produit
    @Autowired
    private ProduitRepository rep;

    // Injection du repository Categorie
    @Autowired
    private CategorieRepository categorieRepository;

    // Injection des repositories depéndants pour la suppression en cascade
    @Autowired
    private AvisRepository avisRepository;

    @Autowired
    private LigneCommandeRepository ligneCommandeRepository;

    @Autowired
    private LignePanierRepository lignePanierRepository;

    @Autowired
    private LigneBonCommandeRepository ligneBonCommandeRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Récupère tous les produits de la base de données
     * @return Liste de tous les produits
     */
    public List<Produit> chercherTout() {
        List<Produit> list = rep.findAll();
        enrichirNotesMoyennes(list);
        return list;
    }

    /**
     * Recherche des produits avec filtres optionnels
     */
    public List<Produit> chercherAvecFiltres(String q, Integer categorieId) {
        List<Produit> produits = rep.findAll();
        if (q != null && !q.trim().isEmpty()) {
            produits = produits.stream()
                    .filter(p -> p.getNom() != null && p.getNom().toLowerCase().contains(q.trim().toLowerCase()))
                    .toList(); // java.util.stream.Collectors.toList() for java 8+ or .toList() for 16+
        }
        if (categorieId != null) {
            produits = produits.stream()
                    .filter(p -> p.getCategorie() != null && p.getCategorie().getId().equals(categorieId))
                    .toList();
        }
        enrichirNotesMoyennes(produits);
        return produits;
    }

    /**
     * Récupère un produit par son ID
     * @param id L'identifiant du produit
     * @return Le produit trouvé
     * @throws ResponseStatusException si le produit n'existe pas
     */
    public Produit chercherParId(Integer id) {
        Produit p = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produit introuvable avec l'ID : " + id));
        enrichirNotesMoyennes(List.of(p));
        return p;
    }

    /**
     * Recherche des produits par nom (commence par...)
     * @param nom Le début du nom à rechercher
     * @return Liste des produits correspondants
     */
    public List<Produit> chercherParNom(String nom) {
        List<Produit> list = rep.findByNomStartingWith(nom);
        enrichirNotesMoyennes(list);
        return list;
    }

    /**
     * Recherche des produits par catégorie
     * @param categorieId L'ID de la catégorie
     * @return Liste des produits de cette catégorie
     */
    public List<Produit> chercherParCategorie(Integer categorieId) {
        List<Produit> list = rep.findByCategorieId(categorieId);
        enrichirNotesMoyennes(list);
        return list;
    }

    /**
     * Renseigne {@link Produit#setNoteMoyenne(Double)} pour chaque produit (requête groupée, pas N+1).
     */
    private void enrichirNotesMoyennes(List<Produit> produits) {
        if (produits == null || produits.isEmpty()) {
            return;
        }
        List<Object[]> rows = avisRepository.findAverageNoteGroupByProduitId();
        Map<Integer, Double> moyennes = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null || row[1] == null) {
                continue;
            }
            Integer pid = ((Number) row[0]).intValue();
            double raw = ((Number) row[1]).doubleValue();
            moyennes.put(pid, Math.round(raw * 10.0) / 10.0);
        }
        for (Produit p : produits) {
            if (p.getId() == null) {
                continue;
            }
            Double m = moyennes.get(p.getId());
            p.setNoteMoyenne(m != null && m > 0 ? m : null);
        }
    }

    /**
     * Ajoute un nouveau produit dans la base de données
     * @param p Le produit à ajouter
     * @return Le produit ajouté avec son ID généré
     */
    @Transactional
    public Produit ajouter(Produit p) {
        // Si une catégorie est spécifiée, on vérifie qu'elle existe
        if (p.getCategorie() != null && p.getCategorie().getId() != null) {
            Categorie cat = categorieRepository.findById(p.getCategorie().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, 
                            "Catégorie introuvable"));
            p.setCategorie(cat);
        }
        Produit saved = rep.save(p);
        enrichirNotesMoyennes(List.of(saved));
        return saved;
    }

    /**
     * Modifie un produit existant
     * @param id L'ID du produit à modifier
     * @param p Les nouvelles données du produit
     * @return Le produit modifié
     * @throws ResponseStatusException si le produit n'existe pas
     */
    @Transactional
    public Produit update(Integer id, Produit p) {
        // On récupère le produit existant
        Produit existant = rep.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, 
                        "Produit introuvable avec l'ID : " + id));

        // On met à jour les champs un par un
        existant.setNom(p.getNom());
        existant.setDescription(p.getDescription());
        existant.setPrixUnitaire(p.getPrixUnitaire());
        existant.setQuantiteEnStock(p.getQuantiteEnStock());
        existant.setImageUrl(p.getImageUrl());

        // Si une catégorie est spécifiée, on la met à jour
        if (p.getCategorie() != null && p.getCategorie().getId() != null) {
            Categorie cat = categorieRepository.findById(p.getCategorie().getId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, 
                            "Catégorie introuvable"));
            existant.setCategorie(cat);
        }

        Produit saved = rep.save(existant);
        enrichirNotesMoyennes(List.of(saved));
        return saved;
    }

    /**
     * Supprime un produit et toutes ses dépendances (avis, lignes panier, lignes commande, lignes bon commande)
     * @param id L'ID du produit à supprimer
     * @throws ResponseStatusException si le produit n'existe pas
     */
    @Transactional
    public void delete(Integer id) {
        // 1. Vérifier que le produit existe
        if (!rep.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Produit introuvable avec l'ID : " + id);
        }

        // 2. Supprimer les avis associés
        avisRepository.deleteAll(avisRepository.findByProduitId(id));

        // 3. Supprimer les lignes de panier associées
        lignePanierRepository.deleteAll(lignePanierRepository.findByProduitId(id.longValue()));

        // 4. Supprimer les lignes de commande associées
        ligneCommandeRepository.deleteAll(ligneCommandeRepository.findByProduitId(id));

        // 5. Supprimer les lignes de bon de commande associées
        ligneBonCommandeRepository.deleteAll(ligneBonCommandeRepository.findByProduitId(id));

        // 6. Enfin, supprimer le produit lui-même
        rep.deleteById(id);
    }

    /**
     * Upload une image pour un produit
     * @param id L'ID du produit
     * @param file Le fichier image
     * @return Le produit mis à jour avec l'URL de l'image
     */
    @Transactional
    public Produit uploadImage(Integer id, MultipartFile file) throws IOException {
        Produit produit = chercherParId(id);
        
        // Créer le dossier uploads s'il n'existe pas
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
            : "";
        String filename = "produit_" + id + "_" + UUID.randomUUID() + extension;
        
        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Mettre à jour l'URL de l'image dans le produit
        produit.setImageUrl("/uploads/" + filename);
        Produit saved = rep.save(produit);
        enrichirNotesMoyennes(List.of(saved));
        return saved;
    }
}
