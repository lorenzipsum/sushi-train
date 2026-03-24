import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL, buildApiUrl } from './http/api-config';
import type { VersionInfoDto } from './types';

@Injectable({ providedIn: 'root' })
export class VersionApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getVersionInfo(): Observable<VersionInfoDto> {
    return this.http.get<VersionInfoDto>(buildApiUrl(this.baseUrl, '/api/version'));
  }
}
