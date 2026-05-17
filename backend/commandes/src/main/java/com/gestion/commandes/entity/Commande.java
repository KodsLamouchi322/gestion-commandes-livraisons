package com.gestion.commandes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Commande - Représente une commande passée par un client
 */
@Entity
@Data
public class Commande {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Date de la commande (obligatoire)
    @Column(name = "date_commande", nullable = false)
    private LocalDateTime dateCommande;

    // Statut de la commande (obligatoire)
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private StatutCommande statut = StatutCommande.EN_ATTENTE;

    // Montant total de la commande (obligatoire, doit être positif)
    @NotNull(message = "Le montant total est obligatoire")
    @Positive(message = "Le montant total doit être positif")
    @Column(name = "montant_total", nullable = false)
    private Double montantTotal;

    // Adresse de livraison (obligatoire, max 255 caractères)
    @NotBlank(message = "L'adresse de livraison est obligatoire")
    @Size(max = 255, message = "L'adresse de livraison ne peut pas dépasser 255 caractères")
    @Column(name = "adresse_livraison", length = 255, nullable = false)
    private String adresseLivraison;

    // Relation ManyToOne : Plusieurs commandes appartiennent à un client
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Relation OneToMany : Une commande contient plusieurs lignes
    @JsonIgnore
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<LigneCommande> lignesCommande = new ArrayList<>();

    // Relation OneToOne : Une commande a un paiement
    @JsonIgnore
    @OneToOne(mappedBy = "commande")
    private Paiement paiement;

    // Relation OneToOne : Une commande a une livraison
    @JsonIgnore
    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private Livraison livraison;

    /**
     * Enum StatutCommande - Les différents statuts d'une commande
     */
    public enum StatutCommande {
        EN_ATTENTE,    // Commande créée, en attente de paiement
        VALIDEE,       // Paiement validé
        EXPEDIEE,      // Commande expédiée
        LIVREE,        // Commande livrée
        ANNULEE        // Commande annulée
    }
}
