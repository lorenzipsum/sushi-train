import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { take } from 'rxjs';

import { BeltsApi } from '../api/belts.api';
import { getProblemDetail } from '../api/http/problem-detail';
import { SeatsApi } from '../api/seats.api';
import type {
  BeltDto,
  BeltSnapshotDto,
  OrderLineDto,
  OrderSummaryDto,
  SeatActionProblemDetail,
  SeatOrderDto,
  SeatStateDto,
  SeatStateListDto,
} from '../api/types';
import { buildBeltStageViewModel } from './belt-view-model';
import { getRenderOffset } from './motion';

const POLL_INTERVAL_MS = 3000;

export interface OccupyFeedback {
  tone: 'success' | 'error';
  title: string;
  detail: string;
  seatId: string;
  seatLabel: string;
  orderId: string | null;
  createdAtLabel: string | null;
}

export interface CheckoutFeedback {
  tone: 'success' | 'error';
  title: string;
  detail: string;
  seatId: string;
  seatLabel: string;
  finalSummary: SeatOrderDto | null;
  statusLabel: string | null;
  createdAtLabel: string | null;
  closedAtLabel: string | null;
  totalPriceLabel: string | null;
}

@Injectable({ providedIn: 'root' })
export class BeltVisualizationStore {
  private readonly beltsApi = inject(BeltsApi);
  private readonly seatsApi = inject(SeatsApi);
  private readonly destroyRef = inject(DestroyRef);

  private readonly primaryBelt = signal<BeltDto | null>(null);
  private readonly snapshot = signal<BeltSnapshotDto | null>(null);
  private readonly seats = signal<SeatStateListDto>([]);
  private readonly beltsLoading = signal(true);
  private readonly snapshotLoading = signal(false);
  private readonly seatsLoading = signal(false);
  private readonly beltsError = signal<string | null>(null);
  private readonly snapshotError = signal<string | null>(null);
  private readonly seatsError = signal<string | null>(null);
  private readonly lastSnapshotSuccessAt = signal<number | null>(null);
  private readonly lastSeatsSuccessAt = signal<number | null>(null);
  private readonly now = signal(Date.now());
  private readonly reducedMotion = signal(false);
  private readonly occupyPendingSeatIdState = signal<string | null>(null);
  private readonly occupyFeedbackState = signal<OccupyFeedback | null>(null);
  private readonly checkoutPendingSeatIdState = signal<string | null>(null);
  private readonly checkoutFeedbackState = signal<CheckoutFeedback | null>(null);
  private readonly activeOrdersBySeatId = signal<Record<string, OrderSummaryDto>>({});
  private readonly checkedOutOrdersBySeatId = signal<Record<string, SeatOrderDto>>({});

  private pollTimerId: ReturnType<typeof setInterval> | null = null;
  private animationFrameId: number | null = null;
  private mediaQuery: MediaQueryList | null = null;
  private readonly handleReducedMotionChange = (event: MediaQueryListEvent): void => {
    this.reducedMotion.set(event.matches);
  };

  readonly hasInitialData = computed(() => !!this.snapshot());
  readonly hasNoBelts = computed(
    () => !this.beltsLoading() && !this.primaryBelt() && !this.beltsError(),
  );
  readonly isLoading = computed(
    () =>
      this.beltsLoading() || (!this.snapshot() && (this.snapshotLoading() || this.seatsLoading())),
  );
  readonly isReducedMotion = computed(() => this.reducedMotion());
  readonly stageViewModel = computed(() => {
    const snapshot = this.snapshot();
    if (!snapshot) {
      return null;
    }

    return buildBeltStageViewModel(
      snapshot,
      this.seats(),
      getRenderOffset(snapshot, this.now(), this.reducedMotion()),
      {
        pendingSeatId: this.checkoutPendingSeatIdState() ?? this.occupyPendingSeatIdState(),
        pendingAction: this.checkoutPendingSeatIdState() ? 'checkout' : 'occupy',
        activeOrdersBySeatId: this.activeOrdersBySeatId(),
      },
    );
  });
  readonly occupyPendingSeatId = computed(() => this.occupyPendingSeatIdState());
  readonly occupyFeedback = computed(() => this.occupyFeedbackState());
  readonly checkoutPendingSeatId = computed(() => this.checkoutPendingSeatIdState());
  readonly checkoutFeedback = computed(() => this.checkoutFeedbackState());
  readonly occupyPendingLabel = computed(() => {
    const seatId = this.occupyPendingSeatIdState();
    if (!seatId) {
      return null;
    }

    return this.getSeatLabel(seatId);
  });
  readonly checkoutPendingLabel = computed(() => {
    const seatId = this.checkoutPendingSeatIdState();
    if (!seatId) {
      return null;
    }

    return this.getSeatLabel(seatId);
  });
  readonly isPaused = computed(() => (this.snapshot()?.beltSpeedSlotsPerTick ?? 0) === 0);
  readonly beltName = computed(
    () => this.snapshot()?.beltName ?? this.primaryBelt()?.name ?? 'Sushi belt overview',
  );
  readonly occupiedPlateCount = computed(() => this.stageViewModel()?.occupiedPlateCount ?? 0);
  readonly occupiedSeatCount = computed(
    () => this.seats().filter((seat) => seat.isOccupied).length,
  );
  readonly visibleOccupiedSeats = computed(() =>
    this.seats()
      .filter((seat) => seat.isOccupied)
      .slice(0, 6)
      .map((seat) => seat.label ?? 'Occupied seat'),
  );
  readonly fatalMessage = computed(() => {
    if (this.snapshot()) {
      return null;
    }

    return this.beltsError() ?? this.snapshotError();
  });
  readonly isDegraded = computed(() => !!this.snapshotError() || !!this.seatsError());
  readonly freshnessLabel = computed(() => {
    if (!this.snapshot()) {
      return this.isLoading() ? 'Setting the belt table' : 'Waiting for belt data';
    }

    const lastSuccess = Math.max(this.lastSnapshotSuccessAt() ?? 0, this.lastSeatsSuccessAt() ?? 0);
    if (!lastSuccess) {
      return 'Waiting for live timing';
    }

    const secondsAgo = Math.max(0, Math.round((this.now() - lastSuccess) / 1000));

    if (this.isDegraded()) {
      return `Showing last good update from ${secondsAgo}s ago`;
    }

    return secondsAgo <= 1 ? 'Fresh from the counter' : `Fresh ${secondsAgo}s ago`;
  });
  readonly movementLabel = computed(() => {
    if (!this.snapshot()) {
      return 'Loading movement';
    }

    if (this.reducedMotion()) {
      return 'Reduced motion view';
    }

    return this.isPaused() ? 'Paused at the counter' : 'Gliding around the counter';
  });
  readonly speedLabel = computed(() => {
    const snapshot = this.snapshot();
    if (!snapshot) {
      return 'No timing yet';
    }

    const speed = snapshot.beltSpeedSlotsPerTick ?? 0;
    const tickMs = snapshot.beltTickIntervalMs ?? 0;

    if (speed === 0) {
      return 'Paused';
    }

    return `${speed} slot${speed === 1 ? '' : 's'} every ${tickMs}ms`;
  });

  constructor() {
    this.setupReducedMotion();
    this.startAnimationClock();
    this.loadBelts();

    this.destroyRef.onDestroy(() => {
      if (this.pollTimerId) {
        clearInterval(this.pollTimerId);
      }

      if (this.animationFrameId != null) {
        cancelAnimationFrame(this.animationFrameId);
      }

      this.mediaQuery?.removeEventListener('change', this.handleReducedMotionChange);
    });
  }

  refreshNow(): void {
    const beltId = this.primaryBelt()?.id ?? null;
    if (!beltId) {
      return;
    }

    this.refreshSnapshot(beltId);
    this.refreshSeats(beltId);
  }

  refreshAfterWrite(): void {
    this.refreshNow();
  }

  occupySeat(seatId: string): void {
    const seat = this.findSeat(seatId);
    if (
      !seat ||
      seat.isOccupied ||
      this.occupyPendingSeatIdState() ||
      this.checkoutPendingSeatIdState()
    ) {
      return;
    }

    this.occupyPendingSeatIdState.set(seatId);
    this.occupyFeedbackState.set(null);

    this.seatsApi
      .occupySeat(seatId)
      .pipe(take(1))
      .subscribe({
        next: (result) => {
          this.applySeatState(result);
          this.reconcileSeatDetail(
            seatId,
            (seatOrder) => this.buildSuccessFeedback(seatOrder),
            () => this.buildMissingContextFeedback(result),
          );
        },
        error: (error: unknown) => {
          this.handleOccupyError(seatId, error);
        },
      });
  }

  checkoutSeat(seatId: string): void {
    const seat = this.findSeat(seatId);
    if (
      !seat ||
      !seat.isOccupied ||
      this.occupyPendingSeatIdState() ||
      this.checkoutPendingSeatIdState()
    ) {
      return;
    }

    this.checkoutPendingSeatIdState.set(seatId);
    this.checkoutFeedbackState.set(null);

    this.seatsApi
      .checkout(seatId)
      .pipe(take(1))
      .subscribe({
        next: (result) => {
          this.storeSeatOrderSummary(result);
          this.storeCheckedOutSummary(result);
          this.applySeatState(result);
          this.checkoutPendingSeatIdState.set(null);
          this.checkoutFeedbackState.set(this.buildCheckoutSuccessFeedback(result));
          this.refreshAfterWrite();
        },
        error: (error: unknown) => {
          this.handleCheckoutError(seatId, error);
        },
      });
  }

  private loadBelts(): void {
    this.beltsLoading.set(true);
    this.beltsError.set(null);

    this.beltsApi
      .getAllBelts()
      .pipe(take(1))
      .subscribe({
        next: (belts) => {
          this.beltsLoading.set(false);

          const primaryBelt = belts[0] ?? null;
          this.primaryBelt.set(primaryBelt);

          if (primaryBelt?.id) {
            this.startTrackingPrimaryBelt(primaryBelt.id);
          }
        },
        error: (error: unknown) => {
          this.beltsLoading.set(false);
          this.beltsError.set(this.formatError(error, 'Unable to reach the sushi belt service.'));
        },
      });
  }

  private startTrackingPrimaryBelt(beltId: string): void {
    if (this.pollTimerId) {
      clearInterval(this.pollTimerId);
    }

    this.refreshNow();
    this.pollTimerId = setInterval(() => this.refreshNow(), POLL_INTERVAL_MS);
  }

  private refreshSnapshot(beltId: string): void {
    this.snapshotLoading.set(true);

    this.beltsApi
      .getBeltSnapshot(beltId)
      .pipe(take(1))
      .subscribe({
        next: (snapshot) => {
          this.snapshot.set(snapshot);
          this.snapshotError.set(null);
          this.snapshotLoading.set(false);
          this.lastSnapshotSuccessAt.set(Date.now());
        },
        error: (error: unknown) => {
          this.snapshotLoading.set(false);
          this.snapshotError.set(
            this.formatError(error, 'The belt snapshot could not be refreshed.'),
          );
        },
      });
  }

  private refreshSeats(beltId: string): void {
    this.seatsLoading.set(true);

    this.beltsApi
      .getSeatOverview(beltId)
      .pipe(take(1))
      .subscribe({
        next: (seats) => {
          this.seats.set(seats);
          this.pruneActiveOrders(seats);
          this.seatsError.set(null);
          this.seatsLoading.set(false);
          this.lastSeatsSuccessAt.set(Date.now());
        },
        error: (error: unknown) => {
          this.seatsLoading.set(false);
          this.seatsError.set(this.formatError(error, 'Seat updates are temporarily unavailable.'));
        },
      });
  }

  private startAnimationClock(): void {
    if (typeof requestAnimationFrame !== 'function') {
      return;
    }

    const tick = () => {
      this.now.set(Date.now());
      this.animationFrameId = requestAnimationFrame(tick);
    };

    this.animationFrameId = requestAnimationFrame(tick);
  }

  private setupReducedMotion(): void {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
      return;
    }

    this.mediaQuery = window.matchMedia('(prefers-reduced-motion: reduce)');
    this.reducedMotion.set(this.mediaQuery.matches);
    this.mediaQuery.addEventListener('change', this.handleReducedMotionChange);
  }

  private formatError(error: unknown, fallback: string): string {
    const problemDetail = getProblemDetail(error);
    return problemDetail?.detail ?? problemDetail?.title ?? fallback;
  }

  private handleOccupyError(seatId: string, error: unknown): void {
    const seatLabel = this.getSeatLabel(seatId);
    const problemDetail = getProblemDetail(error) as SeatActionProblemDetail | null;
    const status = problemDetail?.status ?? null;

    if (status === 409) {
      this.reconcileSeatDetail(
        seatId,
        (seatOrder) => this.buildConflictFeedback(seatLabel, seatOrder, problemDetail),
        () => this.buildConflictFallbackFeedback(seatId, seatLabel, problemDetail),
      );
      return;
    }

    this.occupyPendingSeatIdState.set(null);
    this.occupyFeedbackState.set(
      status === 404
        ? {
            tone: 'error',
            title: `${seatLabel} could not be found`,
            detail: problemDetail?.detail ?? 'That seat is no longer available on this belt.',
            seatId,
            seatLabel,
            orderId: null,
            createdAtLabel: null,
          }
        : {
            tone: 'error',
            title: `We could not occupy ${seatLabel}`,
            detail: this.formatError(error, 'Please try again once the belt service catches up.'),
            seatId,
            seatLabel,
            orderId: null,
            createdAtLabel: null,
          },
    );
    this.refreshAfterWrite();
  }

  private handleCheckoutError(seatId: string, error: unknown): void {
    const seatLabel = this.getSeatLabel(seatId);
    const problemDetail = getProblemDetail(error) as SeatActionProblemDetail | null;
    const status = problemDetail?.status ?? null;

    this.checkoutPendingSeatIdState.set(null);
    this.checkoutFeedbackState.set(
      status === 409
        ? {
            tone: 'error',
            title: `${seatLabel} is already free`,
            detail:
              problemDetail?.detail ??
              'No active occupancy remains for this seat. Another checkout finished first or the seat was already free.',
            seatId,
            seatLabel,
            finalSummary: this.checkedOutOrdersBySeatId()[seatId] ?? null,
            statusLabel: null,
            createdAtLabel: null,
            closedAtLabel: null,
            totalPriceLabel: null,
          }
        : status === 404
          ? {
              tone: 'error',
              title: `${seatLabel} could not be found`,
              detail: problemDetail?.detail ?? 'That seat is no longer available on this belt.',
              seatId,
              seatLabel,
              finalSummary: null,
              statusLabel: null,
              createdAtLabel: null,
              closedAtLabel: null,
              totalPriceLabel: null,
            }
          : {
              tone: 'error',
              title: `We could not check out ${seatLabel}`,
              detail: this.formatError(error, 'Please try again once the belt service catches up.'),
              seatId,
              seatLabel,
              finalSummary: null,
              statusLabel: null,
              createdAtLabel: null,
              closedAtLabel: null,
              totalPriceLabel: null,
            },
    );
    this.refreshAfterWrite();
  }

  private reconcileSeatDetail(
    seatId: string,
    buildFeedback: (seatOrder: SeatOrderDto) => OccupyFeedback,
    buildFallbackFeedback: () => OccupyFeedback,
  ): void {
    this.seatsApi
      .getSeatState(seatId)
      .pipe(take(1))
      .subscribe({
        next: (seatOrder) => {
          this.storeSeatOrderSummary(seatOrder);
          this.applySeatState(seatOrder);
          this.occupyPendingSeatIdState.set(null);
          this.occupyFeedbackState.set(buildFeedback(seatOrder));
          this.refreshAfterWrite();
        },
        error: () => {
          this.occupyPendingSeatIdState.set(null);
          this.occupyFeedbackState.set(buildFallbackFeedback());
          this.refreshAfterWrite();
        },
      });
  }

  private buildSuccessFeedback(seatState: SeatStateDto | SeatOrderDto): OccupyFeedback {
    const seatId = seatState.seatId ?? this.occupyPendingSeatIdState() ?? 'unknown-seat';
    const seatLabel = seatState.label ?? this.getSeatLabel(seatId);
    const orderSummary = 'orderSummary' in seatState ? seatState.orderSummary : undefined;

    return {
      tone: 'success',
      title: `${seatLabel} is yours`,
      detail: orderSummary?.orderId
        ? 'The seat now has an open dining record ready for later checkout and plate actions.'
        : 'The seat is now occupied and will stay synchronized with the backend state.',
      seatId,
      seatLabel,
      orderId: orderSummary?.orderId ?? null,
      createdAtLabel: this.formatTimestamp(orderSummary?.createdAt),
    };
  }

  private buildMissingContextFeedback(seatState: SeatStateDto): OccupyFeedback {
    const seatId = seatState.seatId ?? this.occupyPendingSeatIdState() ?? 'unknown-seat';
    const seatLabel = seatState.label ?? this.getSeatLabel(seatId);

    return {
      tone: 'error',
      title: `${seatLabel} is occupied, but the dining record is still syncing`,
      detail:
        'The seat was taken successfully, but the order details could not be confirmed yet. Refresh and retry before starting follow-up seat actions.',
      seatId,
      seatLabel,
      orderId: null,
      createdAtLabel: null,
    };
  }

  private buildConflictFeedback(
    seatLabel: string,
    seatOrder: SeatOrderDto,
    problemDetail: SeatActionProblemDetail | null,
  ): OccupyFeedback {
    return {
      tone: 'error',
      title: `${seatLabel} was already taken`,
      detail:
        problemDetail?.detail ??
        'Another guest occupied this seat first. The stage now reflects the current backend state.',
      seatId: seatOrder.seatId ?? this.occupyPendingSeatIdState() ?? 'unknown-seat',
      seatLabel,
      orderId: seatOrder.orderSummary?.orderId ?? null,
      createdAtLabel: this.formatTimestamp(seatOrder.orderSummary?.createdAt),
    };
  }

  private buildConflictFallbackFeedback(
    seatId: string,
    seatLabel: string,
    problemDetail: SeatActionProblemDetail | null,
  ): OccupyFeedback {
    return {
      tone: 'error',
      title: `${seatLabel} was already taken`,
      detail:
        problemDetail?.detail ??
        'Another guest occupied this seat first. Refreshing the seat state keeps the layout trustworthy.',
      seatId,
      seatLabel,
      orderId: null,
      createdAtLabel: null,
    };
  }

  private applySeatState(
    seatState: Pick<SeatStateDto, 'seatId' | 'label' | 'positionIndex' | 'isOccupied'>,
  ): void {
    if (!seatState.seatId) {
      return;
    }

    this.seats.update((currentSeats) => {
      const index = currentSeats.findIndex((seat) => seat.seatId === seatState.seatId);
      if (index === -1) {
        return currentSeats;
      }

      const nextSeats = [...currentSeats];
      nextSeats[index] = {
        ...nextSeats[index],
        ...seatState,
      };
      return nextSeats;
    });
  }

  private storeSeatOrderSummary(seatOrder: SeatOrderDto): void {
    const seatId = seatOrder.seatId;
    const orderSummary = seatOrder.orderSummary;
    if (!seatId) {
      return;
    }

    this.activeOrdersBySeatId.update((current) => {
      if (!seatOrder.isOccupied || !orderSummary?.orderId) {
        if (!(seatId in current)) {
          return current;
        }

        const next = { ...current };
        delete next[seatId];
        return next;
      }

      return {
        ...current,
        [seatId]: orderSummary,
      };
    });
  }

  private storeCheckedOutSummary(seatOrder: SeatOrderDto): void {
    const seatId = seatOrder.seatId;
    const orderSummary = seatOrder.orderSummary;
    if (!seatId || seatOrder.isOccupied || !orderSummary?.orderId) {
      return;
    }

    this.checkedOutOrdersBySeatId.update((current) => ({
      ...current,
      [seatId]: seatOrder,
    }));
  }

  private pruneActiveOrders(seats: SeatStateListDto): void {
    const occupiedSeatIds = new Set(
      seats.filter((seat) => seat.isOccupied && seat.seatId).map((seat) => seat.seatId as string),
    );

    this.activeOrdersBySeatId.update((current) => {
      const nextEntries = Object.entries(current).filter(([seatId]) => occupiedSeatIds.has(seatId));
      return nextEntries.length === Object.keys(current).length
        ? current
        : Object.fromEntries(nextEntries);
    });
  }

  private getSeatLabel(seatId: string): string {
    return this.findSeat(seatId)?.label ?? 'That seat';
  }

  private findSeat(seatId: string): SeatStateDto | undefined {
    return this.seats().find((seat) => seat.seatId === seatId);
  }

  private formatTimestamp(timestamp: string | undefined): string | null {
    if (!timestamp) {
      return null;
    }

    const parsed = new Date(timestamp);
    if (Number.isNaN(parsed.getTime())) {
      return null;
    }

    return new Intl.DateTimeFormat('en', {
      month: 'short',
      day: 'numeric',
      hour: 'numeric',
      minute: '2-digit',
    }).format(parsed);
  }

  private formatTotalPrice(amount: number | undefined): string | null {
    if (amount == null) {
      return null;
    }

    return `${amount} Yen`;
  }

  private buildCheckoutSuccessFeedback(seatOrder: SeatOrderDto): CheckoutFeedback {
    const seatId = seatOrder.seatId ?? this.checkoutPendingSeatIdState() ?? 'unknown-seat';
    const seatLabel = seatOrder.label ?? this.getSeatLabel(seatId);
    const orderSummary = seatOrder.orderSummary;
    const lines = orderSummary?.lines ?? [];

    return {
      tone: 'success',
      title: `${seatLabel} is checked out`,
      detail:
        lines.length > 0
          ? 'Checkout is complete. The seat is free again, and this final summary comes directly from the backend.'
          : 'Checkout is complete. No plates were recorded for this order, and the seat is free again.',
      seatId,
      seatLabel,
      finalSummary: seatOrder,
      statusLabel: orderSummary?.status ?? null,
      createdAtLabel: this.formatTimestamp(orderSummary?.createdAt),
      closedAtLabel: this.formatTimestamp(orderSummary?.closedAt),
      totalPriceLabel: this.formatTotalPrice(orderSummary?.totalPrice),
    };
  }
}
