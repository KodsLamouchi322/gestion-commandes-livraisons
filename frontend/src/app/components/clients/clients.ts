import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Client } from '../../models/models';

@Component({
  selector: 'app-clients',
  templateUrl: './clients.html',
  styleUrls: ['./clients.css'],
  standalone: false
})
export class Clients implements OnInit {

  clients: Client[] = [];
  isLoading = true;
  isSaving = false;
  search = '';
  pageSize = 10;
  currentPage = 1;
  deletingClientId?: number;

  isModalOpen = false;
  isEditMode = false;
  nouveauClient: Partial<Client> & { motDePasse?: string } = {};

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.chargerClients();
  }

  chargerClients(): void {
    this.isLoading = true;
    this.apiService.getAdminClients().subscribe({
      next: (data) => {
        this.clients = data;
        this.currentPage = 1;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des clients');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  get filteredClients(): Client[] {
    const q = this.search.trim().toLowerCase();
    if (!q) return this.clients;
    return this.clients.filter(c =>
      String(c.id || '').includes(q) ||
      (c.nom || '').toLowerCase().includes(q) ||
      (c.email || '').toLowerCase().includes(q)
    );
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredClients.length / this.pageSize));
  }

  get paginatedClients(): Client[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredClients.slice(start, start + this.pageSize);
  }

  onSearchChange(): void {
    this.currentPage = 1;
  }

  setPage(page: number): void {
    this.currentPage = Math.min(this.totalPages, Math.max(1, page));
  }

  ouvrirModal(): void {
    this.isEditMode = false;
    this.isSaving = false;
    this.nouveauClient = {};
    this.isModalOpen = true;
  }

  ouvrirEdition(client: Client): void {
    this.isEditMode = true;
    this.isSaving = false;
    this.nouveauClient = { ...client };
    this.isModalOpen = true;
  }

  fermerModal(): void {
    this.isModalOpen = false;
    this.isSaving = false;
    this.nouveauClient = {};
  }

  ajouterClient(): void {
    if (this.isSaving) return;
    if (!this.nouveauClient.nom || !this.nouveauClient.email) {
      this.notificationService.warning('Nom et email sont obligatoires !');
      return;
    }
    this.isSaving = true;
    const op = this.isEditMode && this.nouveauClient.id
      ? this.apiService.updateAdminClient(this.nouveauClient.id, this.nouveauClient as Client)
      : this.apiService.createAdminClient(this.nouveauClient as Client);

    op.subscribe({
      next: () => {
        this.notificationService.success(this.isEditMode ? 'Client modifié !' : 'Client créé !');
        this.chargerClients();
        this.fermerModal();
      },
      error: () => {
        this.notificationService.error('Erreur lors de l\'enregistrement');
        this.isSaving = false;
        this.cdr.detectChanges();
      }
    });
  }

  supprimerClient(id?: number): void {
    if (!id) return;
    const snapshot = [...this.clients];
    this.clients = this.clients.filter(c => c.id !== id);
    this.deletingClientId = id;
    this.apiService.deleteAdminClient(id).subscribe({
      next: () => {
        this.notificationService.success('Client supprimé !');
        this.deletingClientId = undefined;
        this.cdr.detectChanges();
      },
      error: () => {
        this.clients = snapshot;
        this.notificationService.error('Erreur lors de la suppression');
        this.deletingClientId = undefined;
        this.cdr.detectChanges();
      }
    });
  }
}
