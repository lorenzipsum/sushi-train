import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it } from 'vitest';

import { App } from './app';
import { BeltsApi } from './api/belts.api';

const beltsApiMock = {
  getAllBelts: () =>
    of([
      {
        id: 'belt-1',
        name: 'Main Belt',
      },
    ]),
  getBeltSnapshot: () =>
    of({
      beltId: 'belt-1',
      beltName: 'Main Belt',
      beltSlotCount: 8,
      beltBaseRotationOffset: 0,
      beltOffsetStartedAt: '2026-03-14T10:00:00.000Z',
      beltTickIntervalMs: 500,
      beltSpeedSlotsPerTick: 1,
      slots: [{ slotId: 'slot-1', positionIndex: 0 }],
    }),
  getSeatOverview: () =>
    of([
      {
        seatId: 'seat-1',
        label: 'Seat 1',
        positionIndex: 0,
        isOccupied: true,
      },
    ]),
};

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [{ provide: BeltsApi, useValue: beltsApiMock }],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('renders the simplified title-led stage shell', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('h1')?.textContent).toContain('Kaitenzushi');
    expect(compiled.querySelector('.counter-stage')).toBeTruthy();
    expect(compiled.querySelector('.support-panels')).toBeFalsy();
    expect(compiled.textContent).toContain('Service pace');
    expect(compiled.textContent).not.toContain('Guest seats');
  });
});
