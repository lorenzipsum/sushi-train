import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL, buildApiUrl } from './http/api-config';
import type { PickPlateRequest, SeatOrderDto, SeatStateDto } from './types';

@Injectable({ providedIn: 'root' })
export class SeatsApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  private buildSeatUrl(seatId: string, suffix = ''): string {
    return buildApiUrl(this.baseUrl, `/api/v1/seats/${seatId}${suffix}`);
  }

  getSeatState(seatId: string): Observable<SeatOrderDto> {
    return this.http.get<SeatOrderDto>(this.buildSeatUrl(seatId));
  }

  occupySeat(seatId: string): Observable<SeatStateDto> {
    return this.http.post<SeatStateDto>(this.buildSeatUrl(seatId, '/occupy'), {});
  }

  pickPlate(seatId: string, request: PickPlateRequest): Observable<SeatOrderDto> {
    return this.http.post<SeatOrderDto>(this.buildSeatUrl(seatId, '/order-lines'), request);
  }

  checkout(seatId: string): Observable<SeatOrderDto> {
    return this.http.post<SeatOrderDto>(this.buildSeatUrl(seatId, '/checkout'), {});
  }
}
