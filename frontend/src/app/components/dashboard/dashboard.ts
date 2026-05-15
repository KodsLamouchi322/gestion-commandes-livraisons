import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../services/api.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  standalone: false
})
export class Dashboard implements OnInit {

  statistiques = {
    clients: 0,
    commandesEnAttente: 0,
    commandesValidees: 0,
    livraisonsEnCours: 0,
    chiffreAffaires: 0
  };

  isLoading = true;
  today = new Date();

  constructor(private apiService: ApiService) { }

  ngOnInit(): void {
    this.chargerStatistiques();
  }

  get metricPeak(): number {
    return Math.max(
      this.statistiques.clients,
      this.statistiques.commandesEnAttente,
      this.statistiques.commandesValidees,
      this.statistiques.livraisonsEnCours,
      1
    );
  }

  trendPercent(value: number): number {
    return Math.min(100, Math.round((value / this.metricPeak) * 100));
  }

  revenuePercent(): number {
    return Math.min(100, Math.round((this.statistiques.chiffreAffaires / 10000) * 100));
  }

  chargerStatistiques() {
    this.apiService.getAdminClients().subscribe(clients => {
      this.statistiques.clients = clients.length;
    });

    this.apiService.getCommandes().subscribe(commandes => {
      this.statistiques.commandesEnAttente = commandes.filter(c => c.statut === 'EN_ATTENTE').length;
      this.statistiques.commandesValidees = commandes.filter(c => c.statut === 'VALIDEE' || c.statut === 'EXPEDIEE' || c.statut === 'LIVREE').length;

      this.statistiques.chiffreAffaires = commandes
        .filter(c => c.statut !== 'ANNULEE')
        .reduce((sum, current) => sum + (current.montantTotal || 0), 0);

      this.isLoading = false;
    });

    this.apiService.getLivraisons().subscribe(livraisons => {
      this.statistiques.livraisonsEnCours = livraisons.filter(
        l => l.statut === 'EN_PREPARATION' || l.statut === 'EN_TRANSIT'
      ).length;
    });
  }
}
