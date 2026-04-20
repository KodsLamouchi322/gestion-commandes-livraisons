package com.gestion.commandes.dto;

/**
 * DTO Transporteur - Réponse envoyée au frontend
 * Inclut le nombre de livraisons pour les statistiques
 */
public class TransporteurDTO {

    private Integer id;
    private String nom;
    private String telephone;
    private String email;
    private Double note;
    private long nombreLivraisons;

    // ======= Getters & Setters =======

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Double getNote() { return note; }
    public void setNote(Double note) { this.note = note; }

    public long getNombreLivraisons() { return nombreLivraisons; }
    public void setNombreLivraisons(long nombreLivraisons) { this.nombreLivraisons = nombreLivraisons; }
}
