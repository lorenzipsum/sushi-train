import { TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { BeltsApi } from '../api/belts.api';
import type { BeltDto, BeltSnapshotDto, SeatStateListDto } from '../api/types';
import { BeltVisualizationStore } from './belt-visualization.store';

function createBelt(overrides: Partial<BeltDto> = {}): BeltDto {
  return {
    id: 'belt-1',
    name: 'Main Belt',
    ...overrides,
  };
}

function createSnapshot(overrides: Partial<BeltSnapshotDto> = {}): BeltSnapshotDto {
  return {
    beltId: 'belt-1',
    beltName: 'Main Belt',
    beltSlotCount: 8,
    beltBaseRotationOffset: 0,
    beltOffsetStartedAt: '2026-03-14T10:00:00.000Z',
    beltTickIntervalMs: 500,
    beltSpeedSlotsPerTick: 1,
    slots: [{ slotId: 'slot-1', positionIndex: 0 }],
    ...overrides,
  };
}

function createMatchMedia(matches: boolean): MediaQueryList {
  return {
    matches,
    media: '(prefers-reduced-motion: reduce)',
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  } as unknown as MediaQueryList;
}

describe('BeltVisualizationStore', () => {
  let reducedMotionMatches = false;

  beforeEach(() => {
    reducedMotionMatches = false;
    vi.useFakeTimers();
    vi.stubGlobal(
      'requestAnimationFrame',
      vi.fn(() => 1),
    );
    vi.stubGlobal('cancelAnimationFrame', vi.fn());
    vi.stubGlobal(
      'setInterval',
      vi.fn(() => 1),
    );
    vi.stubGlobal('clearInterval', vi.fn());
    Object.defineProperty(window, 'matchMedia', {
      configurable: true,
      writable: true,
      value: vi.fn().mockImplementation(() => createMatchMedia(reducedMotionMatches)),
    });
  });

  afterEach(() => {
    TestBed.resetTestingModule();
    vi.useRealTimers();
    vi.restoreAllMocks();
    vi.unstubAllGlobals();
  });

  it('keeps loading until the first authoritative snapshot arrives', () => {
    const belts$ = new Subject<BeltDto[]>();
    const snapshot$ = new Subject<BeltSnapshotDto>();
    const seats$ = new Subject<SeatStateListDto>();
    const beltsApiMock = {
      getAllBelts: vi.fn(() => belts$.asObservable()),
      getBeltSnapshot: vi.fn(() => snapshot$.asObservable()),
      getSeatOverview: vi.fn(() => seats$.asObservable()),
    };

    TestBed.configureTestingModule({
      providers: [BeltVisualizationStore, { provide: BeltsApi, useValue: beltsApiMock }],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    expect(store.isLoading()).toBe(true);

    belts$.next([createBelt()]);
    expect(store.isLoading()).toBe(true);

    snapshot$.next(createSnapshot());
    expect(store.isLoading()).toBe(false);

    seats$.next([]);
    expect(store.isLoading()).toBe(false);
  });

  it('reports when no belts are returned', () => {
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([])),
      getBeltSnapshot: vi.fn(),
      getSeatOverview: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [BeltVisualizationStore, { provide: BeltsApi, useValue: beltsApiMock }],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    expect(store.hasNoBelts()).toBe(true);
    expect(beltsApiMock.getBeltSnapshot).not.toHaveBeenCalled();
    expect(beltsApiMock.getSeatOverview).not.toHaveBeenCalled();
  });

  it('surfaces paused state when the belt speed is zero', () => {
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi.fn(() => of(createSnapshot({ beltSpeedSlotsPerTick: 0 }))),
      getSeatOverview: vi.fn(() => of([])),
    };

    TestBed.configureTestingModule({
      providers: [BeltVisualizationStore, { provide: BeltsApi, useValue: beltsApiMock }],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    expect(store.isPaused()).toBe(true);
    expect(store.movementLabel()).toBe('Paused at the belt');
    expect(store.speedLabel()).toBe('Paused');
  });

  it('honors reduced-motion mode from the browser preference', () => {
    reducedMotionMatches = true;

    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi.fn(() => of(createSnapshot())),
      getSeatOverview: vi.fn(() => of([])),
    };

    TestBed.configureTestingModule({
      providers: [BeltVisualizationStore, { provide: BeltsApi, useValue: beltsApiMock }],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    expect(store.isReducedMotion()).toBe(true);
    expect(store.movementLabel()).toBe('Reduced motion view');
  });

  it('keeps the last good seat state when a later seat refresh fails after a write-triggered refresh', () => {
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi
        .fn()
        .mockReturnValueOnce(of(createSnapshot()))
        .mockReturnValueOnce(of(createSnapshot({ beltName: 'Main Belt Updated' }))),
      getSeatOverview: vi
        .fn()
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
        )
        .mockReturnValueOnce(throwError(() => new Error('Seat refresh failed'))),
    };

    TestBed.configureTestingModule({
      providers: [BeltVisualizationStore, { provide: BeltsApi, useValue: beltsApiMock }],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    expect(store.beltName()).toBe('Main Belt');
    expect(store.occupiedSeatCount()).toBe(1);
    expect(store.isDegraded()).toBe(false);

    store.refreshAfterWrite();

    expect(beltsApiMock.getBeltSnapshot).toHaveBeenCalledTimes(2);
    expect(beltsApiMock.getSeatOverview).toHaveBeenCalledTimes(2);
    expect(store.beltName()).toBe('Main Belt Updated');
    expect(store.occupiedSeatCount()).toBe(1);
    expect(store.isDegraded()).toBe(true);
    expect(store.freshnessLabel()).toContain('Showing last good update');
  });
});
