import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { AuthResponse, UserRole } from '../models/models';

const AUTH_KEY = 'auth';
const TOKEN_KEY = 'token';

export interface StoredAuth extends AuthResponse { }

@Injectable({ providedIn: 'root' })
export class AuthService {

    private readonly baseUrl = '/api';

    private readonly currentUser$ = new BehaviorSubject<StoredAuth | null>(this.readFromStorage());

    constructor(private http: HttpClient) { }

    private readFromStorage(): StoredAuth | null {
        const raw = localStorage.getItem(AUTH_KEY);
        if (!raw) {
            return null;
        }
        try {
            return JSON.parse(raw) as StoredAuth;
        } catch {
            return null;
        }
    }

    get currentUser(): StoredAuth | null {
        const user = this.currentUser$.value;
        // Assurer la compatibilité clientId
        if (user && !user.clientId && user.id) {
            user.clientId = user.id;
        }
        return user;
    }

    get user$(): Observable<StoredAuth | null> {
        return this.currentUser$.asObservable();
    }

    getToken(): string | null {
        return localStorage.getItem(TOKEN_KEY);
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }

    isAdmin(): boolean {
        return this.currentUser?.role === 'ADMIN';
    }

    isClient(): boolean {
        return this.currentUser?.role === 'CLIENT';
    }

    login(email: string, motDePasse: string): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.baseUrl}/auth/login`, { email, motDePasse }).pipe(
            tap(res => this.persistSession(res))
        );
    }

    register(nom: string, email: string, adresse: string, motDePasse: string): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.baseUrl}/auth/register`, { nom, email, adresse, motDePasse }).pipe(
            tap(res => this.persistSession(res))
        );
    }

    private persistSession(res: AuthResponse): void {
        localStorage.setItem(TOKEN_KEY, res.token);
        localStorage.setItem(AUTH_KEY, JSON.stringify(res));
        this.currentUser$.next(res);
    }

    logout(): void {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(AUTH_KEY);
        localStorage.removeItem('clientConnecte');
        this.currentUser$.next(null);
    }
}
