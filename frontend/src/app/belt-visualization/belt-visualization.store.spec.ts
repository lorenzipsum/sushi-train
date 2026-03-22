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

function createCheckedOutSeatOrder(overrides: Partial<SeatOrderDto> = {}): SeatOrderDto {
  return {
    ...createSeatOrder(),
    isOccupied: false,
    orderSummary: {
      orderId: 'order-1',
      seatId: 'seat-1',
      status: 'CHECKED_OUT',
      createdAt: '2026-03-15T03:00:00Z',
      closedAt: '2026-03-15T03:42:00Z',
      lines: [],
      totalPrice: 0,
      ...overrides.orderSummary,
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
      checkout: vi.fn(),
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
      checkout: vi.fn(),
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
      checkout: vi.fn(),
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
      checkout: vi.fn(),
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
      checkout: vi.fn(),
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
      checkout: vi.fn(),
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
      checkout: vi.fn(),
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
      checkout: vi.fn(),
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

  it('checks out an occupied seat, frees it, and preserves the final checkout summary', () => {
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi
        .fn()
        .mockReturnValueOnce(of(createSnapshot()))
        .mockReturnValueOnce(of(createSnapshot())),
      getSeatOverview: vi
        .fn()
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
        )
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: false }]),
        ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(),
      checkout: vi.fn(() => of(createCheckedOutSeatOrder())),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    store.checkoutSeat('seat-1');

    expect(seatsApiMock.checkout).toHaveBeenCalledWith('seat-1');
    expect(store.checkoutPendingSeatId()).toBeNull();
    expect(store.checkoutFeedback()?.tone).toBe('success');
    expect(store.checkoutFeedback()?.statusLabel).toBe('CHECKED_OUT');
    expect(store.checkoutFeedback()?.totalPriceLabel).toBe('0 Yen');
    expect(store.checkoutFeedback()?.finalSummary?.orderSummary?.orderId).toBe('order-1');
    expect(store.stageViewModel()?.seats[0].isOccupied).toBe(false);
    expect(store.stageViewModel()?.seats[0].orderId).toBeNull();
  });

  it('accepts empty-order checkout without blocking on zero total or missing lines', () => {
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi
        .fn()
        .mockReturnValueOnce(of(createSnapshot()))
        .mockReturnValueOnce(of(createSnapshot())),
      getSeatOverview: vi
        .fn()
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
        )
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: false }]),
        ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(),
      checkout: vi.fn(() =>
        of(
          createCheckedOutSeatOrder({
            orderSummary: {
              orderId: 'order-1',
              seatId: 'seat-1',
              status: 'CHECKED_OUT',
              createdAt: '2026-03-15T03:00:00Z',
              closedAt: '2026-03-15T03:42:00Z',
              lines: [],
              totalPrice: 0,
            },
          }),
        ),
      ),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    store.checkoutSeat('seat-1');

    expect(store.checkoutFeedback()?.detail).toContain('No plates were recorded');
    expect(store.checkoutFeedback()?.finalSummary?.orderSummary?.lines).toEqual([]);
    expect(store.checkoutFeedback()?.finalSummary?.orderSummary?.totalPrice).toBe(0);
  });

  it('reports stale checkout conflicts and refreshes the seat as free', () => {
    const conflict = {
      error: {
        status: 409,
        detail: 'Seat seat-1 has no open order.',
        title: 'Seat not occupied',
        errorCode: 'SEAT_NOT_OCCUPIED',
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
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
        )
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: false }]),
        ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(),
      checkout: vi.fn(() => throwError(() => conflict)),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    store.checkoutSeat('seat-1');

    expect(store.checkoutFeedback()?.tone).toBe('error');
    expect(store.checkoutFeedback()?.title).toContain('already free');
    expect(store.stageViewModel()?.seats[0].isOccupied).toBe(false);
    expect(seatsApiMock.getSeatState).toHaveBeenCalledTimes(1);
  });

  it('reports when checkout targets a missing seat and refreshes backend state', () => {
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
          of([{ seatId: 'seat-404', label: 'Seat 404', positionIndex: 0, isOccupied: true }]),
        )
        .mockReturnValueOnce(of([])),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(),
      checkout: vi.fn(() => throwError(() => notFound)),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    store.checkoutSeat('seat-404');

    expect(store.checkoutFeedback()?.tone).toBe('error');
    expect(store.checkoutFeedback()?.title).toContain('could not be found');
    expect(beltsApiMock.getSeatOverview).toHaveBeenCalledTimes(2);
  });

  it('keeps the final checkout summary during later in-app refresh and reconcile flows', () => {
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi
        .fn()
        .mockReturnValueOnce(of(createSnapshot()))
        .mockReturnValueOnce(of(createSnapshot()))
        .mockReturnValueOnce(of(createSnapshot({ beltName: 'Main Belt Updated' }))),
      getSeatOverview: vi
        .fn()
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
        )
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: false }]),
        )
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: false }]),
        ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(),
      checkout: vi.fn(() => of(createCheckedOutSeatOrder())),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    store.checkoutSeat('seat-1');
    store.refreshAfterWrite();

    expect(store.checkoutFeedback()?.finalSummary?.orderSummary?.orderId).toBe('order-1');
    expect(store.checkoutFeedback()?.statusLabel).toBe('CHECKED_OUT');
    expect(store.beltName()).toBe('Main Belt Updated');
  });

  it('restores the previously selected occupied seat and hydrates all occupied seats on load', () => {
    sessionStorage.setItem('sushi-train:selected-seat-id', 'seat-2');

    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi.fn(() => of(createSnapshot())),
      getSeatOverview: vi.fn(() =>
        of([
          { seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true },
          { seatId: 'seat-2', label: 'Seat 2', positionIndex: 1, isOccupied: true },
        ]),
      ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn((seatId: string) =>
        of(
          createSeatOrder({
            seatId,
            label: seatId === 'seat-2' ? 'Seat 2' : 'Seat 1',
            positionIndex: seatId === 'seat-2' ? 1 : 0,
            orderSummary: {
              orderId: `order-${seatId}`,
              seatId,
              status: 'OPEN',
              createdAt: '2026-03-15T03:00:00Z',
              lines: [],
              totalPrice: 0,
            },
          }),
        ),
      ),
      checkout: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    expect(store.selectedSeatId()).toBe('seat-2');
    expect(seatsApiMock.getSeatState).toHaveBeenCalledTimes(2);
    expect(store.selectedSeatDetail()?.seatId).toBe('seat-2');
    expect(store.selectedSeatDetail()?.canPickPlates).toBe(true);
    expect(store.selectedSeatDetail()?.restorationStatus).toBe('occupied');
  });

  it('blocks plate picking with syncing feedback while occupied seat hydration is still in progress', () => {
    const pendingSeatState$ = new Subject<SeatOrderDto>();
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi.fn(() =>
        of(
          createSnapshot({
            slots: [
              {
                slotId: 'slot-1',
                positionIndex: 0,
                plate: { plateId: 'plate-1', menuItemName: 'Salmon Nigiri', tier: 'RED' },
              },
            ],
          }),
        ),
      ),
      getSeatOverview: vi.fn(() =>
        of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
      ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(() => pendingSeatState$.asObservable()),
      checkout: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    store.pickPlate('plate-1');

    expect(store.pickPlateFeedback()?.outcomeType).toBe('syncing');
    expect(store.pickPlateFeedback()?.title).toContain('still syncing');
    expect(store.selectedSeatDetail()?.blockedReason).toBe('syncing');
  });

  it('retries occupied-seat hydration automatically after a temporary failure', () => {
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi.fn(() => of(createSnapshot())),
      getSeatOverview: vi.fn(() =>
        of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
      ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi
        .fn()
        .mockReturnValueOnce(throwError(() => new Error('Temporary failure')))
        .mockReturnValueOnce(of(createSeatOrder())),
      checkout: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    expect(store.selectedSeatDetail()?.restorationStatus).toBe('unresolved');

    vi.advanceTimersByTime(1500);

    expect(seatsApiMock.getSeatState).toHaveBeenCalledTimes(2);
    expect(store.selectedSeatDetail()?.restorationStatus).toBe('occupied');
    expect(store.selectedSeatDetail()?.canPickPlates).toBe(true);
  });

  it('shows the final checkout summary in the selected-seat detail after checkout succeeds', () => {
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi
        .fn()
        .mockReturnValueOnce(of(createSnapshot()))
        .mockReturnValueOnce(of(createSnapshot())),
      getSeatOverview: vi
        .fn()
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
        )
        .mockReturnValueOnce(
          of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: false }]),
        ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(),
      checkout: vi.fn(() => of(createCheckedOutSeatOrder())),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    store.checkoutSeat('seat-1');

    expect(store.selectedSeatDetail()?.isCheckoutSummary).toBe(true);
    expect(store.selectedSeatDetail()?.statusLabel).toBe('Checked out');
    expect(store.selectedSeatDetail()?.orderSummary?.orderId).toBe('order-1');
  });

  it('reconciles an initially occupied seat to available when hydration finds no active order', () => {
    const beltsApiMock = {
      getAllBelts: vi.fn(() => of([createBelt()])),
      getBeltSnapshot: vi.fn(() => of(createSnapshot())),
      getSeatOverview: vi.fn(() =>
        of([{ seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true }]),
      ),
    };
    const seatsApiMock = {
      occupySeat: vi.fn(),
      getSeatState: vi.fn(() =>
        of({
          seatId: 'seat-1',
          label: 'Seat 1',
          positionIndex: 0,
          isOccupied: false,
          orderSummary: undefined,
        }),
      ),
      checkout: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        BeltVisualizationStore,
        { provide: BeltsApi, useValue: beltsApiMock },
        { provide: SeatsApi, useValue: seatsApiMock },
      ],
    });
    const store = TestBed.inject(BeltVisualizationStore);

    expect(store.stageViewModel()?.seats[0].isOccupied).toBe(false);
    expect(store.selectedSeatDetail()?.restorationStatus).toBe('available');
    expect(store.selectedSeatDetail()?.canStartDining).toBe(true);
    expect(store.selectedSeatDetail()?.canPickPlates).toBe(false);
    expect(store.selectedSeatDetail()?.helperLabel).toContain(
      'Start dining here when you are ready',
    );
  });
});
