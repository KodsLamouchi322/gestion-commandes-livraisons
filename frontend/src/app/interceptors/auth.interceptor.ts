import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(
        private auth: AuthService,
        private router: Router
    ) { }

    intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        let headers: Record<string, string> = {};

        // Ajouter le token JWT si disponible
        const token = this.auth.getToken();
        if (token && !req.url.includes('/auth/login') && !req.url.includes('/auth/register')) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        // Désactiver le cache navigateur sur les requêtes GET
        // (corrige le bug: suppression réapparaît, ajout vu seulement après F5)
        if (req.method === 'GET') {
            headers['Cache-Control'] = 'no-cache, no-store, must-revalidate';
            headers['Pragma'] = 'no-cache';
            headers['Expires'] = '0';
        }

        if (Object.keys(headers).length > 0) {
            req = req.clone({ setHeaders: headers });
        }

        return next.handle(req).pipe(
            catchError((error: HttpErrorResponse) => {
                // 401 = session invalide ou expirée. 403 = souvent « interdit pour ce rôle »
                // (ne pas déconnecter sur 403 : cela cassait l’admin et laissait l’UI bloquée en chargement.)
                if (error.status === 401) {
                    console.error('🔒 Session invalide ou expirée (401)');
                    this.auth.logout();
                    this.router.navigate(['/auth/login'], {
                        queryParams: { returnUrl: this.router.url }
                    });
                }
                return throwError(() => error);
            })
        );
    }
}

