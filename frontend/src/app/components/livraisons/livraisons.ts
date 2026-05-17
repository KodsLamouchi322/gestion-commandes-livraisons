import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';
import { Livraison, Transporteur } from '../../models/models';

@Component({
  selector: 'app-livraisons',
  templateUrl: './livraisons.html',
  styleUrls: ['./livraisons.css'],
  standalone: false
})
export class Livraisons implements OnInit {

  livraisons: Livraison[] = [];
  livraisonsFiltered: Livraison[] = [];
  transporteurs: Transporteur[] = [];
  isLoading = true;
  processingLivraisonId?: number;
  isProcessingModal = false;
  selectedStatutFilter: string = 'TOUS';
  
  // Pour l'assignation de transporteur
  showAssignModal = false;
  selectedLivraisonId?: number;
  selectedTransporteurId?: number;

  constructor(
    private apiService: ApiService,
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    this.chargerLivraisons();
    this.chargerTransporteurs();
  }

  chargerLivraisons() {
    this.isLoading = true;
    this.apiService.getLivraisons().subscribe({
      next: data => {
        this.livraisons = data;
        this.appliquerFiltre();
        this.isLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des livraisons');
        this.isLoading = false;
        this.cdr.detectChanges();
      }
    });
  }

  chargerTransporteurs() {
    this.apiService.getTransporteurs().subscribe({
      next: data => {
        this.transporteurs = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.notificationService.error('Erreur lors du chargement des transporteurs');
      }
    });
  }

  appliquerFiltre() {
    if (this.selectedStatutFilter === 'TOUS') {
      this.livraisonsFiltered = this.livraisons;
    } else {
      this.livraisonsFiltered = this.livraisons.filter(l => l.statut === this.selectedStatutFilter);
    }
  }

  onFilterChange() {
    this.appliquerFiltre();
  }

  getBadgeClass(statut: string | undefined): string {
    switch (statut) {
      case 'EN_PREPARATION': return 'badge badge-info';
      case 'EXPEDIEE': return 'badge badge-warning';
      case 'LIVREE': return 'badge badge-success';
      default: return 'badge';
    }
  }

  expedier(id: number | undefined) {
    if (!id || this.processingLivraisonId) return;
    this.processingLivraisonId = id;
    this.apiService.expedierLivraison(id).subscribe({
      next: () => {
        this.notificationService.success('Livraison marquée comme expédiée !');
        this.processingLivraisonId = undefined;
        this.chargerLivraisons();
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors de l\'expédition';
        this.notificationService.error(msg);
        this.processingLivraisonId = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  livrer(id: number | undefined) {
    if (!id || this.processingLivraisonId) return;
    this.processingLivraisonId = id;
    this.apiService.livrerLivraison(id).subscribe({
      next: () => {
        this.notificationService.success('Livraison marquée comme livrée !');
        this.processingLivraisonId = undefined;
        this.chargerLivraisons();
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err?.error?.message || 'Erreur lors de la livraison';
        this.notificationService.error(msg);
        this.processingLivraisonId = undefined;
        this.cdr.detectChanges();
      }
    });
  }

  ouvrirModalAssignation(livraisonId: number | undefined) {
    if (!livraisonId) return;
    this.selectedLivraisonId = livraisonId;
    this.selectedTransporteurId = undefined;
    this.showAssignModal = true;
  }

  fermerModalAssignation(event?: Event) {
    if (event && (event.target as HTMLElement).className !== 'modal-backdrop' && !(event.target as HTMLElement).classList?.contains('close-btn') && !(event.target as HTMLElement).classList?.contains('btn-outline')) {
      return;
    }
    this.showAssignModal = false;
    this.selectedLivraisonId = undefined;
    this.selectedTransporteurId = undefined;
    this.isProcessingModal = false;
  }

  assignerTransporteur() {
    if (this.isProcessingModal) return;
    if (!this.selectedLivraisonId || !this.selectedTransporteurId) {
      this.notificationService.error('Veuillez sélectionner un transporteur');
      return;
    }

    this.isProcessingModal = true;
    console.log("Starting assignerTransporteur with:", this.selectedLivraisonId, this.selectedTransporteurId);

    try {
      this.apiService.assignerTransporteur(this.selectedLivraisonId, this.selectedTransporteurId).subscribe({
        next: () => {
          console.log("Assignation success!");
          this.notificationService.success('Transporteur assigné avec succès !');
          this.isProcessingModal = false;
          this.fermerModalAssignation();
          this.chargerLivraisons();
          this.cdr.detectChanges();
        },
        error: (err) => {
          console.error("HTTP Error in assignerTransporteur:", err);
          this.isProcessingModal = false;
          const msg = err?.error?.message || err?.message || 'Erreur lors de l\'assignation';
          this.notificationService.error(msg);
          this.cdr.detectChanges();
        }
      });
    } catch (e) {
      console.error("Sync Error in assignerTransporteur:", e);
      this.isProcessingModal = false;
      this.notificationService.error('Erreur inattendue');
      this.cdr.detectChanges();
    }
  }
}
