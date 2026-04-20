package com.gestion.commandes.dto;

/**
 * DTO LigneCommande - Représente une ligne dans une commande
 */
public class LigneCommandeDTO {

    private Integer id;
    private Integer quantite;
    private Double prixUnitaire;
    private Double sousTotal;

    // Infos produit (pas l'objet entier pour éviter les boucles)
    private Integer produitId;
    private String produitNom;
    private Double produitPrix;

    // ======= Getters & Setters =======

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public Double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(Double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public Double getSousTotal() { return sousTotal; }
    public void setSousTotal(Double sousTotal) { this.sousTotal = sousTotal; }

    public Integer getProduitId() { return produitId; }
    public void setProduitId(Integer produitId) { this.produitId = produitId; }

    public String getProduitNom() { return produitNom; }
    public void setProduitNom(String produitNom) { this.produitNom = produitNom; }

    public Double getProduitPrix() { return produitPrix; }
    public void setProduitPrix(Double produitPrix) { this.produitPrix = produitPrix; }
}
