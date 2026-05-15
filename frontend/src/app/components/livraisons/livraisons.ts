import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Livraison } from '../../models/models';

@Component({
  selector: 'app-livraisons',
  templateUrl: './livraisons.html',
  styleUrls: ['./livraisons.css'],
  standalone: false
})
export class Livraisons implements OnInit {

  livraisons: Livraison[] = [];
  isLoading = true;

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService
  ) { }

  ngOnInit() {
    this.chargerLivraisons();
  }

  chargerLivraisons() {
    this.isLoading = true;
    this.apiService.getLivraisons().subscribe({
      next: data => {
        this.livraisons = data;
        this.isLoading = false;
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des livraisons');
        this.isLoading = false;
      }
    });
  }

  getBadgeClass(statut: string | undefined): string {
    switch (statut) {
      case 'EN_PREPARATION': return 'badge badge-warning';
      case 'EN_TRANSIT': return 'badge badge-info';
      case 'LIVREE': return 'badge badge-success';
      default: return 'badge';
    }
  }

  changerStatut(id: number | undefined, statut: string) {
    if (!id) return;
    this.apiService.updateStatutLivraison(id, statut).subscribe({
      next: () => {
        this.notificationService.success('Statut mis à jour !');
        this.chargerLivraisons();
      },
      error: () => {
        this.notificationService.error('Erreur lors de la mise à jour');
      }
    });
  }
}
