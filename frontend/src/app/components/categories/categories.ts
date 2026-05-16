import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Categorie } from '../../models/models';

@Component({
    selector: 'app-categories',
    templateUrl: './categories.html',
    styleUrls: ['./categories.css'],
    standalone: false
})
export class Categories implements OnInit {

    categories: Categorie[] = [];
    isLoading = true;
    isSaving = false;  // Fix bug doublon: bloque les doubles clics
    isModalOpen = false;
    editMode = false;
    current: Categorie = { nom: '' };

    constructor(
        private api: ApiService,
        private notificationService: NotificationService
    ) { }

    ngOnInit(): void { this.charger(); }

    charger(): void {
        this.isLoading = true;
        this.api.getCategories().subscribe({
            next: (c) => { this.categories = c; this.isLoading = false; },
            error: () => {
                this.notificationService.error('Erreur lors du chargement des catégories');
                this.isLoading = false;  // Fix bug écran bloqué: évite le spinner infini
            }
        });
    }

    ouvrir(c?: Categorie): void {
        this.editMode = !!c;
        this.current = c ? { ...c } : { nom: '' };
        this.isModalOpen = true;
    }

    fermer(): void {
        this.isModalOpen = false;
        this.isSaving = false;
    }

    sauvegarder(): void {
        if (this.isSaving) return;  // Bloque les appels multiples (double-clic)
        if (!this.current.nom?.trim()) {
            this.notificationService.warning('Le nom est obligatoire');
            return;
        }
        this.isSaving = true;
        const op = this.editMode && this.current.id
            ? this.api.updateCategorie(this.current.id, this.current)
            : this.api.createCategorie(this.current);
        op.subscribe({
            next: () => {
                this.notificationService.success(this.editMode ? 'Catégorie modifiée !' : 'Catégorie créée !');
                this.charger();
                this.fermer();
            },
            error: () => {
                this.notificationService.error('Erreur lors de l\'enregistrement');
                this.isSaving = false;
            }
        });
    }

    supprimer(id?: number): void {
        if (!id || !confirm('Confirmer la suppression ?')) return;
        const snapshot = [...this.categories];
        this.categories = this.categories.filter(c => c.id != id);
        this.api.deleteCategorie(id).subscribe({
            next: () => {
                this.notificationService.success('Catégorie supprimée !');
            },
            error: () => {
                this.categories = snapshot;
                this.notificationService.error('Erreur lors de la suppression');
            }
        });
    }
}
