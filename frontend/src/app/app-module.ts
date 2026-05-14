import { NgModule, provideBrowserGlobalErrorListeners } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing-module';
import { App } from './app';
import { Navbar } from './components/navbar/navbar';
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
import { Categories } from './components/categories/categories';
import { PaiementSuccess } from './components/paiement-success/paiement-success';
import { PaiementCancel } from './components/paiement-cancel/paiement-cancel';
import { AuthInterceptor } from './interceptors/auth.interceptor';
import { NotificationComponent } from './components/notification/notification.component';

@NgModule({
    declarations: [
        App, Navbar, Dashboard, Clients, Commandes, Livraisons,
        Paiements, Transporteurs, EspaceClient, Login, Register,
        HomeRedirect, Fournisseurs, Produits, BonsCommande,
        AvisAdmin, Categories, PaiementSuccess, PaiementCancel,
    ],
    imports: [
        BrowserModule, AppRoutingModule, HttpClientModule,
        FormsModule, ReactiveFormsModule,
        NotificationComponent,
    ],
    providers: [
        provideBrowserGlobalErrorListeners(),
        { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    ],
    bootstrap: [App],
})
export class AppModule { }
