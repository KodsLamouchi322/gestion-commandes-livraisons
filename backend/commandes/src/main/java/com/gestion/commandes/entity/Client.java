package com.gestion.commandes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Entité Client - Représente un client du site
 * Un client peut passer plusieurs commandes
 */
@Entity
@Data
public class Client {

    // ID auto-généré (stratégie IDENTITY pour les entités simples)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nom du client (obligatoire, max 100 caractères)
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    @Column(name = "nom", length = 100, nullable = false)
    private String nom;

    // Prénom du client (optionnel, max 100 caractères)
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    @Column(name = "prenom", length = 100)
    private String prenom;

    // Email du client (obligatoire, unique, max 150 caractères)
    // Sert d'identifiant pour la connexion
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 150, message = "L'email ne peut pas dépasser 150 caractères")
    @Column(name = "email", length = 150, nullable = false, unique = true)
    private String email;

    // Numéro de téléphone (optionnel, max 20 caractères)
    @Size(max = 20, message = "Le téléphone ne peut pas dépasser 20 caractères")
    @Column(name = "telephone", length = 20)
    private String telephone;

    // Adresse complète du client (obligatoire, max 255 caractères)
    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255, message = "L'adresse ne peut pas dépasser 255 caractères")
    @Column(name = "adresse", length = 255, nullable = false)
    private String adresse;

    // Mot de passe du client (obligatoire, max 255 caractères hashé en BCrypt)
    @Column(name = "mot_de_passe", length = 255, nullable = false)
    @JsonIgnore  // Ne pas exposer le mot de passe dans les réponses JSON
    private String motDePasse;

    // Rôle du client (CLIENT ou ADMIN)
    // Par défaut : CLIENT
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private Role role = Role.CLIENT;

    // Relation OneToMany : Un client peut passer plusieurs commandes
    // mappedBy = "client" indique que Commande possède la clé étrangère
    // @JsonIgnore évite les boucles infinies lors de la sérialisation JSON
    @JsonIgnore
    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private List<Commande> commandes = new ArrayList<>();
}
