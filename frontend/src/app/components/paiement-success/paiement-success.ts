import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-paiement-success',
  templateUrl: './paiement-success.html',
  standalone: false,
  styleUrls: ['./paiement-success.css']
})
export class PaiementSuccess implements OnInit {
  loading = true;
  paiement: any = null;
  error = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private api: ApiService,
    private notif: NotificationService
  ) {}

  ngOnInit(): void {
    const sessionId = this.route.snapshot.queryParamMap.get('session_id');
    
    if (!sessionId) {
      this.error = true;
      this.loading = false;
      return;
    }

    this.api.verifyStripeSession(sessionId).subscribe({
      next: (paiement) => {
        this.paiement = paiement;
        this.loading = false;
        this.notif.success('Paiement validé avec succès !');
      },
      error: (err) => {
        console.error('Erreur vérification paiement:', err);
        this.error = true;
        this.loading = false;
        this.notif.error('Erreur lors de la vérification du paiement');
      }
    });
  }

  goToCommandes(): void {
    this.router.navigate(['/espace-client']);
  }
}
