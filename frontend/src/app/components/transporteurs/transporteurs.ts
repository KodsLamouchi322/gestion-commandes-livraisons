import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Transporteur } from '../../models/models';

@Component({
  selector: 'app-transporteurs',
  templateUrl: './transporteurs.html',
  styleUrls: ['./transporteurs.css'],
  standalone: false
})
export class Transporteurs implements OnInit {

  transporteurs: Transporteur[] = [];
  isLoading = true;
  isSaving = false;
  deletingTransporteurId?: number;

  nouveauTransporteur: Transporteur = { nom: '', telephone: '', note: 5 };
  isModalOpen = false;
  isEditMode = false;

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService
  ) { }

  ngOnInit() {
    this.chargerTransporteurs();
  }

  chargerTransporteurs() {
    this.isLoading = true;
    this.apiService.getTransporteurs().subscribe({
      next: (data) => {
        this.transporteurs = data;
        this.isLoading = false;
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des transporteurs');
        this.isLoading = false;
      }
    });
  }

  ouvrirModal() {
    this.isEditMode = false;
    this.isSaving = false;
    this.nouveauTransporteur = { nom: '', telephone: '', note: 5 };
    this.isModalOpen = true;
  }

  ouvrirEdition(transporteur: Transporteur) {
    this.isEditMode = true;
    this.isSaving = false;
    this.nouveauTransporteur = { ...transporteur };
    this.isModalOpen = true;
  }

  fermerModal() {
    this.isModalOpen = false;
    this.isEditMode = false;
    this.isSaving = false;
    this.nouveauTransporteur = { nom: '', telephone: '', note: 5 };
  }

  ajouterTransporteur() {
    if (this.isSaving) return;
    if (!this.nouveauTransporteur.nom || !this.nouveauTransporteur.telephone) {
      this.notificationService.warning('Veuillez remplir le nom et le téléphone !');
      return;
    }
    this.isSaving = true;
    const request = this.isEditMode && this.nouveauTransporteur.id
      ? this.apiService.updateTransporteur(this.nouveauTransporteur.id, this.nouveauTransporteur)
      : this.apiService.createTransporteur(this.nouveauTransporteur);
    request.subscribe({
      next: () => {
        this.notificationService.success(this.isEditMode ? 'Transporteur modifié !' : 'Transporteur créé !');
        this.chargerTransporteurs();
        this.fermerModal();
      },
      error: () => {
        this.notificationService.error('Erreur lors de l\'ajout');
        this.isSaving = false;
      }
    });
  }

  supprimerTransporteur(id?: number) {
    if (!id) return;
    if (confirm('Voulez-vous révoquer ce transporteur ?')) {
      const snapshot = [...this.transporteurs];
      this.transporteurs = this.transporteurs.filter(t => t.id != id);
      this.deletingTransporteurId = id;
      this.apiService.deleteTransporteur(id).subscribe({
        next: () => {
          this.notificationService.success('Transporteur supprimé !');
          this.deletingTransporteurId = undefined;
        },
        error: () => {
          this.transporteurs = snapshot;
          this.notificationService.error('Erreur lors de la suppression');
          this.deletingTransporteurId = undefined;
        }
      });
    }
  }
}
