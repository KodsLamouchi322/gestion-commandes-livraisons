import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-home-redirect',
    template: '<p style="padding:2rem;text-align:center">Redirection…</p>',
    standalone: false
})
export class HomeRedirect implements OnInit {

    constructor(
        private auth: AuthService,
        private router: Router
    ) { }

    ngOnInit(): void {
        if (!this.auth.isAuthenticated()) {
            this.router.navigate(['/auth/login']);
            return;
        }
        if (this.auth.isAdmin()) {
            this.router.navigate(['/admin/dashboard']);
            return;
        }
        this.router.navigate(['/client/espace']);
    }
}
