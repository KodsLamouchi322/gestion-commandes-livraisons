package com.gestion.commandes.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entité Paiement - Représente un paiement pour une commande
 */
@Entity
@Data
public class Paiement {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Date du paiement (obligatoire)
    @NotNull(message = "La date du paiement est obligatoire")
    @Column(name = "date_paiement", nullable = false)
    private LocalDateTime datePaiement;

    // Montant payé (obligatoire, doit être positif)
    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit être positif")
    @Column(name = "montant", nullable = false)
    private Double montant;

    // Méthode de paiement (obligatoire)
    @NotNull(message = "La méthode de paiement est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "methode_paiement", length = 20, nullable = false)
    private MethodePaiement methodePaiement;

    // Statut du paiement (obligatoire)
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutPaiement statut = StatutPaiement.EN_ATTENTE;

    // Relation OneToOne : Un paiement correspond à une commande
    @OneToOne
    @JoinColumn(name = "commande_id", nullable = false)
    private Commande commande;

    /**
     * Enum MethodePaiement - Les différentes méthodes de paiement
     */
    public enum MethodePaiement {
        CARTE,      // Carte bancaire
        ESPECES,    // Espèces
        VIREMENT    // Virement bancaire
    }

    /**
     * Enum StatutPaiement - Les différents statuts d'un paiement
     */
    public enum StatutPaiement {
        EN_ATTENTE,  // Paiement en attente
        VALIDE,      // Paiement validé
        REFUSE       // Paiement refusé
    }
}
