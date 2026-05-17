import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Produit, Fournisseur, BonCommande, LigneBonCommande } from '../../models/models';

@Component({
  selector: 'app-stock',
  templateUrl: './stock.html',
  styleUrls: ['./stock.css'],
  standalone: false
})
export class Stock implements OnInit {

  produitsStockFaible: Produit[] = [];
  fournisseurs: Fournisseur[] = [];
  isLoading = true;
  
  // Pour la création de bon de commande
  showCreateBonModal = false;
  selectedProduit?: Produit;
  selectedFournisseurId?: number;
  bonQuantite: number = 10;
  bonPrixAchat: number = 0;

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.chargerProduitsStockFaible();
    this.chargerFournisseurs();
  }

  chargerProduitsStockFaible() {
    this.isLoading = true;
    this.apiService.getProduitsStockFaible().subscribe({
      next: data => {
        this.produitsStockFaible = data;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des produits en stock faible');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerFournisseurs() {
    this.apiService.getFournisseurs().subscribe({
      next: data => {
        this.fournisseurs = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des fournisseurs');
      }
    });
  }

  ouvrirModalCreationBon(produit: Produit) {
    this.selectedProduit = produit;
    this.selectedFournisseurId = undefined;
    this.bonQuantite = 10;
    this.bonPrixAchat = produit.prixUnitaire || 0;
    this.showCreateBonModal = true;
  }

  fermerModalCreationBon() {
    this.showCreateBonModal = false;
    this.selectedProduit = undefined;
    this.selectedFournisseurId = undefined;
    this.bonQuantite = 10;
    this.bonPrixAchat = 0;
  }

  creerBonCommande() {
    if (!this.selectedProduit || !this.selectedProduit.id) {
      this.notificationService.error('Produit non sélectionné');
      return;
    }

    if (!this.selectedFournisseurId) {
      this.notificationService.error('Veuillez sélectionner un fournisseur');
      return;
    }

    if (this.bonQuantite <= 0) {
      this.notificationService.error('La quantité doit être positive');
      return;
    }

    if (this.bonPrixAchat <= 0) {
      this.notificationService.error('Le prix d\'achat doit être positif');
      return;
    }

    // Créer le bon de commande avec ses lignes
    const bonCommande: any = {
      fournisseur: { id: this.selectedFournisseurId }
    };

    const lignes: any[] = [{
      produit: { id: this.selectedProduit.id },
      quantite: this.bonQuantite,
      prixAchat: this.bonPrixAchat
    }];

    const request = {
      bonCommande: bonCommande,
      lignes: lignes
    };

    this.apiService.creerBonCommande(request).subscribe({
      next: () => {
        this.notificationService.success('Bon de commande créé avec succès !');
        this.fermerModalCreationBon();
        this.chargerProduitsStockFaible();
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors de la création du bon de commande';
        this.notificationService.error(msg);
        this.cdr.detectChanges();
      }
    });
  }

  getFournisseurNom(fournisseurId: number | undefined): string {
    if (!fournisseurId) return '—';
    const fournisseur = this.fournisseurs.find(f => f.id === fournisseurId);
    return fournisseur?.nom || '—';
  }
}
