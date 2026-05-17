import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Notification } from '../../services/notification.service';

@Component({
  selector: 'app-notification',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="notification.message" 
         class="notification notification-{{notification.type}}"
         >
      <span>{{ notification.message }}</span>
      <button (click)="close()" class="close-btn">&times;</button>
    </div>
  `,
  styles: [`
    .notification {
      position: fixed;
      top: 20px;
      right: 20px;
      padding: 15px 20px;
      border-radius: 4px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.2);
      z-index: 9999;
      display: flex;
      align-items: center;
      gap: 10px;
      min-width: 300px;
      animation: slideIn 0.3s ease-out;
    }

    @keyframes slideIn {
      from {
        transform: translateX(400px);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }

    .notification-success {
      background-color: #4caf50;
      color: white;
    }

    .notification-error {
      background-color: #f44336;
      color: white;
    }

    .notification-info {
      background-color: #2196f3;
      color: white;
    }

    .notification-warning {
      background-color: #ff9800;
      color: white;
    }

    .close-btn {
      background: none;
      border: none;
      color: white;
      font-size: 24px;
      cursor: pointer;
      padding: 0;
      margin-left: auto;
    }

    .close-btn:hover {
      opacity: 0.8;
    }
  `]
})
export class NotificationComponent implements OnInit {
  notification: Notification = { message: '', type: 'info' };

  constructor(
    private notificationService: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.notificationService.notification$.subscribe((notification) => {
      this.notification = notification;
      this.cdr.detectChanges();
    });
  }

  close() {
    this.notification = { message: '', type: 'info' };
  }
}
