import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Paiement } from '../../models/models';

@Component({
  selector: 'app-paiements',
  templateUrl: './paiements.html',
  styleUrls: ['./paiements.css'],
  standalone: false
})
export class Paiements implements OnInit {

  paiements: Paiement[] = [];
  isLoading = true;
  processingPaiementId?: number;

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService
  ) { }

  ngOnInit() {
    this.chargerPaiements();
  }

  chargerPaiements() {
    this.isLoading = true;
    this.apiService.getPaiements().subscribe({
      next: data => {
        this.paiements = data;
        this.isLoading = false;
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des paiements');
        this.isLoading = false;
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

  confirmerPaiement(id?: number) {
    if (!id) return;
    this.processingPaiementId = id;
    this.apiService.confirmerPaiement(id).subscribe({
      next: () => {
        this.notificationService.success('Paiement confirmé !');
        this.chargerPaiements();
        this.processingPaiementId = undefined;
      },
      error: () => {
        this.notificationService.error('Erreur lors de la confirmation');
        this.processingPaiementId = undefined;
      }
    });
  }

  rembourserPaiement(id?: number) {
    if (!id) return;
    if (confirm('Voulez-vous procéder au remboursement ?')) {
      this.processingPaiementId = id;
      this.apiService.rembourserPaiement(id).subscribe({
        next: () => {
          this.notificationService.success('Paiement remboursé !');
          this.chargerPaiements();
          this.processingPaiementId = undefined;
        },
        error: () => {
          this.notificationService.error('Erreur lors du remboursement');
          this.processingPaiementId = undefined;
        }
      });
    }
  }
}
