import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../services/api.service';
import { AuthService } from '../../services/auth.service';
import { FavorisService } from '../../services/favoris.service';
import { NotificationService } from '../../services/notification.service';
import { Avis, Categorie, Client, Commande, LignePanier, Paiement, Panier, Produit } from '../../models/models';
import { forkJoin } from 'rxjs';
import { finalize, timeout } from 'rxjs';

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
    isCommandeBusy = false; // Séparé pour la finalisation de commande

    // Favoris
    favorisIds: number[] = [];
    produitsFavoris: Produit[] = [];

    // Commandes
    mesCommandes: Commande[] = [];
    commandeAPayer?: Commande;
    modePaiement: 'CARTE' | 'ESPECES' = 'CARTE';
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
        private notificationService: NotificationService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        // Charger le catalogue immédiatement (produits + catégories)
        this.chargerCatalogue();

        // Charger les données client dès que la session est prête.
        this.auth.user$.subscribe(() => {
            this.chargerPanier();
            this.chargerCommandes();
            this.chargerProfil();
        });

        this.favoris.favorisIds$.subscribe(ids => {
            this.favorisIds = ids;
            this.majFavoris();
        });
        this.verifierRetourStripe();
    }

    // ── CATALOGUE ────────────────────────────────────────────
    chargerCatalogue(): void {
        this.api.getCategories().subscribe({
            next: (c) => this.categories = c,
            error: (err) => console.error('Erreur chargement catégories:', err)
        });
        this.filtrer();
    }

    filtrer(): void {
        this.api.getProduits(this.recherche || undefined, this.categorieFiltre).subscribe({
            next: (p) => {
                this.produits = p;
                this.majFavoris();
                console.log('Produits chargés:', p.length);
            },
            error: (err) => {
                console.error('Erreur chargement produits:', err);
                this.notificationService.error('Impossible de charger les produits');
            }
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
        if (!this.monAvisCommentaire.trim()) {
            this.notificationService.warning('Veuillez écrire un commentaire avant de publier.');
            return;
        }
        const pid = this.produitDetail.id;
        this.api.posterAvis(pid, this.monAvisNote, this.monAvisCommentaire).subscribe({
            next: () => {
                this.notificationService.success('Avis publié avec succès !');
                this.monAvisCommentaire = '';
                this.monAvisNote = 5;
                forkJoin({
                    avis: this.api.getAvisByProduit(pid),
                    stats: this.api.getNoteMoyenne(pid)
                }).subscribe({
                    next: ({ avis, stats }) => {
                        this.avisDetail = avis;
                        const n = stats?.noteMoyenne != null ? Number(stats.noteMoyenne) : 0;
                        const note = n > 0 ? n : undefined;
                        if (this.produitDetail?.id === pid) {
                            this.produitDetail.noteMoyenne = note;
                        }
                        const pr = this.produits.find(x => x.id === pid);
                        if (pr) {
                            pr.noteMoyenne = note;
                        }
                        this.majFavoris();
                        this.filtrer();
                        this.cdr.detectChanges();
                    },
                    error: () => {
                        this.filtrer();
                        this.cdr.detectChanges();
                    }
                });
            },
            error: (err: any) => {
                // Le backend renvoie GlobalExceptionHandler avec { message, status, ... }
                const msg: string =
                    err?.error?.message ||
                    err?.error?.error ||
                    err?.message ||
                    'Erreur lors de la publication de l\'avis';
                this.notificationService.error(String(msg));
            }
        });
    }

    etoiles(n: number): string[] {
        return Array.from({ length: 5 }, (_, i) => i < Math.round(n) ? '★' : '☆');
    }

    // ── PANIER ───────────────────────────────────────────────
    chargerPanier(): void {
        if (!this.auth.isClient()) return;
        try {
            this.api.getPanier().subscribe({
                next: (p) => this.appliquerPanier(p),
                error: () => {
                    // Pas bloquant : si le panier n'est pas accessible maintenant, on réessaiera au prochain tick/login
                    this.appliquerPanier({ lignes: [] });
                }
            });
        } catch {
            this.appliquerPanier({ lignes: [] });
        }
    }

    ajouterAuPanier(produit: Produit, qte = 1): void {
        if (this.isPanierBusy || !produit.id) return;
        if (produit.quantiteEnStock <= 0) {
            this.notificationService.warning('Produit en rupture de stock');
            return;
        }
        // Vérifier la quantité déjà dans le panier
        const ligneExistante = this.panier.lignes.find(l => l.produit.id === produit.id);
        const qteDejaEnPanier = ligneExistante ? ligneExistante.quantite : 0;
        if (qteDejaEnPanier + qte > produit.quantiteEnStock) {
            this.notificationService.warning(
                `Stock insuffisant : ${produit.quantiteEnStock} disponible(s), vous en avez déjà ${qteDejaEnPanier} dans votre panier`
            );
            return;
        }
        const snapshot = this.snapshotPanier();
        this.optimisticAdd(produit, qte);
        this.isPanierBusy = true;
        this.api.ajouterAuPanier(produit.id!, qte).subscribe(p => {
            this.appliquerPanier(p); // vérité backend
            this.activeTab = 'panier';
            this.fermerDetail();
            this.notificationService.success('Produit ajouté au panier ✓');
            this.isPanierBusy = false;
            // Sur mobile / écran scrollé, le changement d'onglet peut sembler "ne rien faire"
            // si l'utilisateur est en bas de page. On remonte pour afficher le panier.
            this.scrollToTop();
            this.filtrer(); // Rafraîchir le catalogue pour mettre à jour les stocks
        }, (err: any) => {
            this.restorePanier(snapshot);
            const msg: string = err?.error?.message || err?.error || '';
            if (msg.toLowerCase().includes('stock') || err?.status === 400) {
                this.notificationService.warning('Stock insuffisant pour ce produit');
                this.filtrer(); // Rafraîchir les stocks affichés
            } else {
                this.notificationService.error('Impossible d\'ajouter ce produit au panier');
            }
            this.isPanierBusy = false;
        });
    }

    modifierQte(ligne: LignePanier, delta: number): void {
        if (this.isPanierBusy || !ligne.produit.id) return;
        const nv = ligne.quantite + delta;
        if (nv <= 0) {
            this.supprimerLigne(ligne);
        } else {
            const snapshot = this.snapshotPanier();
            this.optimisticSetQty(ligne.produit.id!, nv);
            this.isPanierBusy = true;
            this.api.modifierQuantitePanier(ligne.produit.id!, nv).subscribe({
                next: (p) => {
                    this.appliquerPanier(p);
                    this.isPanierBusy = false;
                    // Forcer la détection de changement
                    this.cdr.detectChanges();
                },
                error: () => {
                    this.restorePanier(snapshot);
                    this.notificationService.error('Erreur lors de la mise à jour de la quantité');
                    this.isPanierBusy = false;
                    this.cdr.detectChanges();
                    this.chargerPanier();
                }
            });
        }
    }

    supprimerLigne(ligne: LignePanier): void {
        if (this.isPanierBusy || !ligne.produit.id) return;
        const snapshot = this.snapshotPanier();
        this.optimisticRemove(ligne.produit.id!);
        this.isPanierBusy = true;
        this.api.supprimerLignePanier(ligne.produit.id!).subscribe({
            next: (p) => {
                this.appliquerPanier(p);
                this.notificationService.success('Produit supprimé du panier');
                this.isPanierBusy = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.restorePanier(snapshot);
                this.notificationService.error('Erreur lors de la suppression');
                this.isPanierBusy = false;
                this.cdr.detectChanges();
                this.chargerPanier();
            }
        });
    }

    totalPanier(): number {
        return this.panier.lignes.reduce(
            (s, l) => s + (l.prixUnitaire ?? l.produit.prixUnitaire ?? 0) * l.quantite, 0
        );
    }

    viderPanier(): void {
        if (this.isPanierBusy || this.panier.lignes.length === 0) return;
        const snapshot = this.snapshotPanier();
        this.appliquerPanier({ lignes: [] });
        this.isPanierBusy = true;
        try {
            this.api.viderPanier().subscribe({
                next: () => {
                    this.appliquerPanier({ lignes: [] });
                    this.notificationService.success('Panier vidé');
                    this.isPanierBusy = false;
                    this.cdr.detectChanges();
                },
                error: () => {
                    // Le backend a peut-être vidé le panier mais JSON parse a échoué
                    this.appliquerPanier({ lignes: [] });
                    this.notificationService.success('Panier vidé');
                    this.isPanierBusy = false;
                    this.cdr.detectChanges();
                    this.chargerPanier();
                }
            });
        } catch {
            this.restorePanier(snapshot);
            this.notificationService.error('Session expirée. Reconnectez-vous.');
            this.isPanierBusy = false;
            this.cdr.detectChanges();
        }
    }

    commander(): void {
        if (this.isCommandeBusy) return;
        if (this.panier.lignes.length === 0) {
            this.notificationService.warning('Votre panier est vide');
            return;
        }
        if (!this.profil.adresse?.trim()) {
            this.notificationService.warning('Veuillez renseigner votre adresse avant de commander');
            this.activeTab = 'profil';
            return;
        }
        this.isCommandeBusy = true;
        // Feedback immédiat (évite l'impression "rien ne se passe")
        this.notificationService.info('Finalisation de votre commande...');
        const snapshot = this.snapshotPanier();
        try {
            this.api.commanderPanier().pipe(
                // Empêche un blocage infini si l'API ne répond pas
                timeout(20000),
                // Garantit le déblocage UI quoi qu'il arrive (success/error/exception)
                finalize(() => {
                    this.isCommandeBusy = false;
                    // Sécurité: forcer le rafraîchissement UI même si un handler a cassé la zone
                    try { this.cdr.detectChanges(); } catch { /* ignore */ }
                    setTimeout(() => {
                        this.isCommandeBusy = false;
                        try { this.cdr.detectChanges(); } catch { /* ignore */ }
                    }, 0);
                })
            ).subscribe({
                next: (cmd) => {
                    this.appliquerPanier({ lignes: [] });
                    this.activeTab = 'commandes';
                    this.scrollToTop();
                    this.notificationService.success(`Commande #${cmd?.id ?? ''} créée avec succès`);
                    this.chargerCommandes();
                },
                error: (err: any) => {
                    this.restorePanier(snapshot);
                    // Erreur HTTP backend ou erreur Observable (throwError)
                    const msg: string = err?.error?.message || err?.error || err?.message || '';
                    if (msg.toLowerCase().includes('montant') || msg.toLowerCase().includes('prix')) {
                        this.notificationService.warning('Prix des produits invalide — mettez à jour le catalogue admin');
                    } else if (msg.toLowerCase().includes('adresse')) {
                        this.notificationService.warning('Veuillez renseigner votre adresse de livraison');
                        this.activeTab = 'profil';
                    } else if (msg.toLowerCase().includes('session') || msg.toLowerCase().includes('reconnect')) {
                        this.notificationService.error('Session expirée. Reconnectez-vous.');
                    } else if (msg.toLowerCase().includes('vide')) {
                        this.notificationService.warning('Votre panier est vide');
                    } else if (String(err?.name || '').toLowerCase().includes('timeout')) {
                        this.notificationService.error('Délai dépassé. Réessayez dans quelques secondes.');
                    } else {
                        this.notificationService.error(msg || 'Impossible de finaliser la commande');
                    }
                    this.chargerPanier();
                }
            });
        } catch {
            this.notificationService.error('Session expirée. Reconnectez-vous.');
            this.isCommandeBusy = false;
        }
    }

    // ── FAVORIS ──────────────────────────────────────────────
    activerFavoris(): void {
        this.activeTab = 'favoris';
        // Si le catalogue n'est pas encore chargé, le charger
        if (this.produits.length === 0) {
            this.filtrer();
        } else {
            this.majFavoris();
        }
    }

    majFavoris(): void {
        this.produitsFavoris = this.produits.filter(p => p.id && this.favorisIds.includes(p.id));
    }

    toggleFavori(p: Produit): void {
        const wasInFavoris = this.favoris.isFavori(p.id!);
        this.favoris.toggle(p);
        this.notificationService.success(wasInFavoris ? 'Retiré des favoris' : 'Ajouté aux favoris ❤️');
        if (this.activeTab === 'favoris') {
            this.majFavoris();
        }
    }

    // ── COMMANDES ────────────────────────────────────────────
    chargerCommandes(): void {
        if (!this.auth.isClient()) return;
        this.api.getMesCommandes().subscribe({
            next: (c) => {
                this.mesCommandes = c;
                this.cdr.detectChanges();
            },
            error: () => {
                // Erreur silencieuse, on garde les commandes actuelles
            }
        });
    }

    initierPaiement(cmd: Commande): void {
        // Le bouton ne s'affiche que si aucun paiement n'existe
        // Donc pas besoin de vérifier
        this.commandeAPayer = cmd;
    }

    payerEnLigne(): void {
        if (this.isPaiementBusy || !this.commandeAPayer?.id) return;
        if ((this.commandeAPayer.montantTotal ?? 0) <= 0) {
            this.notificationService.warning('Montant de commande invalide');
            return;
        }

        this.isPaiementBusy = true;
        const commandeId = this.commandeAPayer.id!;

        // ── Carte bancaire → Stripe Checkout ──
        if (this.modePaiement === 'CARTE') {
            try {
                this.api.createStripeCheckoutSession(commandeId).subscribe({
                    next: (res) => {
                        // Redirection vers la page Stripe (isPaiementBusy reste true pendant le redirect)
                        this.notificationService.info('Redirection vers Stripe...');
                        setTimeout(() => { window.location.href = res.url; }, 800);
                    },
                    error: (err: any) => {
                        if (err?.status === 503) {
                            this.notificationService.warning(
                                '⚠️ Paiement Stripe non configuré. Utilisez Espèces pour tester.'
                            );
                        } else if (err?.status === 409) {
                            this.notificationService.warning('Un paiement existe déjà pour cette commande.');
                        } else {
                            const msg =
                                err?.error?.error ||
                                err?.error?.message ||
                                err?.error ||
                                err?.message ||
                                'Erreur Stripe';
                            this.notificationService.error(String(msg));
                        }
                        this.isPaiementBusy = false;
                    }
                });
            } catch {
                this.notificationService.error('Erreur de redirection Stripe.');
                this.isPaiementBusy = false;
            }
            return;
        }

        // ── Espèces → paiement classique ──
        const p: Paiement = {
            commande: this.commandeAPayer,
            methodePaiement: this.modePaiement,
            statut: 'EN_ATTENTE'
        };
        try {
            this.api.createPaiement(p).subscribe({
                next: () => {
                    // Fermer le modal immédiatement
                    this.commandeAPayer = undefined;
                    this.isPaiementBusy = false;
                    
                    // Afficher la notification
                    this.notificationService.success(
                        `✅ Paiement enregistré pour la commande #${commandeId}`
                    );
                    
                    // Recharger les commandes après un court délai pour s'assurer que le backend a terminé
                    setTimeout(() => {
                        this.chargerCommandes();
                    }, 300);
                    
                    this.cdr.detectChanges();
                },
                error: (err: any) => {
                    const msg = err?.error?.message || 'Le paiement a échoué';
                    this.notificationService.error(msg);
                    this.isPaiementBusy = false;
                    this.cdr.detectChanges();
                }
            });
        } catch {
            this.notificationService.error('Session expirée. Reconnectez-vous.');
            this.isPaiementBusy = false;
            this.cdr.detectChanges();
        }
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
        if (!this.profil.nom?.trim() || !this.profil.email?.trim()) {
            this.notificationService.warning('Nom et email sont obligatoires.');
            return;
        }
        this.api.updateMonProfil({
            nom: this.profil.nom,
            email: this.profil.email,
            adresse: this.profil.adresse || '',
            nouveauMotDePasse: this.nouveauMdp || undefined
        }).subscribe({
            next: (updated) => {
                this.profil = updated;
                this.nouveauMdp = '';
                this.profilMsg = '';
                this.notificationService.success('Profil mis à jour avec succès !');
            },
            error: (err: any) => {
                const msg = err?.error?.message || 'Erreur lors de la mise à jour du profil.';
                this.notificationService.error(msg);
            }
        });
    }

    private appliquerPanier(panier: Panier): void {
        this.panier = panier ?? { lignes: [] };
        // Compter le nombre de produits différents (lignes) au lieu de la quantité totale
        this.panierCount = this.panier.lignes.length;
    }

    private scrollToTop(): void {
        try {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        } catch {
            window.scrollTo(0, 0);
        }
    }

    private snapshotPanier(): Panier {
        return {
            id: this.panier.id,
            client: this.panier.client,
            total: this.panier.total,
            lignes: (this.panier.lignes || []).map(l => ({
                id: l.id,
                produit: l.produit,
                quantite: l.quantite,
                prixUnitaire: l.prixUnitaire,
                sousTotal: l.sousTotal
            }))
        };
    }

    private restorePanier(snapshot: Panier): void {
        this.appliquerPanier(snapshot);
    }

    private optimisticAdd(produit: Produit, qte: number): void {
        const lignes = [...(this.panier.lignes || [])];
        const idx = lignes.findIndex(l => l.produit?.id === produit.id);
        if (idx >= 0) {
            lignes[idx] = { ...lignes[idx], quantite: lignes[idx].quantite + qte };
        } else {
            lignes.push({ produit, quantite: qte, prixUnitaire: produit.prixUnitaire });
        }
        this.appliquerPanier({ ...this.panier, lignes });
    }

    private optimisticSetQty(produitId: number, qty: number): void {
        const lignes = (this.panier.lignes || [])
            .map(l => l.produit?.id === produitId ? { ...l, quantite: qty } : l)
            .filter(l => (l.quantite ?? 0) > 0);
        this.appliquerPanier({ ...this.panier, lignes });
    }

    private optimisticRemove(produitId: number): void {
        const lignes = (this.panier.lignes || []).filter(l => l.produit?.id !== produitId);
        this.appliquerPanier({ ...this.panier, lignes });
    }
}
