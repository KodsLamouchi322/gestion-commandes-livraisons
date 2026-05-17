import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Paiement, Livraison } from '../../models/models';
import { Subscription, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-paiements',
  templateUrl: './paiements.html',
  styleUrls: ['./paiements.css'],
  standalone: false
})
export class Paiements implements OnInit, OnDestroy {

  paiements: Paiement[] = [];
  livraisons: Map<number, Livraison> = new Map();
  isLoading = true;
  processingPaiementId?: number;

  private paiementsSub?: Subscription;
  private livraisonsSub?: Subscription;

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.chargerPaiements();
  }

  ngOnDestroy(): void {
    this.paiementsSub?.unsubscribe();
    this.livraisonsSub?.unsubscribe();
  }

  chargerPaiements(): void {
    this.paiementsSub?.unsubscribe();
    this.livraisonsSub?.unsubscribe();
    this.isLoading = true;
    this.paiementsSub = this.apiService.getPaiements().subscribe({
      next: data => {
        this.paiements = data;
        this.chargerLivraisons();
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des paiements');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerLivraisons(): void {
    this.livraisonsSub?.unsubscribe();
    const paiementsSnapshot = [...this.paiements];
    const livraisonRequests = paiementsSnapshot.map(p => {
      if (p.commandeId) {
        return this.apiService.getLivraisonByCommande(p.commandeId).pipe(
          catchError((err) => {
            console.warn(`Livraison non trouvée pour commande ${p.commandeId}:`, err);
            return of(null);
          })
        );
      }
      return of(null);
    });

    if (livraisonRequests.length === 0) {
      this.isLoading = false;
      this.cdr.detectChanges();
      return;
    }

    this.livraisonsSub = forkJoin(livraisonRequests).subscribe({
      next: (livraisons) => {
        paiementsSnapshot.forEach((p, index) => {
          const livraison = livraisons[index];
          if (livraison && p.commandeId) {
            this.livraisons.set(p.commandeId, livraison);
          }
        });
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('Erreur lors du chargement des livraisons:', err);
        this.notificationService.warning('Certaines livraisons n\'ont pas pu être chargées');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  getBadgeClass(statut: string | undefined): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'badge badge-warning';
      case 'VALIDE': return 'badge badge-success';
      case 'REFUSE': return 'badge badge-error';
      default: return 'badge';
    }
  }

  peutConfirmer(paiement: Paiement): boolean {
    if (paiement.statut !== 'EN_ATTENTE') {
      return false;
    }

    if (paiement.methodePaiement === 'CARTE') {
      return true;
    }

    if (paiement.methodePaiement === 'ESPECES') {
      const livraison = paiement.commandeId ? this.livraisons.get(paiement.commandeId) : null;
      return livraison?.statut === 'LIVREE';
    }

    return false;
  }

  estEspecesEnAttente(paiement: Paiement): boolean {
    return paiement.methodePaiement === 'ESPECES' && paiement.statut === 'EN_ATTENTE';
  }

  confirmerPaiement(id?: number): void {
    if (!id || this.processingPaiementId) return;
    this.processingPaiementId = id;
    this.apiService.confirmerPaiement(id).subscribe({
      next: () => {
        this.notificationService.success('Paiement confirmé avec succès !');
        this.processingPaiementId = undefined;
        this.chargerPaiements();
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors de la confirmation';
        this.notificationService.error(msg);
        this.processingPaiementId = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  rembourserPaiement(id?: number): void {
    if (!id || this.processingPaiementId) return;
    this.processingPaiementId = id;
    this.apiService.rembourserPaiement(id).subscribe({
      next: () => {
        this.notificationService.success('Paiement remboursé avec succès !');
        this.processingPaiementId = undefined;
        this.chargerPaiements();
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors du remboursement';
        this.notificationService.error(msg);
        this.processingPaiementId = undefined;
        this.cdr.detectChanges();
      }
    });
  }
}
