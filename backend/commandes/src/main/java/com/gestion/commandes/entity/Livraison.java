package com.gestion.commandes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entité Livraison - Représente une livraison pour une commande
 */
@Entity
@Data
public class Livraison {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Date de livraison prévue ou effective (obligatoire)
    @NotNull(message = "La date de livraison est obligatoire")
    @Column(name = "date_livraison", nullable = false)
    private LocalDateTime dateLivraison;

    // Adresse de livraison (obligatoire, max 255 caractères)
    @NotBlank(message = "L'adresse de livraison est obligatoire")
    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    @Column(name = "adresse", length = 255, nullable = false)
    private String adresse;

    // Coût de la livraison (obligatoire, doit être positif ou zéro)
    @NotNull(message = "Le coût de livraison est obligatoire")
    @PositiveOrZero(message = "Le coût de livraison ne peut pas être négatif")
    @Column(name = "cout", nullable = false)
    private Double cout = 0.0;

    // Statut de la livraison (obligatoire)
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutLivraison statut = StatutLivraison.EN_PREPARATION;

    // Relation OneToOne : Une livraison correspond à une commande
    @OneToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    // Relation ManyToOne : Plusieurs livraisons peuvent être gérées par un transporteur
    @ManyToOne
    @JoinColumn(name = "transporteur_id")
    private Transporteur transporteur;

    /**
     * Enum StatutLivraison - Les différents statuts d'une livraison
     */
    public enum StatutLivraison {
        EN_PREPARATION,  // Livraison en préparation
        EN_TRANSIT,      // Livraison en transit
        LIVREE           // Livraison effectuée
    }
}
