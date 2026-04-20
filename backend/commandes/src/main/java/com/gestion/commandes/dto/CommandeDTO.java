package com.gestion.commandes.dto;

import com.gestion.commandes.entity.Commande.StatutCommande;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO Commande - Réponse envoyée au frontend
 * Contient les infos client (sans mot de passe) et les lignes de commande
 */
public class CommandeDTO {

    private Integer id;
    private LocalDateTime dateCommande;
    private StatutCommande statut;
    private Double montantTotal;
    private String adresseLivraison;

    // Infos client (juste l'essentiel, pas l'objet entier)
    private Integer clientId;
    private String clientNom;
    private String clientEmail;

    // Lignes de commande
    private List<LigneCommandeDTO> lignesCommande;

    // ======= Getters & Setters =======

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }

    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }

    public Double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(Double montantTotal) { this.montantTotal = montantTotal; }

    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison = adresseLivraison; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public String getClientNom() { return clientNom; }
    public void setClientNom(String clientNom) { this.clientNom = clientNom; }

    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }

    public List<LigneCommandeDTO> getLignesCommande() { return lignesCommande; }
    public void setLignesCommande(List<LigneCommandeDTO> lignesCommande) { this.lignesCommande = lignesCommande; }
}
