import { TestBed } from '@angular/core/testing';
import { Subject, of, throwError } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { BeltsApi } from '../api/belts.api';
import { SeatsApi } from '../api/seats.api';
import type { BeltDto, BeltSnapshotDto, SeatOrderDto, SeatStateListDto } from '../api/types';
import { BeltVisualizationStore } from './belt-visualization.store';

function createBelt(overrides: Partial<BeltDto> = {}): BeltDto {
  return {
    id: 'belt-1',
    name: 'Main Belt',
    ...overrides,
  };
}

function createSeatOrder(overrides: Partial<SeatOrderDto> = {}): SeatOrderDto {
  return {
    seatId: 'seat-1',
    label: 'Seat 1',
    positionIndex: 0,
    isOccupied: true,
    orderSummary: {
      orderId: 'order-1',
      seatId: 'seat-1',
      status: 'OPEN',
      createdAt: '2026-03-15T03:00:00Z',
      closedAt: undefined,
      lines: [],
      totalPrice: 0,
    },
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
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
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
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
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
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    expect(store.isPaused()).toBe(true);
    expect(store.movementLabel()).toBe('Paused at the counter');
    expect(store.speedLabel()).toBe('Paused');
  });

  it('honors reduced-motion mode from the browser preference', () => {
    reducedMotionMatches = true;

    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi.fn(() => of(createSnapshot())),
      getSeatOverview: vi.fn(() => of([])),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
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
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
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

  it('occupies a free seat, reconciles seat detail, and exposes durable order context', () => {
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi
        .fn()
        .mockReturnValueOnce(of(createSnapshot()))
        .mockReturnValueOnce(of(createSnapshot())),
      getSeatOverview: vi
        .fn()
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: false }]),
        )
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
        ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(() =>
        of({ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }),
      ),
      getSeatState: vi.fn(() => of(createSeatOrder())),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    store.occupySeat('seat-1');

    expect(seatsApiMock.occupySeat).toHaveBeenCalledWith('seat-1');
    expect(seatsApiMock.getSeatState).toHaveBeenCalledWith('seat-1');
    expect(store.occupyPendingSeatId()).toBeNull();
    expect(store.occupyFeedback()?.tone).toBe('success');
    expect(store.occupyFeedback()?.orderId).toBe('order-1');
    expect(store.stageViewModel()?.seats[0].isOccupied).toBe(true);
    expect(store.stageViewModel()?.seats[0].orderId).toBe('order-1');
  });

  it('does not report full occupy success when seat-detail reconciliation fails', () => {
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi
        .fn()
        .mockReturnValueOnce(of(createSnapshot()))
        .mockReturnValueOnce(of(createSnapshot())),
      getSeatOverview: vi
        .fn()
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: false }]),
        )
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
        ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(() =>
        of({ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }),
      ),
      getSeatState: vi.fn(() => throwError(() => new Error('Seat detail unavailable'))),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    store.occupySeat('seat-1');

    expect(store.occupyPendingSeatId()).toBeNull();
    expect(store.occupyFeedback()?.tone).toBe('error');
    expect(store.occupyFeedback()?.title).toContain('still syncing');
    expect(store.occupyFeedback()?.orderId).toBeNull();
    expect(store.stageViewModel()?.seats[0].isOccupied).toBe(true);
    expect(store.stageViewModel()?.seats[0].orderId).toBeNull();
  });

  it('reports a seat-already-occupied conflict and reconciles the occupied seat state', () => {
    const conflict = {
      error: {
        status: 409,
        detail: 'Seat seat-1 already has an open order.',
        title: 'Seat already occupied',
      },
    };
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi
        .fn()
        .mockReturnValueOnce(of(createSnapshot()))
        .mockReturnValueOnce(of(createSnapshot())),
      getSeatOverview: vi
        .fn()
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: false }]),
        )
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
        ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(() => throwError(() => conflict)),
      getSeatState: vi.fn(() => of(createSeatOrder())),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    store.occupySeat('seat-1');

    expect(store.occupyFeedback()?.tone).toBe('error');
    expect(store.occupyFeedback()?.title).toContain('already taken');
    expect(store.stageViewModel()?.seats[0].isOccupied).toBe(true);
    expect(store.stageViewModel()?.seats[0].orderId).toBe('order-1');
  });

  it('reports when a requested seat no longer exists and refreshes backend state', () => {
    const notFound = {
      error: {
        status: 404,
        detail: 'Seat not found: seat-404',
        title: 'Resource not found',
      },
    };
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi
        .fn()
        .mockReturnValueOnce(of(createSnapshot()))
        .mockReturnValueOnce(of(createSnapshot())),
      getSeatOverview: vi
        .fn()
        .mockReturnValueOnce(
          of([{ seatId: 'seat-404', label: 'Seat 404', positionIndex: 0, isOccupied: false }]),
        )
        .mockReturnValueOnce(of([])),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(() => throwError(() => notFound)),
      getSeatState: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    store.occupySeat('seat-404');

    expect(store.occupyFeedback()?.tone).toBe('error');
    expect(store.occupyFeedback()?.title).toContain('could not be found');
    expect(seatsApiMock.getSeatState).not.toHaveBeenCalled();
    expect(beltsApiMock.getSeatOverview).toHaveBeenCalledTimes(2);
  });
});
