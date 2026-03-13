import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { API_BASE_URL } from './http/api-config';
import type { PickPlateRequest, SeatOrderDto, SeatStateDto } from './types';

@Injectable({ providedIn: 'root' })
export class SeatsApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = inject(API_BASE_URL);

  getSeatState(seatId: string): Observable<SeatStateDto> {
    return this.http.get<SeatStateDto>(`${this.baseUrl}/api/v1/seats/${seatId}`);
  }

  occupySeat(seatId: string): Observable<SeatStateDto> {
    return this.http.post<SeatStateDto>(`${this.baseUrl}/api/v1/seats/${seatId}/occupy`, {});
  }

  pickPlate(seatId: string, request: PickPlateRequest): Observable<SeatOrderDto> {
    return this.http.post<SeatOrderDto>(
      `${this.baseUrl}/api/v1/seats/${seatId}/order-lines`,
      request,
    );
  }

  checkout(seatId: string): Observable<SeatOrderDto> {
    return this.http.post<SeatOrderDto>(`${this.baseUrl}/api/v1/seats/${seatId}/checkout`, {});
  }
}
