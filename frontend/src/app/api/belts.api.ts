import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './http/api-config';
import type { BeltListDto, BeltSnapshotDto, FullBeltDto, SeatStateListDto } from './types';

@Injectable({ providedIn: 'root' })
export class BeltsApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getAllBelts(): Observable<BeltListDto> {
    return this.http.get<BeltListDto>(`${this.baseUrl}/api/v1/belts`);
  }

  getBelt(beltId: string): Observable<FullBeltDto> {
    return this.http.get<FullBeltDto>(`${this.baseUrl}/api/v1/belts/${beltId}`);
  }

  getBeltSnapshot(beltId: string): Observable<BeltSnapshotDto> {
    return this.http.get<BeltSnapshotDto>(`${this.baseUrl}/api/v1/belts/${beltId}/snapshot`);
  }

  getSeatOverview(beltId: string): Observable<SeatStateListDto> {
    return this.http.get<SeatStateListDto>(`${this.baseUrl}/api/v1/belts/${beltId}/seats`);
  }
}
