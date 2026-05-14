import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(private auth: AuthService) { }

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

        return next.handle(req);
    }
}
