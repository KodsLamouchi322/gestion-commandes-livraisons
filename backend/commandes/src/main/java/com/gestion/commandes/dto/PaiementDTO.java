package com.gestion.commandes.dto;

import com.gestion.commandes.entity.Paiement.MethodePaiement;
import com.gestion.commandes.entity.Paiement.StatutPaiement;

import java.time.LocalDateTime;

/**
 * DTO Paiement - Réponse envoyée au frontend
 */
public class PaiementDTO {

    private Integer id;
    private LocalDateTime datePaiement;
    private Double montant;
    private MethodePaiement methodePaiement;
    private StatutPaiement statut;

    // Infos commande
    private Integer commandeId;

    // ======= Getters & Setters =======

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getDatePaiement() { return datePaiement; }
    public void setDatePaiement(LocalDateTime datePaiement) { this.datePaiement = datePaiement; }

    public Double getMontant() { return montant; }
    public void setMontant(Double montant) { this.montant = montant; }

    public MethodePaiement getMethodePaiement() { return methodePaiement; }
    public void setMethodePaiement(MethodePaiement methodePaiement) { this.methodePaiement = methodePaiement; }

    public StatutPaiement getStatut() { return statut; }
    public void setStatut(StatutPaiement statut) { this.statut = statut; }

    public Integer getCommandeId() { return commandeId; }
    public void setCommandeId(Integer commandeId) { this.commandeId = commandeId; }
}
