import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Fournisseur } from '../../models/models';

@Component({ selector: 'app-fournisseurs', templateUrl: './fournisseurs.html', styleUrls: ['./fournisseurs.css'], standalone: false })
export class Fournisseurs implements OnInit {
  fournisseurs: Fournisseur[] = [];
  nouveau: Fournisseur = { nom: '', email: '', telephone: '', adresse: '' };
  isLoading = true;
  isSaving = false;
  isModalOpen = false;
  isEditMode = false;
  deletingFournisseurId?: number;
  
  constructor(
    private api: ApiService,
    private notificationService: NotificationService
  ) {}
  
  ngOnInit() { this.charger(); }
  
  charger() {
    this.isLoading = true;
    this.api.getFournisseurs().subscribe({
      next: (res) => {
        this.fournisseurs = res;
        this.isLoading = false;
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des fournisseurs');
        this.isLoading = false;
      }
    });
  }

  ouvrirModal(f?: Fournisseur) {
    this.isEditMode = !!f;
    this.nouveau = f ? { ...f } : { nom: '', email: '', telephone: '', adresse: '' };
    this.isModalOpen = true;
  }

  fermerModal() {
    this.isModalOpen = false;
    this.isEditMode = false;
    this.isSaving = false;
    this.nouveau = { nom: '', email: '', telephone: '', adresse: '' };
  }

  ajouter() {
    if (this.isSaving) return;
    this.isSaving = true;

    const op = this.isEditMode && this.nouveau.id
      ? this.api.updateFournisseur(this.nouveau.id, this.nouveau)
      : this.api.createFournisseur(this.nouveau);
      
    op.subscribe({
        next: () => {
            this.notificationService.success(this.isEditMode ? 'Fournisseur modifié !' : 'Fournisseur créé !');
            this.charger();
            this.fermerModal();
        },
        error: () => {
            this.notificationService.error(this.isEditMode ? 'Erreur lors de la modification' : 'Erreur lors de l\'ajout');
            this.isSaving = false;
        }
    });
  }

  supprimer(id?: number) {
    if (!id || !confirm('Supprimer ce fournisseur ?')) return;
    const snapshot = [...this.fournisseurs];
    this.fournisseurs = this.fournisseurs.filter(f => f.id != id);
    this.deletingFournisseurId = id;
    this.api.deleteFournisseur(id).subscribe({
      next: () => {
        this.notificationService.success('Fournisseur supprimé !');
        this.deletingFournisseurId = undefined;
      },
      error: () => {
        this.fournisseurs = snapshot;
        this.notificationService.error('Erreur lors de la suppression');
        this.deletingFournisseurId = undefined;
      }
    });
  }
}
