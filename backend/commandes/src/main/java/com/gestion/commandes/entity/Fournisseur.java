package com.gestion.commandes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité Fournisseur - Représente un fournisseur de produits
 */
@Entity
@Data
public class Fournisseur {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nom du fournisseur (obligatoire, max 100 caractères)
    @Column(name = "nom", length = 100, nullable = false)
    private String nom;

    // Email du fournisseur (obligatoire, unique, max 150 caractères)
    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email;

    // Numéro de téléphone (obligatoire, max 20 caractères)
    @Column(name = "telephone", length = 20, nullable = false)
    private String telephone;

    // Adresse du fournisseur (obligatoire, max 255 caractères)
    @Column(name = "adresse", length = 255, nullable = false)
    private String adresse;

    // Relation OneToMany : Un fournisseur peut avoir plusieurs bons de commande
    @JsonIgnore
    @OneToMany(mappedBy = "fournisseur", cascade = CascadeType.ALL)
    private List<BonCommande> bonsCommande = new ArrayList<>();
}
