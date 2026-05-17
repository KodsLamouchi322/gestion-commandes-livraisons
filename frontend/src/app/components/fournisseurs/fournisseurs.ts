import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
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
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.isLoading = true;
    this.api.getFournisseurs().subscribe({
      next: (res) => {
        this.fournisseurs = res;
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des fournisseurs');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  ouvrirModal(f?: Fournisseur): void {
    this.isEditMode = !!f;
    this.nouveau = f ? { ...f } : { nom: '', email: '', telephone: '', adresse: '' };
    this.isModalOpen = true;
  }

  fermerModal(): void {
    this.isModalOpen = false;
    this.isSaving = false;
  }

  ajouter(): void {
    if (this.isSaving) return;
    if (!this.nouveau.nom?.trim() || !this.nouveau.email?.trim()) {
      this.notificationService.warning('Nom et email sont obligatoires');
      return;
    }
    this.isSaving = true;
    const op = this.isEditMode && this.nouveau.id
      ? this.api.updateFournisseur(this.nouveau.id, this.nouveau)
      : this.api.createFournisseur(this.nouveau);

    op.subscribe({
      next: () => {
        this.notificationService.success(this.isEditMode ? 'Fournisseur modifié' : 'Fournisseur créé');
        this.isSaving = false;
        this.charger();
        this.fermerModal();
        this.cdr.detectChanges();
      },
      error: () => {
        this.notificationService.error('Erreur lors de l\'enregistrement');
        this.isSaving = false;
        this.cdr.detectChanges();
      }
    });
  }

  supprimer(id?: number): void {
    if (!id) return;
    const snap = [...this.fournisseurs];
    this.fournisseurs = this.fournisseurs.filter(f => f.id !== id);
    this.deletingFournisseurId = id;
    this.api.deleteFournisseur(id).subscribe({
      next: () => {
        this.notificationService.success('Fournisseur supprimé');
        this.deletingFournisseurId = undefined;
        this.cdr.detectChanges();
      },
      error: () => {
        this.fournisseurs = snap;
        this.notificationService.error('Erreur lors de la suppression');
        this.deletingFournisseurId = undefined;
        this.cdr.detectChanges();
      }
    });
  }
}
