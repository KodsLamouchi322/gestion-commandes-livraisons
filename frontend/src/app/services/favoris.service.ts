import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Produit } from '../models/models';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class FavorisService {

    private readonly ids$ = new BehaviorSubject<number[]>([]);
    private storageKey = 'favoris_anon';

    constructor(private auth: AuthService) {
        this.setStorageKeyFromAuth();
        this.ids$.next(this.load());

        // Recharger les favoris quand l'utilisateur change (login/logout)
        this.auth.user$.subscribe(() => {
            this.setStorageKeyFromAuth();
            this.ids$.next(this.load());
        });
    }

    get favorisIds$() { return this.ids$.asObservable(); }

    get count(): number { return this.ids$.value.length; }

    isFavori(id: number): boolean {
        return this.ids$.value.includes(id);
    }

    toggle(produit: Produit): void {
        if (!produit.id) return;
        const ids = this.ids$.value;
        const next = ids.includes(produit.id)
            ? ids.filter(i => i !== produit.id)
            : [...ids, produit.id];
        localStorage.setItem(this.storageKey, JSON.stringify(next));
        this.ids$.next(next);
    }

    getFavorisIds(): number[] {
        return this.ids$.value;
    }

    private load(): number[] {
        try {
            const raw = localStorage.getItem(this.storageKey);
            const parsed = JSON.parse(raw || '[]');
            return Array.isArray(parsed) ? parsed.filter((x) => typeof x === 'number') : [];
        } catch { return []; }
    }

    private setStorageKeyFromAuth(): void {
        const clientId = this.auth.currentUser?.clientId ?? this.auth.currentUser?.id;
        this.storageKey = clientId ? `favoris_${clientId}` : 'favoris_anon';
    }
}
