import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { BonCommande, Fournisseur, Produit, LigneBonCommande } from '../../models/models';

@Component({ selector: 'app-bons-commande', templateUrl: './bons-commande.html', styleUrls: ['./bons-commande.css'], standalone: false })
export class BonsCommande implements OnInit {
  bons: BonCommande[] = [];
  fournisseurs: Fournisseur[] = [];
  produits: Produit[] = [];
  
  nouveauBcFournisseurId: number = 0;
  ligneForms: Record<number, { produitId: number; quantite: number; prixAchat: number }> = {};
  isCreateModalOpen = false;
  isLigneModalOpen = false;
  selectedBcId: number | null = null;
  isSaving = false;
  isLoading = true;
  
  constructor(
    private api: ApiService,
    private notificationService: NotificationService
  ) {}
  
  ngOnInit() {
    this.charger();
    this.api.getFournisseurs().subscribe({
      next: (f) => this.fournisseurs = f,
      error: () => this.notificationService.error('Erreur chargement fournisseurs')
    });
    this.api.getProduits().subscribe({
      next: (p) => this.produits = p,
      error: () => this.notificationService.error('Erreur chargement produits')
    });
  }
  
  charger() {
    this.isLoading = true;
    this.api.getBonsCommande().subscribe({
      next: (res) => {
        this.bons = res;
        this.isLoading = false;
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des bons de commande');
        this.isLoading = false;
      }
    });
  }

  ouvrirCreateModal(): void {
    this.isCreateModalOpen = true;
    this.isSaving = false;
  }

  fermerCreateModal(): void {
    this.isCreateModalOpen = false;
    this.isSaving = false;
    this.nouveauBcFournisseurId = 0;
  }

  ouvrirLigneModal(bcId: number): void {
    this.selectedBcId = bcId;
    if (!this.ligneForms[bcId]) {
      this.ligneForms[bcId] = { produitId: 0, quantite: 1, prixAchat: 0 };
    }
    this.isLigneModalOpen = true;
    this.isSaving = false;
  }

  fermerLigneModal(): void {
    this.isLigneModalOpen = false;
    this.selectedBcId = null;
    this.isSaving = false;
  }
  
  creerBon() {
    if (this.isSaving) return;
    if (!this.nouveauBcFournisseurId) return;
    this.isSaving = true;
    this.api.createBonCommande({ fournisseur: { id: this.nouveauBcFournisseurId } } as any).subscribe({
      next: () => {
        this.notificationService.success('Bon de commande créé !');
        this.charger();
        this.fermerCreateModal();
      },
      error: () => {
        this.notificationService.error('Erreur lors de la création');
        this.isSaving = false;
      }
    });
  }
  
  ajouterLigne(bcId: number) {
    if (this.isSaving) return;
    const form = this.ligneForms[bcId] || { produitId: 0, quantite: 1, prixAchat: 0 };
    if (!form.produitId || form.quantite < 1 || form.prixAchat <= 0) {
      this.notificationService.warning('Veuillez renseigner un produit, une quantité et un prix valides.');
      return;
    }

    const nouvelleLigne: LigneBonCommande = {
      produit: { id: form.produitId } as any,
      quantite: form.quantite,
      prixAchat: form.prixAchat
    };

    this.isSaving = true;
    this.api.addLigneBonCommande(bcId, nouvelleLigne).subscribe({
      next: () => {
        this.notificationService.success('Ligne ajoutée !');
        this.ligneForms[bcId] = { produitId: 0, quantite: 1, prixAchat: 0 };
        this.charger();
        this.fermerLigneModal();
      },
      error: () => {
        this.notificationService.error('Erreur lors de l\'ajout');
        this.isSaving = false;
      }
    });
  }
  
  recevoir(bcId: number) {
    this.api.receptionnerBonCommande(bcId).subscribe({
      next: () => {
        this.notificationService.success('Bon réceptionné ! Stocks mis à jour.');
        this.charger();
      },
      error: () => {
        this.notificationService.error('Erreur lors de la réception');
      }
    });
  }
}
