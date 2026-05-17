import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Produit } from '../../models/models';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  standalone: false
})
export class Dashboard implements OnInit {

  statistiques = {
    clients: 0,
    commandesEnAttente: 0,
    commandesValidees: 0,
    livraisonsEnCours: 0,
    chiffreAffaires: 0,
    alertesStock: 0
  };

  produitsStockFaible: Produit[] = [];
  isLoading = true;
  today = new Date();
  private hasErrors = false;

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.chargerStatistiques();
  }

  get metricPeak(): number {
    return Math.max(
      this.statistiques.clients,
      this.statistiques.commandesEnAttente,
      this.statistiques.commandesValidees,
      this.statistiques.livraisonsEnCours,
      1
    );
  }

  trendPercent(value: number): number {
    return Math.min(100, Math.round((value / this.metricPeak) * 100));
  }

  revenuePercent(): number {
    return Math.min(100, Math.round((this.statistiques.chiffreAffaires / 10000) * 100));
  }

  chargerStatistiques(): void {
    this.isLoading = true;
    this.hasErrors = false;

    forkJoin({
      clients: this.apiService.getAdminClients().pipe(
        catchError((err) => {
          console.error('Erreur chargement clients:', err);
          this.hasErrors = true;
          return of([]);
        })
      ),
      commandes: this.apiService.getCommandes().pipe(
        catchError((err) => {
          console.error('Erreur chargement commandes:', err);
          this.hasErrors = true;
          return of([]);
        })
      ),
      livraisons: this.apiService.getLivraisons().pipe(
        catchError((err) => {
          console.error('Erreur chargement livraisons:', err);
          this.hasErrors = true;
          return of([]);
        })
      ),
      stock: this.apiService.getProduitsStockFaible().pipe(
        catchError((err) => {
          console.error('Erreur chargement stock:', err);
          this.hasErrors = true;
          return of([]);
        })
      )
    }).pipe(
      finalize(() => {
        this.isLoading = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (data) => {
        try {
          const clients = Array.isArray(data.clients) ? data.clients : [];
          const commandes = Array.isArray(data.commandes) ? data.commandes : [];
          const livraisons = Array.isArray(data.livraisons) ? data.livraisons : [];
          const stock = Array.isArray(data.stock) ? data.stock : [];

          this.statistiques.clients = clients.length;

          this.statistiques.commandesEnAttente = commandes.filter(c => c.statut === 'EN_ATTENTE').length;
          this.statistiques.commandesValidees = commandes.filter(
            c => c.statut === 'VALIDEE' || c.statut === 'EXPEDIEE' || c.statut === 'LIVREE'
          ).length;
          this.statistiques.chiffreAffaires = commandes
            .filter(c => c.statut !== 'ANNULEE')
            .reduce((sum, current) => sum + (current.montantTotal || 0), 0);

          this.statistiques.livraisonsEnCours = livraisons.filter(
            l => l.statut === 'EN_PREPARATION' || l.statut === 'EXPEDIEE'
          ).length;

          this.produitsStockFaible = stock;
          this.statistiques.alertesStock = stock.length;

          if (this.hasErrors) {
            this.notificationService.warning(
              'Certaines données n\'ont pas pu être chargées. Les statistiques peuvent être incomplètes.'
            );
          }
        } catch (e) {
          console.error('Erreur traitement dashboard:', e);
          this.notificationService.error('Erreur lors de l\'affichage du tableau de bord');
        }
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur chargement globale:', err);
        this.notificationService.error('Erreur lors du chargement du tableau de bord');
      }
    });
  }
}
