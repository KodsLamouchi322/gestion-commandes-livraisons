package com.gestion.commandes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Entité LignePanier - Représente une ligne dans le panier
 * Contient un produit et sa quantité
 */
@Entity
@Data
public class LignePanier {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Quantité du produit dans le panier (obligatoire, minimum 1)
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    // Prix unitaire au moment de l'ajout au panier
    // Permet de garder le prix même si le produit change de prix
    @Column(name = "prix_unitaire", nullable = false)
    private Double prixUnitaire;

    // Relation ManyToOne : Plusieurs lignes appartiennent à un panier
    // @JsonIgnore évite les boucles infinies lors de la sérialisation JSON
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "panier_id", nullable = false)
    private Panier panier;

    // Relation ManyToOne : Une ligne contient un produit
    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /**
     * Calcule le sous-total de cette ligne
     * @return quantite * prixUnitaire
     */
    public Double getSousTotal() {
        return quantite * prixUnitaire;
    }
}
