package com.gestion.commandes.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entité Avis - Représente un avis client sur un produit
 */
@Entity
@Data
public class Avis {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Note donnée par le client (obligatoire, entre 1 et 5)
    @Column(name = "note", nullable = false)
    private Integer note;

    // Commentaire du client (optionnel, max 500 caractères)
    @Column(name = "commentaire", length = 500)
    private String commentaire;

    // Date de l'avis (obligatoire)
    @Column(name = "date_avis", nullable = false)
    private LocalDateTime dateAvis;

    // Relation ManyToOne : Plusieurs avis peuvent être donnés par un client
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // Relation ManyToOne : Plusieurs avis peuvent concerner un produit
    @ManyToOne
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;
}
