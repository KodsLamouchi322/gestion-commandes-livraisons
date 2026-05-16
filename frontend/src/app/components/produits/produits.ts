import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Categorie, Produit } from '../../models/models';

@Component({
    selector: 'app-produits',
    templateUrl: './produits.html',
    styleUrls: ['./produits.css'],
    standalone: false
})
export class Produits implements OnInit {

    produits: Produit[] = [];
    categories: Categorie[] = [];
    isLoading = true;
    isSaving = false;  // Fix bug doublon: bloque les doubles clics

    isModalOpen = false;
    editMode = false;
    current: Produit = { nom: '', prixUnitaire: 0, quantiteEnStock: 0 };
    selectedFile: File | null = null;
    uploadingId: number | null = null;

    constructor(
        private api: ApiService,
        private notificationService: NotificationService
    ) { }

    ngOnInit(): void {
        this.charger();
        this.api.getCategories().subscribe({
            next: (c) => this.categories = c,
            error: () => this.notificationService.error('Erreur chargement catégories')
        });
    }

    charger(): void {
        this.isLoading = true;
        this.api.getProduits().subscribe({
            next: (p) => {
                this.produits = [...p];
                this.isLoading = false;
            },
            error: () => {
                this.notificationService.error('Erreur lors du chargement des produits');
                this.isLoading = false;  // Fix bug écran bloqué
            }
        });
    }

    ouvrir(p?: Produit): void {
        this.editMode = !!p;
        this.current = p ? { ...p } : { nom: '', prixUnitaire: 0, quantiteEnStock: 0 };
        this.selectedFile = null;
        this.isSaving = false;
        this.isModalOpen = true;
    }

    fermer(): void {
        this.isModalOpen = false;
        this.isSaving = false;
    }

    sauvegarder(): void {
        if (this.isSaving) return;  // Bloque les appels multiples (double-clic)

        const op = this.editMode && this.current.id
            ? this.api.updateProduit(this.current.id, this.current)
            : this.api.createProduit(this.current);

        this.isSaving = true;
        op.subscribe({
            next: (saved) => {
                if (this.selectedFile && saved.id) {
                    this.api.uploadImageProduit(saved.id, this.selectedFile).subscribe({
                        next: () => {
                            this.notificationService.success('Produit enregistré avec image !');
                            this.charger();
                            this.fermer();
                        },
                        error: () => {
                            this.notificationService.error('Erreur lors de l\'upload de l\'image');
                            this.charger();
                            this.fermer();
                        }
                    });
                } else {
                    this.notificationService.success(this.editMode ? 'Produit modifié !' : 'Produit créé !');
                    this.charger();
                    this.fermer();
                }
            },
            error: () => {
                this.notificationService.error('Erreur lors de l\'enregistrement');
                this.isSaving = false;
            }
        });
    }

    onFileChange(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files?.length) this.selectedFile = input.files[0];
    }

    uploadImage(p: Produit): void {
        if (!this.selectedFile || !p.id) return;
        this.uploadingId = p.id;
        this.api.uploadImageProduit(p.id, this.selectedFile).subscribe({
            next: () => {
                this.notificationService.success('Image uploadée !');
                this.uploadingId = null;
                this.selectedFile = null;
                this.charger();
            },
            error: () => {
                this.notificationService.error('Erreur lors de l\'upload');
                this.uploadingId = null;
            }
        });
    }

    supprimer(id?: number): void {
        if (!id || !confirm('Supprimer ce produit ?')) return;
        const snapshot = [...this.produits];
        this.produits = this.produits.filter(p => p.id != id);
        this.api.deleteProduit(id).subscribe({
            next: () => {
                this.notificationService.success('Produit supprimé !');
            },
            error: () => {
                this.produits = snapshot;
                this.notificationService.error('Erreur lors de la suppression');
            }
        });
    }

    getCatNom(p: Produit): string {
        return p.categorie?.nom || '—';
    }
}
