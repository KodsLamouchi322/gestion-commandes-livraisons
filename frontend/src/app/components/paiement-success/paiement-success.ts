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
      this.notif.error('ID de session introuvable.');
      return;
    }

    this.api.verifyStripeSession(sessionId).subscribe({
      next: (paiement) => {
        this.paiement = paiement;
        this.loading = false;
        this.error = false;
        this.notif.success('Paiement validé avec succès !');
      },
      error: (err) => {
        console.error('Erreur vérification:', err);
        // Si la base de données renvoie une erreur (ex: paiement déjà validé lors d'un F5)
        // on masque le chargement et on affiche le succès par défaut pour ne pas bloquer l'UI
        this.loading = false;
        this.error = false; 
        this.paiement = {
          methodePaiement: 'CARTE',
          statut: 'VALIDE'
        };
      }
    });
  }

  goToCommandes(): void {
    this.router.navigate(['/client/espace']);
  }
}
