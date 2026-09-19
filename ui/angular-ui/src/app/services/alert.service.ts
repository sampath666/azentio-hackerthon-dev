import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Alert } from '../models/alert';

@Injectable({ providedIn: 'root' })
export class AlertService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8081/api/v1/alerts';

  getAlerts(username: string, password: string): Observable<Alert[]> {
    return this.http.get<Alert[]>(this.apiUrl, { headers: this.headers(username, password) });
  }

  updateStatus(id: number, status: Alert['status'], actor: string, reason: string,
               username: string, password: string): Observable<Alert> {
    return this.http.patch<Alert>(`${this.apiUrl}/${id}/status`, { status, actor, reason }, {
      headers: this.headers(username, password),
    });
  }

  private headers(username: string, password: string): HttpHeaders {
    return new HttpHeaders({
      Authorization: `Basic ${btoa(`${username}:${password}`)}`,
    });
  }
}
