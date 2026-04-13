package com.gestion.commandes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

/**
 * Entité LigneBonCommande - Représente une ligne dans un bon de commande
 * Contient un produit à commander et sa quantité
 */
@Entity
@Data
public class LigneBonCommande {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Quantité à commander (obligatoire)
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    // Prix d'achat unitaire (obligatoire)
    @Column(name = "prix_achat", nullable = false)
    private Double prixAchat;

    // Relation ManyToOne : Plusieurs lignes appartiennent à un bon de commande
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "bon_commande_id", nullable = false)
    private BonCommande bonCommande;

    // Relation ManyToOne : Une ligne concerne un produit
    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    /**
     * Calcule le sous-total de cette ligne
     * @return quantite * prixAchat
     */
    public Double getSousTotal() {
        return quantite * prixAchat;
    }
}
