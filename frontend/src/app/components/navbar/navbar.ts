import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.html',
  styleUrls: ['./navbar.css'],
  standalone: false
})
export class Navbar implements OnInit, OnDestroy {
  private authSub?: Subscription;

  constructor(
    public auth: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.updateLayoutClass();
    this.authSub = this.auth.user$.subscribe(() => this.updateLayoutClass());
  }

  ngOnDestroy(): void {
    this.authSub?.unsubscribe();
    document.body.classList.remove('admin-layout');
  }

  private updateLayoutClass(): void {
    const isAdmin = this.auth.isAuthenticated() && this.auth.isAdmin();
    document.body.classList.toggle('admin-layout', isAdmin);
  }

  goToProfile(): void {
    if (this.auth.isAdmin()) {
      this.router.navigate(['/admin/dashboard']);
      return;
    }
    this.router.navigate(['/client/espace']);
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/auth/login']);
    this.updateLayoutClass();
  }
}
