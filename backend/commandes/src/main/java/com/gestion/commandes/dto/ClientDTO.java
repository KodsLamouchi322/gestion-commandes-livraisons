package com.gestion.commandes.dto;

import com.gestion.commandes.entity.Role;

/**
 * DTO Client - Réponse envoyée au frontend
 * Le mot de passe n'est JAMAIS inclus dans ce DTO
 */
public class ClientDTO {

    private Integer id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private Role role;

    // ======= Getters & Setters =======

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
