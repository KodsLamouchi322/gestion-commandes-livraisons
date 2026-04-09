package com.gestion.commandes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité Transporteur - Représente un transporteur de colis
 */
@Entity
@Data
public class Transporteur {

    // ID auto-généré (stratégie IDENTITY)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nom du transporteur (obligatoire, max 100 caractères)
    @NotBlank(message = "Le nom du transporteur est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Column(name = "nom", length = 100, nullable = false)
    private String nom;

    // Numéro de téléphone (obligatoire, max 20 caractères)
    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    @Column(name = "telephone", length = 20, nullable = false)
    private String telephone;

    // Email du transporteur (optionnel, max 150 caractères)
    @Email(message = "Email invalide")
    @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères")
    @Column(name = "email", length = 150)
    private String email;

    // Note du transporteur (0.0 à 5.0, optionnel)
    @DecimalMin(value = "0.0", message = "La note doit être au minimum 0")
    @DecimalMax(value = "5.0", message = "La note doit être au maximum 5")
    @Column(name = "note")
    private Double note;

    // Relation OneToMany : Un transporteur gère plusieurs livraisons
    @JsonIgnore
    @OneToMany(mappedBy = "transporteur", cascade = CascadeType.ALL)
    private List<Livraison> livraisons = new ArrayList<>();
}
