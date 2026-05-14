import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isAuthenticated() && auth.isAdmin()) {
        return true;
    }
    router.navigate(['/auth/login'], { queryParams: { returnUrl: router.url } });
    return false;
};

export const clientGuard: CanActivateFn = () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    if (auth.isAuthenticated() && auth.isClient()) {
        return true;
    }
    router.navigate(['/auth/login'], { queryParams: { returnUrl: router.url } });
    return false;
};
