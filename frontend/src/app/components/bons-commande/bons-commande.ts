import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { BonCommande, Fournisseur, Produit, LigneBonCommande } from '../../models/models';

@Component({ 
  selector: 'app-bons-commande', 
  templateUrl: './bons-commande.html', 
  styleUrls: ['./bons-commande.css'], 
  standalone: false 
})
export class BonsCommande implements OnInit {
  bons: BonCommande[] = [];
  fournisseurs: Fournisseur[] = [];
  produits: Produit[] = [];
  
  // Pour la création de bon de commande
  isCreateModalOpen = false;
  nouveauBcFournisseurId: number = 0;
  lignesNouveau: Array<{ produitId: number; quantite: number; prixAchat: number }> = [];
  
  isLoading = true;
  processingBonId?: number;
  
  constructor(
    private api: ApiService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}
  
  ngOnInit() {
    this.charger();
    this.api.getFournisseurs().subscribe({
      next: (f) => {
        this.fournisseurs = f;
        this.cdr.detectChanges();
      },
      error: () => this.notificationService.error('Erreur chargement fournisseurs')
    });
    this.api.getProduits().subscribe({
      next: (p) => {
        this.produits = p;
        this.cdr.detectChanges();
      },
      error: () => this.notificationService.error('Erreur chargement produits')
    });
  }
  
  charger() {
    this.isLoading = true;
    this.api.getBonsCommande().subscribe({
      next: (res) => {
        this.bons = res;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des bons de commande');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  getBadgeClass(statut: string | undefined): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'badge badge-warning';
      case 'ENVOYE': return 'badge badge-info';
      case 'RECU': return 'badge badge-success';
      case 'ANNULE': return 'badge badge-error';
      default: return 'badge';
    }
  }

  ouvrirCreateModal(): void {
    this.isCreateModalOpen = true;
    this.nouveauBcFournisseurId = 0;
    this.lignesNouveau = [{ produitId: 0, quantite: 1, prixAchat: 0 }];
  }

  fermerCreateModal(): void {
    this.isCreateModalOpen = false;
    this.nouveauBcFournisseurId = 0;
    this.lignesNouveau = [];
  }

  ajouterLigneNouveau(): void {
    this.lignesNouveau.push({ produitId: 0, quantite: 1, prixAchat: 0 });
  }

  supprimerLigneNouveau(index: number): void {
    this.lignesNouveau.splice(index, 1);
  }

  calculerMontantTotal(): number {
    return this.lignesNouveau.reduce((total, ligne) => {
      return total + (ligne.quantite * ligne.prixAchat);
    }, 0);
  }
  
  creerBon() {
    if (!this.nouveauBcFournisseurId) {
      this.notificationService.error('Veuillez sélectionner un fournisseur');
      return;
    }

    if (this.lignesNouveau.length === 0) {
      this.notificationService.error('Veuillez ajouter au moins une ligne');
      return;
    }

    // Valider les lignes
    for (const ligne of this.lignesNouveau) {
      if (!ligne.produitId || ligne.quantite <= 0 || ligne.prixAchat <= 0) {
        this.notificationService.error('Toutes les lignes doivent avoir un produit, une quantité et un prix valides');
        return;
      }
    }

    const request = {
      bonCommande: {
        fournisseur: { id: this.nouveauBcFournisseurId }
      },
      lignes: this.lignesNouveau.map(l => ({
        produit: { id: l.produitId },
        quantite: l.quantite,
        prixAchat: l.prixAchat
      }))
    };

    this.api.creerBonCommande(request).subscribe({
      next: () => {
        this.notificationService.success('Bon de commande créé !');
        this.charger();
        this.fermerCreateModal();
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors de la création';
        this.notificationService.error(msg);
        this.cdr.detectChanges();
      }
    });
  }

  envoyer(id: number | undefined) {
    if (!id || this.processingBonId) return;
    this.processingBonId = id;
    this.api.envoyerBonCommande(id).subscribe({
      next: () => {
        this.notificationService.success('Bon de commande envoyé au fournisseur !');
        this.processingBonId = undefined;
        this.charger();
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors de l\'envoi';
        this.notificationService.error(msg);
        this.processingBonId = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  recevoir(id: number | undefined) {
    if (!id || this.processingBonId) return;
    this.processingBonId = id;
    this.api.recevoirBonCommande(id).subscribe({
      next: () => {
        this.notificationService.success('Bon réceptionné ! Stocks mis à jour.');
        this.processingBonId = undefined;
        this.charger();
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors de la réception';
        this.notificationService.error(msg);
        this.processingBonId = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  annuler(id: number | undefined) {
    if (!id || this.processingBonId) return;
    this.processingBonId = id;
    this.api.annulerBonCommande(id).subscribe({
      next: () => {
        this.notificationService.success('Bon de commande annulé !');
        this.processingBonId = undefined;
        this.charger();
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors de l\'annulation';
        this.notificationService.error(msg);
        this.processingBonId = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  getFournisseurNom(fournisseurId: number | undefined): string {
    if (!fournisseurId) return '—';
    const fournisseur = this.fournisseurs.find(f => f.id === fournisseurId);
    return fournisseur?.nom || '—';
  }

  getProduitNom(produitId: number): string {
    const produit = this.produits.find(p => p.id === produitId);
    return produit?.nom || '—';
  }
}
