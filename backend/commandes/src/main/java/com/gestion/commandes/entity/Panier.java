package com.gestion.commandes.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité Panier - Représente le panier d'achat d'un client
 * Un client a un seul panier (relation OneToOne)
 */
@Entity
@Data
public class Panier {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Date de création du panier
    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    // Relation OneToOne : Un panier appartient à un seul client
    @OneToOne
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    private Client client;

    // Relation OneToMany : Un panier contient plusieurs lignes
    // orphanRemoval = true : supprime les lignes orphelines automatiquement
    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LignePanier> lignes = new ArrayList<>();

    /**
     * Calcule le montant total du panier
     * @return La somme des sous-totaux de toutes les lignes
     */
    public Double getTotal() {
        return lignes.stream()
                .mapToDouble(LignePanier::getSousTotal)
                .sum();
    }
}
