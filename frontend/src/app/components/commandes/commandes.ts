import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Commande, Livraison, Transporteur } from '../../models/models';
import { Subscription, forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Component({
  selector: 'app-commandes',
  templateUrl: './commandes.html',
  styleUrls: ['./commandes.css'],
  standalone: false
})
export class Commandes implements OnInit, OnDestroy {

  commandes: Commande[] = [];
  livraisons: Map<number, Livraison> = new Map();
  transporteurs: Transporteur[] = [];
  isLoading = true;
  search = '';
  pageSize = 10;
  currentPage = 1;
  processingCommandeId?: number;

  showCreateLivraisonModal = false;
  selectedCommandeId?: number;
  livraisonCout: number = 0;
  livraisonTransporteurId?: number;
  isProcessingLivraison = false;

  private commandesSub?: Subscription;
  private livraisonsSub?: Subscription;

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.chargerCommandes();
    this.chargerTransporteurs();
  }

  ngOnDestroy(): void {
    this.commandesSub?.unsubscribe();
    this.livraisonsSub?.unsubscribe();
  }

  chargerCommandes(): void {
    this.commandesSub?.unsubscribe();
    this.livraisonsSub?.unsubscribe();
    this.isLoading = true;
    this.commandesSub = this.apiService.getCommandes().subscribe({
      next: (data) => {
        this.commandes = data;
        this.currentPage = 1;
        this.chargerLivraisons();
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des commandes');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerLivraisons(): void {
    this.livraisonsSub?.unsubscribe();
    const commandesSnapshot = [...this.commandes];
    const livraisonRequests = commandesSnapshot.map(c => {
      if (c.id) {
        return this.apiService.getLivraisonByCommande(c.id).pipe(
          catchError((err) => {
            console.warn(`Livraison non trouvée pour commande ${c.id}:`, err);
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
        commandesSnapshot.forEach((c, index) => {
          const livraison = livraisons[index];
          if (livraison && c.id) {
            this.livraisons.set(c.id, livraison);
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

  chargerTransporteurs(): void {
    this.apiService.getTransporteurs().subscribe({
      next: data => {
        this.transporteurs = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des transporteurs');
      }
    });
  }

  get filteredCommandes(): Commande[] {
    const q = this.search.trim().toLowerCase();
    if (!q) return this.commandes;
    return this.commandes.filter(c =>
      String(c.id || '').includes(q)
      || this.getClientNom(c).toLowerCase().includes(q)
      || (c.statut || '').toLowerCase().includes(q)
    );
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredCommandes.length / this.pageSize));
  }

  get paginatedCommandes(): Commande[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredCommandes.slice(start, start + this.pageSize);
  }

  onSearchChange(): void {
    this.currentPage = 1;
  }

  setPage(page: number): void {
    this.currentPage = Math.min(this.totalPages, Math.max(1, page));
  }

  getClientNom(cmd: Commande): string {
    return cmd.clientNom || cmd.client?.nom || '—';
  }

  getBadgeClass(statut: string | undefined): string {
    switch (statut) {
      case 'EN_ATTENTE': return 'badge badge-warning';
      case 'VALIDEE': return 'badge badge-success';
      case 'EXPEDIEE': return 'badge badge-info';
      case 'LIVREE': return 'badge badge-success';
      case 'ANNULEE': return 'badge badge-error';
      default: return 'badge';
    }
  }

  getLivraison(commandeId: number | undefined): Livraison | undefined {
    return commandeId ? this.livraisons.get(commandeId) : undefined;
  }

  /** Création livraison autorisée si commande pas terminée / pas déjà une livraison (aligné backend : EN_ATTENTE ou VALIDEE). */
  peutCreerLivraison(cmd: Commande): boolean {
    if (!cmd.id || this.getLivraison(cmd.id)) {
      return false;
    }
    const s = cmd.statut;
    return s === 'EN_ATTENTE' || s === 'VALIDEE';
  }

  resumePaiement(cmd: Commande): string {
    const p = cmd.paiement;
    if (!p?.methodePaiement && !p?.statut) {
      return '—';
    }
    const m = p.methodePaiement ?? '—';
    const st = p.statut ?? '—';
    return `${m} · ${st}`;
  }

  validerCommande(id?: number): void {
    if (!id) return;
    this.processingCommandeId = id;
    this.apiService.validerCommande(id).subscribe({
      next: (updated) => {
        this.notificationService.success('Commande validée !');
        const cmd = this.commandes.find(c => c.id === id);
        if (cmd) cmd.statut = updated.statut;
        this.processingCommandeId = undefined;
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de la validation';
        this.notificationService.error(msg);
        this.processingCommandeId = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  annulerCommande(id?: number): void {
    if (!id) return;
    this.processingCommandeId = id;
    this.apiService.annulerCommande(id).subscribe({
      next: (updated) => {
        this.notificationService.success('Commande annulée !');
        const cmd = this.commandes.find(c => c.id === id);
        if (cmd) cmd.statut = updated.statut;
        this.processingCommandeId = undefined;
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err.error?.message || 'Erreur lors de l\'annulation';
        this.notificationService.error(msg);
        this.processingCommandeId = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  ouvrirModalCreationLivraison(commandeId: number | undefined): void {
    if (!commandeId) return;
    this.selectedCommandeId = commandeId;
    this.livraisonCout = 0;
    this.livraisonTransporteurId = undefined;
    this.showCreateLivraisonModal = true;
  }

  fermerModalCreationLivraison(event?: Event): void {
    if (event && (event.target as HTMLElement).className !== 'modal-backdrop' && !(event.target as HTMLElement).classList?.contains('close-btn') && !(event.target as HTMLElement).classList?.contains('btn-outline')) {
      return;
    }
    this.showCreateLivraisonModal = false;
    this.selectedCommandeId = undefined;
    this.livraisonCout = 0;
    this.livraisonTransporteurId = undefined;
    this.isProcessingLivraison = false;
  }

  creerLivraison(): void {
    if (this.isProcessingLivraison) return;
    if (!this.selectedCommandeId) {
      this.notificationService.error('Commande non sélectionnée');
      return;
    }

    if (this.livraisonCout < 0) {
      this.notificationService.error('Le coût doit être positif');
      return;
    }

    this.isProcessingLivraison = true;
    try {
      this.apiService.creerLivraisonDepuisCommande(
        this.selectedCommandeId,
        this.livraisonCout,
        this.livraisonTransporteurId
      ).subscribe({
        next: (livraison) => {
          this.notificationService.success('Livraison créée avec succès !');
          if (this.selectedCommandeId) {
            this.livraisons.set(this.selectedCommandeId, livraison);
          }
          this.isProcessingLivraison = false;
          this.fermerModalCreationLivraison();
          this.chargerCommandes();
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.isProcessingLivraison = false;
          console.error('HTTP Error in creerLivraison:', err);
          const msg = err?.error?.message || err?.message || 'Erreur lors de la création de la livraison';
          this.notificationService.error(msg);
          this.cdr.detectChanges();
        }
      });
    } catch (e) {
      this.isProcessingLivraison = false;
      console.error('Sync Error in creerLivraison:', e);
      this.notificationService.error('Erreur inattendue');
      this.cdr.detectChanges();
    }
  }
}
