package com.gestion.commandes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité BonCommande - Représente un bon de commande passé à un fournisseur
 */
@Entity
@Data
public class BonCommande {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Date de création du bon de commande (obligatoire)
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    // Statut du bon de commande (obligatoire)
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", length = 20, nullable = false)
    private Statut statut = Statut.EN_ATTENTE;

    // Relation ManyToOne : Plusieurs bons de commande peuvent être passés à un fournisseur
    @ManyToOne
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    // Relation OneToMany : Un bon de commande contient plusieurs lignes
    @JsonIgnore
    @OneToMany(mappedBy = "bonCommande", cascade = CascadeType.ALL)
    private List<LigneBonCommande> lignes = new ArrayList<>();

    /**
     * Enum Statut - Les différents statuts d'un bon de commande
     */
    public enum Statut {
        EN_ATTENTE,  // Bon de commande créé, en attente d'envoi
        ENVOYE,      // Bon de commande envoyé au fournisseur
        RECU,        // Marchandise reçue
        ANNULE       // Bon de commande annulé
    }
}
