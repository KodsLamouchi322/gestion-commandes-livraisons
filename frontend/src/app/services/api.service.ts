import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, forkJoin, map, switchMap } from 'rxjs';
import { AuthService } from './auth.service';
import {
    Avis, BonCommande, Categorie, Client, Commande,
    Fournisseur, LigneBonCommande, LigneCommande,
    Livraison, Paiement, Panier, Produit, Transporteur
} from '../models/models';

@Injectable({ providedIn: 'root' })
export class ApiService {

    private readonly base = '/api';

    constructor(
        private http: HttpClient,
        private auth: AuthService
    ) { }

    private getClientId(): number {
        const clientId = this.auth.currentUser?.clientId;
        if (!clientId) {
            throw new Error('Client non authentifie');
        }
        return clientId;
    }

    // ── CATEGORIES ──────────────────────────────────────────
    getCategories(): Observable<Categorie[]> {
        return this.http.get<Categorie[]>(`${this.base}/categories`);
    }
    createCategorie(c: Categorie): Observable<Categorie> {
        return this.http.post<Categorie>(`${this.base}/categories`, c);
    }
    updateCategorie(id: number, c: Categorie): Observable<Categorie> {
        return this.http.put<Categorie>(`${this.base}/categories/${id}`, c);
    }
    deleteCategorie(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/categories/${id}`);
    }

    // ── PRODUITS ─────────────────────────────────────────────
    getProduits(q?: string, categorieId?: number): Observable<Produit[]> {
        let url = `${this.base}/produits`;
        const params: string[] = [];
        if (q) params.push(`q=${encodeURIComponent(q)}`);
        if (categorieId) params.push(`categorieId=${categorieId}`);
        if (params.length) url += '?' + params.join('&');
        return this.http.get<Produit[]>(url);
    }
    getProduitById(id: number): Observable<Produit> {
        return this.http.get<Produit>(`${this.base}/produits/${id}`);
    }
    createProduit(p: Produit): Observable<Produit> {
        return this.http.post<Produit>(`${this.base}/produits`, p);
    }
    updateProduit(id: number, p: Produit): Observable<Produit> {
        return this.http.put<Produit>(`${this.base}/produits/${id}`, p);
    }
    uploadImageProduit(id: number, file: File): Observable<Produit> {
        const fd = new FormData();
        fd.append('file', file);
        return this.http.post<Produit>(`${this.base}/produits/${id}/image`, fd);
    }
    deleteProduit(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/produits/${id}`);
    }

    // ── AVIS ─────────────────────────────────────────────────
    getAllAvis(): Observable<Avis[]> {
        return this.http.get<Avis[]>(`${this.base}/avis`);
    }
    getAvisByProduit(produitId: number): Observable<Avis[]> {
        return this.http.get<Avis[]>(`${this.base}/avis/produit/${produitId}`);
    }
    getNoteMoyenne(produitId: number): Observable<{ noteMoyenne: number; count: number }> {
        return this.http.get<any>(`${this.base}/avis/produit/${produitId}/moyenne`);
    }
    posterAvis(produitId: number, note: number, commentaire: string): Observable<Avis> {
        return this.http.post<Avis>(`${this.base}/avis`, {
            note,
            commentaire,
            client: { id: this.getClientId() },
            produit: { id: produitId }
        });
    }
    supprimerAvis(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/avis/${id}`);
    }

    // ── PANIER ───────────────────────────────────────────────
    getPanier(): Observable<Panier> {
        return this.http.get<Panier>(`${this.base}/panier/client/${this.getClientId()}`);
    }
    ajouterAuPanier(produitId: number, quantite: number = 1): Observable<Panier> {
        return this.http.post<Panier>(
            `${this.base}/panier/client/${this.getClientId()}/produit/${produitId}?quantite=${quantite}`,
            {}
        );
    }
    modifierQuantitePanier(produitId: number, quantite: number): Observable<Panier> {
        const clientId = this.getClientId();
        if (quantite <= 0) {
            return this.http.delete<Panier>(`${this.base}/panier/client/${clientId}/produit/${produitId}`);
        }
        return this.http.delete<Panier>(`${this.base}/panier/client/${clientId}/produit/${produitId}`).pipe(
            switchMap(() => this.http.post<Panier>(
                `${this.base}/panier/client/${clientId}/produit/${produitId}?quantite=${quantite}`,
                {}
            ))
        );
    }
    supprimerLignePanier(produitId: number): Observable<Panier> {
        return this.http.delete<Panier>(`${this.base}/panier/client/${this.getClientId()}/produit/${produitId}`);
    }
    viderPanier(): Observable<void> {
        return this.http.delete<void>(`${this.base}/panier/client/${this.getClientId()}`);
    }
    commanderPanier(): Observable<Commande> {
        const clientId = this.getClientId();
        return this.getPanier().pipe(
            switchMap((panier) => {
                const montantTotal = panier.lignes.reduce((sum, l) => sum + (l.produit.prixUnitaire * l.quantite), 0);
                return this.getMonProfil().pipe(
                    switchMap((profil) => this.createCommande({
                        client: { id: clientId, nom: profil.nom, email: profil.email, adresse: profil.adresse },
                        montantTotal,
                        adresseLivraison: profil.adresse || 'Adresse à renseigner'
                    }).pipe(
                        switchMap((commande) => {
                            const lignes = panier.lignes.map((ligne) => this.createLigne({
                                commande: { id: commande.id },
                                produit: ligne.produit,
                                quantite: ligne.quantite,
                                prixUnitaire: ligne.produit.prixUnitaire
                            }));
                            if (lignes.length === 0) {
                                return this.viderPanier().pipe(map(() => commande));
                            }
                            return forkJoin(lignes).pipe(
                                switchMap(() => this.viderPanier().pipe(map(() => commande)))
                            );
                        })
                    ))
                );
            })
        );
    }

    // ── CLIENTS (admin) ──────────────────────────────────────
    getAdminClients(): Observable<Client[]> {
        return this.http.get<Client[]>(`${this.base}/admin/clients`);
    }
    getAdminClientById(id: number): Observable<Client> {
        return this.http.get<Client>(`${this.base}/admin/clients/${id}`);
    }
    createAdminClient(client: Client): Observable<Client> {
        return this.http.post<Client>(`${this.base}/admin/clients`, client);
    }
    updateAdminClient(id: number, client: Client): Observable<Client> {
        return this.http.put<Client>(`${this.base}/admin/clients/${id}`, client);
    }
    deleteAdminClient(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/admin/clients/${id}`);
    }

    // ── COMMANDES ────────────────────────────────────────────
    getCommandes(): Observable<Commande[]> {
        return this.http.get<Commande[]>(`${this.base}/commandes`);
    }
    getMesCommandes(): Observable<Commande[]> {
        return this.http.get<Commande[]>(`${this.base}/commandes/client/${this.getClientId()}`);
    }
    getCommandeById(id: number): Observable<Commande> {
        return this.http.get<Commande>(`${this.base}/commandes/${id}`);
    }
    getCommandesByClient(clientId: number): Observable<Commande[]> {
        return this.http.get<Commande[]>(`${this.base}/commandes/client/${clientId}`);
    }
    createCommande(commande?: Commande): Observable<Commande> {
        return this.http.post<Commande>(`${this.base}/commandes`, commande ?? {});
    }
    validerCommande(id: number): Observable<Commande> {
        return this.http.put<Commande>(`${this.base}/commandes/${id}/statut/VALIDEE`, {});
    }
    annulerCommande(id: number): Observable<Commande> {
        return this.http.put<Commande>(`${this.base}/commandes/${id}/statut/ANNULEE`, {});
    }
    deleteCommande(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/commandes/${id}`);
    }

    // ── LIGNES COMMANDE ──────────────────────────────────────
    getLignesByCommande(commandeId: number): Observable<LigneCommande[]> {
        return this.http.get<LigneCommande[]>(`${this.base}/lignes-commande/commande/${commandeId}`);
    }
    createLigne(ligne: LigneCommande): Observable<LigneCommande> {
        return this.http.post<LigneCommande>(`${this.base}/lignes-commande`, ligne);
    }
    deleteLigne(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/lignes-commande/${id}`);
    }

    // ── TRANSPORTEURS ────────────────────────────────────────
    getTransporteurs(): Observable<Transporteur[]> {
        return this.http.get<Transporteur[]>(`${this.base}/transporteurs`);
    }
    createTransporteur(t: Transporteur): Observable<Transporteur> {
        return this.http.post<Transporteur>(`${this.base}/transporteurs`, t);
    }
    updateTransporteur(id: number, t: Transporteur): Observable<Transporteur> {
        return this.http.put<Transporteur>(`${this.base}/transporteurs/${id}`, t);
    }
    deleteTransporteur(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/transporteurs/${id}`);
    }

    // ── LIVRAISONS ───────────────────────────────────────────
    getLivraisons(): Observable<Livraison[]> {
        return this.http.get<Livraison[]>(`${this.base}/livraisons`);
    }
    getLivraisonByCommande(commandeId: number): Observable<Livraison> {
        return this.http.get<Livraison>(`${this.base}/livraisons/commande/${commandeId}`);
    }
    createLivraison(l: Livraison): Observable<Livraison> {
        return this.http.post<Livraison>(`${this.base}/livraisons`, l);
    }
    updateStatutLivraison(id: number, statut: string): Observable<Livraison> {
        return this.http.put<Livraison>(`${this.base}/livraisons/${id}/statut/${statut}`, {});
    }
    deleteLivraison(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/livraisons/${id}`);
    }

    // ── PAIEMENTS ────────────────────────────────────────────
    getPaiements(): Observable<Paiement[]> {
        return this.http.get<Paiement[]>(`${this.base}/paiements`);
    }
    createPaiement(p: Paiement): Observable<Paiement> {
        const commandeId = (p.commande as Commande | undefined)?.id;
        if (!commandeId) {
            return throwError(() => new Error('Commande invalide pour le paiement'));
        }
        return this.getCommandeById(commandeId).pipe(
            switchMap((commande) => this.http.post<Paiement>(`${this.base}/paiements`, {
                commande: { id: commande.id },
                methodePaiement: p.methodePaiement,
                statut: p.statut,
                montant: commande.montantTotal ?? 0,
                datePaiement: new Date().toISOString()
            })),
            catchError((error) => throwError(() => error))
        );
    }
    confirmerPaiement(id: number): Observable<Paiement> {
        return this.http.put<Paiement>(`${this.base}/paiements/${id}/statut/VALIDE`, {});
    }
    rembourserPaiement(id: number): Observable<Paiement> {
        return this.http.put<Paiement>(`${this.base}/paiements/${id}/statut/REFUSE`, {});
    }
    createStripeCheckoutSession(commandeId: number): Observable<{ url: string }> {
        return this.http.post<{ url: string }>(`${this.base}/paiements/stripe/create-checkout-session`, { commandeId });
    }
    verifyStripeSession(sessionId: string): Observable<Paiement> {
        return this.http.get<Paiement>(`${this.base}/paiements/stripe/verify-session?session_id=${sessionId}`);
    }

    // ── FOURNISSEURS ─────────────────────────────────────────
    getFournisseurs(): Observable<Fournisseur[]> {
        return this.http.get<Fournisseur[]>(`${this.base}/fournisseurs`);
    }
    createFournisseur(f: Fournisseur): Observable<Fournisseur> {
        return this.http.post<Fournisseur>(`${this.base}/fournisseurs`, f);
    }
    updateFournisseur(id: number, f: Fournisseur): Observable<Fournisseur> {
        return this.http.put<Fournisseur>(`${this.base}/fournisseurs/${id}`, f);
    }
    deleteFournisseur(id: number): Observable<void> {
        return this.http.delete<void>(`${this.base}/fournisseurs/${id}`);
    }

    // ── BONS DE COMMANDE ─────────────────────────────────────
    getBonsCommande(): Observable<BonCommande[]> {
        return this.http.get<BonCommande[]>(`${this.base}/bons-commande`);
    }
    getBonsCommandeByFournisseur(fournisseurId: number): Observable<BonCommande[]> {
        return this.http.get<BonCommande[]>(`${this.base}/bons-commande/fournisseur/${fournisseurId}`);
    }
    createBonCommande(bc: Partial<BonCommande>): Observable<BonCommande> {
        return this.http.post<BonCommande>(`${this.base}/bons-commande`, bc);
    }
    addLigneBonCommande(bcId: number, l: LigneBonCommande): Observable<LigneBonCommande> {
        return this.http.post<LigneBonCommande>(`${this.base}/bons-commande/${bcId}/lignes`, l);
    }
    getLignesBonCommande(bcId: number): Observable<LigneBonCommande[]> {
        return this.http.get<LigneBonCommande[]>(`${this.base}/bons-commande/${bcId}/lignes`);
    }
    receptionnerBonCommande(bcId: number): Observable<BonCommande> {
        return this.http.put<BonCommande>(`${this.base}/bons-commande/${bcId}/receptionner`, {});
    }

    // ── PROFIL CLIENT ────────────────────────────────────────
    getMonProfil(): Observable<Client> {
        return this.http.get<Client>(`${this.base}/clients/me?id=${this.getClientId()}`);
    }
    updateMonProfil(data: { nom: string; email: string; adresse: string; nouveauMotDePasse?: string }): Observable<Client> {
        return this.http.put<Client>(`${this.base}/clients/me?id=${this.getClientId()}`, data);
    }
}
