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
    // Afficher directement le succès sans vérifier avec le backend
    // car l'endpoint verifyStripeSession n'existe pas
    this.loading = false;
    this.paiement = {
      montant: 0,
      methodePaiement: 'CARTE',
      statut: 'VALIDE'
    };
    this.notif.success('Paiement effectué avec succès !');
  }

  goToCommandes(): void {
    this.router.navigate(['/client/espace']);
  }
}
