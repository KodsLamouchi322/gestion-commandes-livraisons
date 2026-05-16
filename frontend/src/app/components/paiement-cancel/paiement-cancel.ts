import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-paiement-cancel',
  templateUrl: './paiement-cancel.html',
  standalone: false
})
export class PaiementCancel implements OnInit {
  commandeId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: ApiService,
    private notif: NotificationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.queryParamMap.get('commande_id');
    if (id) {
      this.commandeId = parseInt(id, 10);
    }
  }

  retryPayment(): void {
    if (!this.commandeId) {
      this.notif.error('Commande introuvable');
      return;
    }

    this.api.createStripeCheckoutSession(this.commandeId).subscribe({
      next: (response) => {
        window.location.href = response.url;
      },
      error: (err) => {
        console.error('Erreur création session:', err);
        this.notif.error('Erreur lors de la création de la session de paiement');
      }
    });
  }

  goToCommandes(): void {
    this.router.navigate(['/espace-client']);
  }
}
