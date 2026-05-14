import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Produit } from '../models/models';

const KEY = 'favoris';

@Injectable({ providedIn: 'root' })
export class FavorisService {

    private ids$ = new BehaviorSubject<number[]>(this.load());

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
        localStorage.setItem(KEY, JSON.stringify(next));
        this.ids$.next(next);
    }

    getFavorisIds(): number[] {
        return this.ids$.value;
    }

    private load(): number[] {
        try {
            return JSON.parse(localStorage.getItem(KEY) || '[]');
        } catch { return []; }
    }
}
