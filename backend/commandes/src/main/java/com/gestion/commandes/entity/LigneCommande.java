package com.gestion.commandes.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * Entité LigneCommande - Représente une ligne dans une commande
 * Contient un produit commandé et sa quantité
 */
@Entity
@Data
public class LigneCommande {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Quantité commandée (obligatoire, minimum 1)
    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1, message = "La quantité doit être au minimum 1")
    @Column(name = "quantite", nullable = false)
    private Integer quantite;

    // Prix unitaire au moment de la commande (obligatoire, doit être positif)
    // Permet de garder le prix même si le produit change de prix
    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix unitaire doit être positif")
    @Column(name = "prix_unitaire", nullable = false)
    private Double prixUnitaire;

    // Write-only: accepté en entrée JSON, masqué en sortie pour éviter les boucles.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ManyToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

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
