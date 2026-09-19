import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Alert } from './models/alert';
import { AlertService } from './services/alert.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private readonly alertService = inject(AlertService);
  private readonly platformId = inject(PLATFORM_ID);

  alerts: Alert[] = [];
  selectedAlert: Alert | null = null;
  username = '';
  password = '';
  statusFilter = 'ALL';
  ruleFilter = 'ALL';
  error = '';
  loading = false;
  authenticated = false;

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.username = sessionStorage.getItem('sentinel.username') ?? '';
      sessionStorage.removeItem('sentinel.authenticated');
    }
  }

  login(): void {
    this.error = '';
    this.username = this.username.trim();
    if (!this.username || !this.password) {
      this.error = 'Enter the analyst username and API password.';
      return;
    }
    this.authenticated = false;
    this.loadAlerts();
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.removeItem('sentinel.username');
    }
    this.authenticated = false;
    this.password = '';
    this.alerts = [];
    this.selectedAlert = null;
  }

  loadAlerts(): void {
    this.loading = true;
    this.error = '';
    this.alertService.getAlerts(this.username, this.password).subscribe({
      next: (alerts) => {
        this.alerts = alerts.sort((left, right) => right.score - left.score);
        this.authenticated = true;
        if (isPlatformBrowser(this.platformId)) {
          sessionStorage.setItem('sentinel.username', this.username);
        }
        this.loading = false;
      },
      error: (response) => {
        this.loading = false;
        this.authenticated = false;
        if (isPlatformBrowser(this.platformId)) {
          sessionStorage.removeItem('sentinel.authenticated');
        }
        this.error = response.status === 401
          ? 'Authentication failed. Check the API credentials.'
          : 'Unable to load alerts. Confirm that the backend is running.';
      },
    });
  }

  selectAlert(alert: Alert): void {
    this.selectedAlert = alert;
  }

  clearSelectedAlert(): void {
    if (!this.selectedAlert) return;
    const reason = isPlatformBrowser(this.platformId) ? window.prompt('Disposition reason')?.trim() : undefined;
    if (!reason) return;
    this.alertService.updateStatus(this.selectedAlert.id, 'CLEARED', this.username, reason,
      this.username, this.password).subscribe({
        next: (updated) => {
          this.alerts = this.alerts.map((alert) => alert.id === updated.id ? updated : alert);
          this.selectedAlert = updated;
        },
        error: () => this.error = 'Unable to update the alert status.',
      });
  }

  get filteredAlerts(): Alert[] {
    return this.alerts.filter((alert) => {
      const statusMatches = this.statusFilter === 'ALL' || alert.status === this.statusFilter;
      const ruleMatches = this.ruleFilter === 'ALL' || alert.ruleType.includes(this.ruleFilter);
      return statusMatches && ruleMatches;
    });
  }

  get openCount(): number {
    return this.alerts.filter((alert) => alert.status === 'OPEN').length;
  }

  get highRiskCount(): number {
    return this.alerts.filter((alert) => alert.score >= 80 && alert.status !== 'CLEARED').length;
  }

  get ruleTypes(): string[] {
    return [...new Set(this.alerts.flatMap((alert) => alert.ruleType.split(',')))].sort();
  }

  trackAlert(_index: number, alert: Alert): number {
    return alert.id;
  }
}
