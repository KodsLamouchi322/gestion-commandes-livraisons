package com.gestion.commandes.dto;

import com.gestion.commandes.entity.Livraison.StatutLivraison;

import java.time.LocalDateTime;

/**
 * DTO Livraison - Réponse envoyée au frontend
 */
public class LivraisonDTO {

    private Integer id;
    private LocalDateTime dateLivraison;
    private String adresse;
    private Double cout;
    private StatutLivraison statut;

    // Infos commande
    private Integer commandeId;

    // Infos transporteur
    private Integer transporteurId;
    private String transporteurNom;

    // ======= Getters & Setters =======

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getDateLivraison() { return dateLivraison; }
    public void setDateLivraison(LocalDateTime dateLivraison) { this.dateLivraison = dateLivraison; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public Double getCout() { return cout; }
    public void setCout(Double cout) { this.cout = cout; }

    public StatutLivraison getStatut() { return statut; }
    public void setStatut(StatutLivraison statut) { this.statut = statut; }

    public Integer getCommandeId() { return commandeId; }
    public void setCommandeId(Integer commandeId) { this.commandeId = commandeId; }

    public Integer getTransporteurId() { return transporteurId; }
    public void setTransporteurId(Integer transporteurId) { this.transporteurId = transporteurId; }

    public String getTransporteurNom() { return transporteurNom; }
    public void setTransporteurNom(String transporteurNom) { this.transporteurNom = transporteurNom; }
}
