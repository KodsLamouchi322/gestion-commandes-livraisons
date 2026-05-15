import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Commande } from '../../models/models';

@Component({
  selector: 'app-commandes',
  templateUrl: './commandes.html',
  styleUrls: ['./commandes.css'],
  standalone: false
})
export class Commandes implements OnInit {

  commandes: Commande[] = [];
  isLoading = true;
  search = '';
  pageSize = 10;
  currentPage = 1;
  processingCommandeId?: number;

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService
  ) { }

  ngOnInit() {
    this.chargerCommandes();
  }

  chargerCommandes() {
    this.isLoading = true;
    this.apiService.getCommandes().subscribe({
      next: (data) => {
        this.commandes = data;
        this.currentPage = 1;
        this.isLoading = false;
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des commandes');
        this.isLoading = false;
      }
    });
  }

  get filteredCommandes(): Commande[] {
    const q = this.search.trim().toLowerCase();
    if (!q) return this.commandes;
    return this.commandes.filter(c =>
      String(c.id || '').includes(q)
      || (c.client?.nom || '').toLowerCase().includes(q)
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

  validerCommande(id?: number) {
    if (!id) return;
    this.processingCommandeId = id;
    this.apiService.validerCommande(id).subscribe({
      next: (updated) => {
        this.notificationService.success('Commande validée !');
        // Mise à jour locale du statut pour éviter rechargement complet
        const cmd = this.commandes.find(c => c.id === id);
        if (cmd) cmd.statut = updated.statut;
        this.processingCommandeId = undefined;
      },
      error: () => {
        this.notificationService.error('Erreur lors de la validation');
        this.processingCommandeId = undefined;
      }
    });
  }

  annulerCommande(id?: number) {
    if (!id) return;
    if (confirm('Êtes-vous sûr de vouloir annuler cette commande ?')) {
      this.processingCommandeId = id;
      this.apiService.annulerCommande(id).subscribe({
        next: (updated) => {
          this.notificationService.success('Commande annulée !');
          // Mise à jour locale du statut pour éviter rechargement complet
          const cmd = this.commandes.find(c => c.id === id);
          if (cmd) cmd.statut = updated.statut;
          this.processingCommandeId = undefined;
        },
        error: () => {
          this.notificationService.error('Erreur lors de l\'annulation');
          this.processingCommandeId = undefined;
        }
      });
    }
  }
}
