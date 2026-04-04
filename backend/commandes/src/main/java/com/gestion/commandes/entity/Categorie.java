package com.gestion.commandes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité Categorie - Représente une catégorie de produits
 * Exemple : Électronique, Vêtements, Alimentation, etc.
 */
@Entity
@Data
public class Categorie {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nom de la catégorie (obligatoire, unique, max 100 caractères)
    @Column(name = "nom", length = 100, nullable = false, unique = true)
    private String nom;

    // Description de la catégorie (optionnel, max 500 caractères)
    @Column(name = "description", length = 500)
    private String description;

    // Relation OneToMany : Une catégorie peut contenir plusieurs produits
    // mappedBy = "categorie" indique que Produit possède la clé étrangère
    // @JsonIgnore évite les boucles infinies lors de la sérialisation JSON
    @JsonIgnore
    @OneToMany(mappedBy = "categorie")
    private List<Produit> produits = new ArrayList<>();
}
