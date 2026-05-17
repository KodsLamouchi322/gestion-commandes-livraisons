import { ErrorHandler, Injectable, Injector } from '@angular/core';
import { NotificationService } from './notification.service';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
    
    constructor(private injector: Injector) {}

    handleError(error: any): void {
        // Log l'erreur dans la console
        console.error('🔴 Erreur globale capturée:', error);

        // Obtenir le service de notification via l'injector pour éviter les dépendances circulaires
        const notificationService = this.injector.get(NotificationService);

        // Extraire le message d'erreur
        let message = 'Une erreur inattendue est survenue';
        
        if (error?.error?.message) {
            message = error.error.message;
        } else if (error?.message) {
            message = error.message;
        } else if (typeof error === 'string') {
            message = error;
        }

        // Afficher la notification d'erreur
        notificationService.error(message);

        // Ne pas re-lancer : un rethrow ici peut casser la zone Angular et laisser
        // les vues admin sans rafraîchissement jusqu’à une interaction (double-clic).
        console.error('Détail:', error);
    }
}
