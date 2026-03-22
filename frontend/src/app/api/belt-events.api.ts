import { inject, Injectable } from '@angular/core';

import { API_BASE_URL, buildApiUrl } from './http/api-config';

@Injectable({ providedIn: 'root' })
export class BeltEventsApi {
  private readonly baseUrl = inject(API_BASE_URL);

  openBeltEvents(beltId: string): EventSource {
    return new EventSource(buildApiUrl(this.baseUrl, `/api/v1/belts/${beltId}/events`));
  }
}
