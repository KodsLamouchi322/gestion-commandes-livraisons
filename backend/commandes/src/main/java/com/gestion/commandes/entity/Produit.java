package com.gestion.commandes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité Produit - Représente un produit dans le catalogue
 * Utilise @Data de Lombok pour générer automatiquement :
 * - getters/setters
 * - toString()
 * - equals() et hashCode()
 */
@Entity
@Data
public class Produit {

    // ID auto-généré (stratégie IDENTITY pour les entités simples)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nom du produit (obligatoire, max 100 caractères)
    @Column(name = "nom", length = 100, nullable = false)
    private String nom;

    // Description du produit (optionnel, max 500 caractères)
    @Column(name = "description", length = 500)
    private String description;

    // Prix unitaire du produit (obligatoire)
    @Column(name = "prix_unitaire", nullable = false)
    private Double prixUnitaire;

    // Quantité disponible en stock (obligatoire, par défaut 0)
    @Column(name = "quantite_en_stock", nullable = false)
    private Integer quantiteEnStock = 0;

    // URL de l'image du produit (optionnel)
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    // Relation ManyToOne : Un produit appartient à une catégorie
    // FetchType.EAGER = charge la catégorie automatiquement avec le produit
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    // Relation OneToMany : Un produit peut avoir plusieurs lignes de bon de commande
    // mappedBy = "produit" indique que LigneBonCommande possède la clé étrangère
    // @JsonIgnore évite les boucles infinies lors de la sérialisation JSON
    @JsonIgnore
    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL)
    private List<LigneBonCommande> lignesBonCommande = new ArrayList<>();

    // Relation OneToMany : Un produit peut avoir plusieurs avis
    @JsonIgnore
    @OneToMany(mappedBy = "produit", cascade = CascadeType.ALL)
    private List<Avis> avis = new ArrayList<>();

    /** Note moyenne (1–5), calculée à la volée — non persistée, exposée au JSON catalogue. */
    @Transient
    private Double noteMoyenne;
}
