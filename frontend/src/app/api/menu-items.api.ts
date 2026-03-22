import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL, buildApiUrl } from './http/api-config';
import type { PagedMenuItemDto } from './types';

@Injectable({ providedIn: 'root' })
export class MenuItemsApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getMenuItems(page = 0, size = 200): Observable<PagedMenuItemDto> {
    return this.http.get<PagedMenuItemDto>(
      buildApiUrl(this.baseUrl, `/api/v1/menu-items?page=${page}&size=${size}`),
    );
  }
}
