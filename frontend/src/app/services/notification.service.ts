import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface Notification {
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private notificationSubject = new Subject<Notification>();
  public notification$ = this.notificationSubject.asObservable();

  // Stocker le timer pour pouvoir l'annuler avant d'en créer un nouveau
  private hideTimer: ReturnType<typeof setTimeout> | null = null;

  success(message: string) {
    this.show(message, 'success');
  }

  error(message: string) {
    this.show(message, 'error');
  }

  info(message: string) {
    this.show(message, 'info');
  }

  warning(message: string) {
    this.show(message, 'warning');
  }

  hide() {
    this.notificationSubject.next({ message: '', type: 'info' });
  }

  private show(message: string, type: 'success' | 'error' | 'info' | 'warning') {
    // Annuler le timer précédent s'il est encore actif
    if (this.hideTimer) {
      clearTimeout(this.hideTimer);
      this.hideTimer = null;
    }

    this.notificationSubject.next({ message, type });

    // Programmer la disparition après 4 secondes
    this.hideTimer = setTimeout(() => {
      this.notificationSubject.next({ message: '', type: 'info' });
      this.hideTimer = null;
    }, 4000);
  }
}
