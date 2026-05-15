import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.html',
    styleUrls: ['./login.css'],
    standalone: false
})
export class Login {

    email = '';
    motDePasse = '';
    error = '';

    constructor(
        private auth: AuthService,
        private router: Router,
        private route: ActivatedRoute
    ) { }

    seConnecter(): void {
        this.error = '';
        if (!this.email || !this.motDePasse) {
            this.error = 'Veuillez remplir tous les champs.';
            return;
        }
        this.auth.login(this.email, this.motDePasse).subscribe({
            next: () => {
                const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
                if (returnUrl && returnUrl.startsWith('/')) {
                    this.router.navigateByUrl(returnUrl);
                    return;
                }
                if (this.auth.isAdmin()) {
                    this.router.navigate(['/admin/dashboard']);
                } else {
                    this.router.navigate(['/client/espace']);
                }
            },
            error: () => {
                this.error = 'Email ou mot de passe incorrect.';
            }
        });
    }
}
