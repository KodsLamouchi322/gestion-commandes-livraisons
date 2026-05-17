import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { Dashboard } from './components/dashboard/dashboard';
import { Clients } from './components/clients/clients';
import { Commandes } from './components/commandes/commandes';
import { Livraisons } from './components/livraisons/livraisons';
import { Paiements } from './components/paiements/paiements';
import { Transporteurs } from './components/transporteurs/transporteurs';
import { EspaceClient } from './components/espace-client/espace-client';
import { Login } from './components/auth/login/login';
import { Register } from './components/auth/register/register';
import { HomeRedirect } from './components/home-redirect/home-redirect';
import { Fournisseurs } from './components/fournisseurs/fournisseurs';
import { Produits } from './components/produits/produits';
import { BonsCommande } from './components/bons-commande/bons-commande';
import { AvisAdmin } from './components/avis/avis';
import { Stock } from './components/stock/stock';
import { Categories } from './components/categories/categories';
import { PaiementSuccess } from './components/paiement-success/paiement-success';
import { PaiementCancel } from './components/paiement-cancel/paiement-cancel';
import { adminGuard, clientGuard } from './guards/auth.guard';

const routes: Routes = [
    { path: '', component: HomeRedirect },
    { path: 'auth/login', component: Login },
    { path: 'auth/register', component: Register },

    {
        path: 'admin',
        canActivate: [adminGuard],
        children: [
            { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
            { path: 'dashboard', component: Dashboard },
            { path: 'clients', component: Clients },
            { path: 'commandes', component: Commandes },
            { path: 'livraisons', component: Livraisons },
            { path: 'paiements', component: Paiements },
            { path: 'transporteurs', component: Transporteurs },
            { path: 'produits', component: Produits },
            { path: 'categories', component: Categories },
            { path: 'fournisseurs', component: Fournisseurs },
            { path: 'bons-commande', component: BonsCommande },
            { path: 'stock', component: Stock },
            { path: 'avis', component: AvisAdmin },
        ]
    },

    {
        path: 'client',
        canActivate: [clientGuard],
        children: [
            { path: '', pathMatch: 'full', redirectTo: 'espace' },
            { path: 'espace', component: EspaceClient },
        ]
    },

    // Routes publiques pour les paiements Stripe
    { path: 'paiement/success', component: PaiementSuccess },
    { path: 'paiement/cancel', component: PaiementCancel },

    { path: '**', redirectTo: '' }
];

@NgModule({
    imports: [RouterModule.forRoot(routes, {
        onSameUrlNavigation: 'reload'
    })],
    exports: [RouterModule]
})
export class AppRoutingModule { }
