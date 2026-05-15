import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
    selector: 'app-register',
    templateUrl: './register.html',
    styleUrls: ['./register.css'],
    standalone: false
})
export class Register {

    nom = '';
    email = '';
    adresse = '';
    motDePasse = '';
    error = '';

    constructor(
        private auth: AuthService,
        private router: Router
    ) { }

    creerCompte(): void {
        this.error = '';
        if (!this.nom || !this.email || !this.adresse || !this.motDePasse) {
            this.error = 'Tous les champs sont obligatoires.';
            return;
        }
        if (this.motDePasse.length < 6) {
            this.error = 'Le mot de passe doit contenir au moins 6 caractères.';
            return;
        }
        this.auth.register(this.nom, this.email, this.adresse, this.motDePasse).subscribe({
            next: () => {
                alert('Inscription réussie !');
                this.router.navigate(['/client/espace']);
            },
            error: (err) => {
                console.error(err);
                if (err.status === 400) {
                    this.error = 'Erreur : Veuillez vérifier que l\'email est valide et que les champs sont corrects.';
                } else if (err.status === 403 || err.status === 409) {
                    this.error = 'Impossible de créer le compte : cet email est probablement déjà utilisé.';
                } else {
                    this.error = 'Erreur serveur. Regardez la console ou les logs backend.';
                }
                alert(this.error);
            }
        });
    }
}
