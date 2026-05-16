import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { FavorisService } from '../../services/favoris.service';
import { NotificationService } from '../../services/notification.service';
import { Avis, Categorie, Client, Commande, LignePanier, Paiement, Panier, Produit } from '../../models/models';

type Tab = 'catalogue' | 'panier' | 'favoris' | 'commandes' | 'profil';

@Component({
    selector: 'app-espace-client',
    templateUrl: './espace-client.html',
    styleUrls: ['./espace-client.css'],
    standalone: false
})
export class EspaceClient implements OnInit {

    activeTab: Tab = 'catalogue';

    // Catalogue
    produits: Produit[] = [];
    categories: Categorie[] = [];
    recherche = '';
    categorieFiltre: number | undefined;
    produitDetail: Produit | null = null;
    avisDetail: Avis[] = [];
    monAvisNote = 5;
    monAvisCommentaire = '';

    // Panier
    panier: Panier = { lignes: [] };
    panierCount = 0;
    isPanierBusy = false;

    // Favoris
    favorisIds: number[] = [];
    produitsFavoris: Produit[] = [];

    // Commandes
    mesCommandes: Commande[] = [];
    commandeAPayer?: Commande;
    modePaiement: 'CARTE' | 'VIREMENT' | 'ESPECES' = 'CARTE';
    isPaiementBusy = false;

    // Profil
    profil: Client = { nom: '', email: '', adresse: '' };
    nouveauMdp = '';
    profilMsg = '';

    constructor(
        public auth: AuthService,
        public favoris: FavorisService,
        private api: ApiService,
        private route: ActivatedRoute,
        private router: Router,
        private notificationService: NotificationService
    ) { }

    ngOnInit(): void {
        this.chargerCatalogue();
        this.chargerPanier();
        this.chargerCommandes();
        this.chargerProfil();
        this.favoris.favorisIds$.subscribe(ids => {
            this.favorisIds = ids;
            this.majFavoris();
        });
        this.verifierRetourStripe();
    }

    // ── CATALOGUE ────────────────────────────────────────────
    chargerCatalogue(): void {
        this.api.getCategories().subscribe(c => this.categories = c);
        this.filtrer();
    }

    filtrer(): void {
        this.api.getProduits(this.recherche || undefined, this.categorieFiltre).subscribe(p => {
            this.produits = p;
            this.majFavoris();
        });
    }

    ouvrirDetail(p: Produit): void {
        this.produitDetail = p;
        this.api.getAvisByProduit(p.id!).subscribe(a => this.avisDetail = a);
    }

    fermerDetail(): void {
        this.produitDetail = null;
        this.avisDetail = [];
    }

    posterAvis(): void {
        if (!this.produitDetail?.id) return;
        this.api.posterAvis(this.produitDetail.id, this.monAvisNote, this.monAvisCommentaire).subscribe(() => {
            this.api.getAvisByProduit(this.produitDetail!.id!).subscribe(a => this.avisDetail = a);
            this.monAvisCommentaire = '';
            this.filtrer();
        });
    }

    etoiles(n: number): string[] {
        return Array.from({ length: 5 }, (_, i) => i < Math.round(n) ? '★' : '☆');
    }

    // ── PANIER ───────────────────────────────────────────────
    chargerPanier(): void {
        if (!this.auth.isClient()) return;
        this.api.getPanier().subscribe(p => {
            this.appliquerPanier(p);
        });
    }

    ajouterAuPanier(produit: Produit, qte = 1): void {
        if (this.isPanierBusy || !produit.id) return;
        if (produit.quantiteEnStock <= 0) {
            this.notificationService.warning('Produit en rupture de stock');
            return;
        }
        this.isPanierBusy = true;
        this.api.ajouterAuPanier(produit.id!, qte).subscribe(p => {
            this.appliquerPanier(p);
            this.activeTab = 'panier';
            this.fermerDetail();
            this.notificationService.success('Produit ajouté au panier');
            this.isPanierBusy = false;
        }, () => {
            this.notificationService.error('Impossible d\'ajouter ce produit au panier');
            this.isPanierBusy = false;
        });
    }

    modifierQte(ligne: LignePanier, delta: number): void {
        if (this.isPanierBusy || !ligne.produit.id) return;
        const nv = ligne.quantite + delta;
        if (nv <= 0) {
            this.supprimerLigne(ligne);
        } else {
            this.isPanierBusy = true;
            this.api.modifierQuantitePanier(ligne.produit.id!, nv).subscribe(p => {
                this.appliquerPanier(p);
                this.isPanierBusy = false;
            }, () => {
                this.notificationService.error('Erreur lors de la mise à jour de la quantité');
                this.isPanierBusy = false;
                this.chargerPanier();
            });
        }
    }

    supprimerLigne(ligne: LignePanier): void {
        if (this.isPanierBusy || !ligne.produit.id) return;
        this.isPanierBusy = true;
        this.api.supprimerLignePanier(ligne.produit.id!).subscribe(p => {
            this.appliquerPanier(p);
            this.notificationService.success('Produit supprimé du panier');
            this.isPanierBusy = false;
        }, () => {
            this.notificationService.error('Erreur lors de la suppression');
            this.isPanierBusy = false;
            this.chargerPanier();
        });
    }

    totalPanier(): number {
        return this.panier.lignes.reduce((s, l) => s + l.produit.prixUnitaire * l.quantite, 0);
    }

    viderPanier(): void {
        if (this.isPanierBusy || this.panier.lignes.length === 0) return;
        this.isPanierBusy = true;
        this.api.viderPanier().subscribe(() => {
            this.appliquerPanier({ lignes: [] });
            this.notificationService.success('Panier vidé');
            this.isPanierBusy = false;
        }, () => {
            this.notificationService.error('Erreur lors du vidage du panier');
            this.isPanierBusy = false;
        });
    }

    commander(): void {
        if (this.isPanierBusy) return;
        if (this.panier.lignes.length === 0) {
            this.notificationService.warning('Votre panier est vide');
            return;
        }
        if (!this.profil.adresse?.trim()) {
            this.notificationService.warning('Veuillez renseigner votre adresse avant de commander');
            this.activeTab = 'profil';
            return;
        }
        this.isPanierBusy = true;
        this.api.commanderPanier().subscribe(cmd => {
            this.appliquerPanier({ lignes: [] });
            this.chargerCommandes();
            this.activeTab = 'commandes';
            this.notificationService.success(`Commande #${cmd.id} créée avec succès`);
            this.isPanierBusy = false;
        }, () => {
            this.notificationService.error('Impossible de finaliser la commande');
            this.isPanierBusy = false;
            this.chargerPanier();
        });
    }

    // ── FAVORIS ──────────────────────────────────────────────
    majFavoris(): void {
        this.produitsFavoris = this.produits.filter(p => p.id && this.favorisIds.includes(p.id));
    }

    toggleFavori(p: Produit): void {
        this.favoris.toggle(p);
    }

    // ── COMMANDES ────────────────────────────────────────────
    chargerCommandes(): void {
        if (!this.auth.isClient()) return;
        this.api.getMesCommandes().subscribe(c => this.mesCommandes = c);
    }

    initierPaiement(cmd: Commande): void {
        this.commandeAPayer = cmd;
    }

    payerEnLigne(): void {
        if (this.isPaiementBusy || !this.commandeAPayer?.id) return;
        if ((this.commandeAPayer.montantTotal ?? 0) <= 0) {
            this.notificationService.warning('Montant de commande invalide');
            return;
        }
        const p: Paiement = {
            commande: this.commandeAPayer,
            methodePaiement: this.modePaiement,
            statut: 'EN_ATTENTE'
        };
        this.isPaiementBusy = true;
        this.api.createPaiement(p).subscribe(() => {
            this.notificationService.success(`Paiement initié pour la commande #${this.commandeAPayer!.id}`);
            this.commandeAPayer = undefined;
            this.chargerCommandes();
            this.isPaiementBusy = false;
        }, () => {
            this.notificationService.error('Le paiement a échoué');
            this.isPaiementBusy = false;
        });
    }

    verifierRetourStripe(): void {
        // Flux Stripe retiré: backend actuel expose seulement le paiement classique.
        if (this.route.snapshot.queryParamMap.get('stripe') === 'cancel') {
            this.notificationService.warning('Paiement annulé.');
            this.router.navigate(['/client/espace']);
        }
    }

    getBadgeCmd(statut?: string): string {
        const m: Record<string, string> = {
            EN_ATTENTE: 'badge-warning', VALIDEE: 'badge-success',
            EXPEDIEE: 'badge-info', LIVREE: 'badge-success', ANNULEE: 'badge-error'
        };
        return 'badge ' + (m[statut || ''] || '');
    }

    // ── PROFIL ───────────────────────────────────────────────
    chargerProfil(): void {
        if (!this.auth.isAuthenticated()) return;
        this.api.getMonProfil().subscribe(c => this.profil = c);
    }

    sauvegarderProfil(): void {
        this.api.updateMonProfil({
            nom: this.profil.nom,
            email: this.profil.email,
            adresse: this.profil.adresse,
            nouveauMotDePasse: this.nouveauMdp || undefined
        }).subscribe({
            next: () => { this.profilMsg = 'Profil mis à jour !'; this.nouveauMdp = ''; },
            error: () => this.profilMsg = 'Erreur lors de la mise à jour.'
        });
    }

    private appliquerPanier(panier: Panier): void {
        this.panier = panier ?? { lignes: [] };
        this.panierCount = this.panier.lignes.reduce((s, l) => s + l.quantite, 0);
    }
}
