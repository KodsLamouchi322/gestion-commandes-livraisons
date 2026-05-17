import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Avis } from '../../models/models';

@Component({
    selector: 'app-avis',
    templateUrl: './avis.html',
    styleUrls: ['./avis.css'],
    standalone: false
})
export class AvisAdmin implements OnInit {

    avis: Avis[] = [];
    isLoading = true;
    search = '';
    pageSize = 10;
    currentPage = 1;

    constructor(
        private api: ApiService,
        private notificationService: NotificationService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void { this.charger(); }

    charger(): void {
        this.isLoading = true;
        this.api.getAllAvis().subscribe({
            next: (a) => {
                this.avis = a;
                this.currentPage = 1;
                this.isLoading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.notificationService.error('Erreur lors du chargement des avis');
                this.isLoading = false;
                this.cdr.detectChanges();
            }
        });
    }

    get filteredAvis(): Avis[] {
        const q = this.search.trim().toLowerCase();
        if (!q) return this.avis;
        return this.avis.filter(a =>
            (a.client?.nom || '').toLowerCase().includes(q)
            || (a.produit?.nom || '').toLowerCase().includes(q)
            || String(a.id || '').includes(q)
        );
    }

    get totalPages(): number {
        return Math.max(1, Math.ceil(this.filteredAvis.length / this.pageSize));
    }

    get paginatedAvis(): Avis[] {
        const start = (this.currentPage - 1) * this.pageSize;
        return this.filteredAvis.slice(start, start + this.pageSize);
    }

    onSearchChange(): void {
        this.currentPage = 1;
    }

    setPage(page: number): void {
        this.currentPage = Math.min(this.totalPages, Math.max(1, page));
    }

    supprimer(id?: number): void {
        if (!id) return;
        const snapshot = [...this.avis];
        this.avis = this.avis.filter(a => a.id != id);
        this.api.supprimerAvis(id).subscribe({
            next: () => {
                this.notificationService.success('Avis supprimé !');
            },
            error: () => {
                this.avis = snapshot;
                this.notificationService.error('Erreur lors de la suppression');
            }
        });
    }

    etoiles(n: number): string[] {
        return Array.from({ length: 5 }, (_, i) => i < n ? '★' : '☆');
    }
}
