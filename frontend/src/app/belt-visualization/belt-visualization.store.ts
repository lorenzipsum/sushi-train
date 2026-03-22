import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { take } from 'rxjs';

import { BeltsApi } from '../api/belts.api';
import { getProblemDetail } from '../api/http/problem-detail';
import { SeatsApi } from '../api/seats.api';
import type {
  BeltDto,
  BeltSnapshotDto,
  OrderSummaryDto,
  PlatePickFeedback,
  PickPlateRequest,
  SelectedSeatDetailViewModel,
  SeatPendingAction,
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
  private readonly selectedSeatIdState = signal<string | null>(null);
  private readonly pickPlatePendingSeatIdState = signal<string | null>(null);
  private readonly pickPlatePendingPlateIdState = signal<string | null>(null);
  private readonly pickPlateFeedbackState = signal<PlatePickFeedback | null>(null);
  private readonly rejectedPlateIdState = signal<string | null>(null);
  private readonly activeOrdersBySeatId = signal<Record<string, OrderSummaryDto>>({});
  private readonly checkedOutOrdersBySeatId = signal<Record<string, SeatOrderDto>>({});

  private pollTimerId: ReturnType<typeof setInterval> | null = null;
  private animationFrameId: number | null = null;
  private rejectAnimationTimerId: ReturnType<typeof setTimeout> | null = null;
  private mediaQuery: MediaQueryList | null = null;
  private readonly handleReducedMotionChange = (event: MediaQueryListEvent): void => {
    this.reducedMotion.set(event.matches);
  };

  readonly hasInitialData = computed(() => !!this.snapshot());
  readonly selectedSeatId = computed(() => this.selectedSeatIdState());
  readonly pickPlatePendingPlateId = computed(() => this.pickPlatePendingPlateIdState());
  readonly pickPlateFeedback = computed(() => this.pickPlateFeedbackState());
  readonly pendingSeatId = computed(
    () =>
      this.pickPlatePendingSeatIdState() ??
      this.checkoutPendingSeatIdState() ??
      this.occupyPendingSeatIdState(),
  );
  readonly pendingAction = computed<SeatPendingAction>(() => {
    if (this.pickPlatePendingSeatIdState()) {
      return 'pick-plate';
    }

    if (this.checkoutPendingSeatIdState()) {
      return 'checkout';
    }

    if (this.occupyPendingSeatIdState()) {
      return 'occupy';
    }

    return null;
  });
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
        pendingSeatId: this.pendingSeatId(),
        pendingAction: this.pendingAction(),
        pendingPlateId: this.pickPlatePendingPlateIdState(),
        rejectedPlateId: this.rejectedPlateIdState(),
        activeOrdersBySeatId: this.activeOrdersBySeatId(),
        selectedSeatId: this.selectedSeatIdState(),
      },
    );
  });
  readonly occupyPendingSeatId = computed(() => this.occupyPendingSeatIdState());
  readonly occupyFeedback = computed(() => this.occupyFeedbackState());
  readonly checkoutPendingSeatId = computed(() => this.checkoutPendingSeatIdState());
  readonly checkoutFeedback = computed(() => this.checkoutFeedbackState());
  readonly selectedSeatDetail = computed<SelectedSeatDetailViewModel | null>(() => {
    const selectedSeatId = this.selectedSeatIdState();
    if (!selectedSeatId) {
      return null;
    }

    const selectedSeat = this.findSeat(selectedSeatId);
    if (!selectedSeat) {
      return null;
    }

    const orderSummary = this.activeOrdersBySeatId()[selectedSeatId] ?? null;
    const pendingAction = this.pendingSeatId() === selectedSeatId ? this.pendingAction() : null;
    const occupyFeedback = this.occupyFeedbackState();
    const checkoutFeedback = this.checkoutFeedbackState();
    const pickPlateFeedback = this.pickPlateFeedbackState();

    const matchingFeedback =
      (pickPlateFeedback?.seatId === selectedSeatId
        ? {
            tone: pickPlateFeedback.tone,
            title: pickPlateFeedback.title,
            detail: pickPlateFeedback.detail,
          }
        : null) ??
      (occupyFeedback?.seatId === selectedSeatId
        ? {
            tone: occupyFeedback.tone,
            title: occupyFeedback.title,
            detail: occupyFeedback.detail,
          }
        : null) ??
      (checkoutFeedback?.seatId === selectedSeatId
        ? {
            tone: checkoutFeedback.tone,
            title: checkoutFeedback.title,
            detail: checkoutFeedback.detail,
          }
        : null);

    const canStartDining = !selectedSeat.isOccupied;
    const canCheckout = !!selectedSeat.isOccupied;
    const canPickPlates = !!selectedSeat.isOccupied && !!orderSummary?.orderId;

    return {
      seatId: selectedSeatId,
      seatLabel: selectedSeat.label ?? 'Selected seat',
      statusLabel: selectedSeat.isOccupied ? 'Occupied' : 'Available',
      helperLabel: selectedSeat.isOccupied
        ? 'Pick plates from the highlighted reach area, or check out when the order is complete.'
        : 'Seat clicks only change selection. Start dining here when you are ready.',
      isOccupied: !!selectedSeat.isOccupied,
      canStartDining,
      canCheckout,
      canPickPlates,
      pendingAction,
      orderSummary,
      feedbackTone: matchingFeedback?.tone ?? null,
      feedbackTitle: matchingFeedback?.title ?? null,
      feedbackDetail: matchingFeedback?.detail ?? null,
    };
  });
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

      if (this.rejectAnimationTimerId) {
        clearTimeout(this.rejectAnimationTimerId);
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

  selectSeat(seatId: string): void {
    if (!this.findSeat(seatId)) {
      return;
    }

    this.selectedSeatIdState.set(seatId);
  }

  startDiningForSelectedSeat(): void {
    const seatId = this.selectedSeatIdState();
    if (!seatId) {
      return;
    }

    this.occupySeat(seatId);
  }

  checkoutSelectedSeat(): void {
    const seatId = this.selectedSeatIdState();
    if (!seatId) {
      return;
    }

    this.checkoutSeat(seatId);
  }

  occupySeat(seatId: string): void {
    const seat = this.findSeat(seatId);
    if (
      !seat ||
      seat.isOccupied ||
      this.occupyPendingSeatIdState() ||
      this.checkoutPendingSeatIdState() ||
      this.pickPlatePendingSeatIdState()
    ) {
      return;
    }

    this.selectedSeatIdState.set(seatId);
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
      this.checkoutPendingSeatIdState() ||
      this.pickPlatePendingSeatIdState()
    ) {
      return;
    }

    this.selectedSeatIdState.set(seatId);
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

  pickPlate(plateId: string): void {
    const selectedSeatId = this.selectedSeatIdState();
    if (!selectedSeatId || this.pendingAction()) {
      return;
    }

    const selectedSeat = this.findSeat(selectedSeatId);
    const stage = this.stageViewModel();
    const selectedPlate = stage?.slots.find((slot) => slot.plate?.id === plateId)?.plate ?? null;
    const seatLabel = selectedSeat?.label ?? 'Selected seat';

    if (!selectedSeat?.isOccupied || !this.activeOrdersBySeatId()[selectedSeatId]?.orderId) {
      this.pickPlateFeedbackState.set({
        tone: 'error',
        title: `${seatLabel} must start dining first`,
        detail: 'Select an occupied seat or start dining for this seat before adding plates.',
        seatId: selectedSeatId,
        seatLabel,
        plateId,
        outcomeType: 'seat-not-occupied',
        orderSummary: null,
        rejectAnimationShown: false,
      });
      return;
    }

    if (!selectedPlate?.isPickable) {
      this.triggerPlateReject(plateId);
      this.pickPlateFeedbackState.set({
        tone: 'error',
        title: `${seatLabel} cannot reach that plate`,
        detail:
          "That plate is outside this seat's reachable area or is not currently pickable for the selected seat.",
        seatId: selectedSeatId,
        seatLabel,
        plateId,
        outcomeType: 'out-of-range',
        orderSummary: null,
        rejectAnimationShown: true,
      });
      return;
    }

    const request: PickPlateRequest = { plateId };
    this.pickPlatePendingSeatIdState.set(selectedSeatId);
    this.pickPlatePendingPlateIdState.set(plateId);
    this.pickPlateFeedbackState.set(null);

    this.seatsApi
      .pickPlate(selectedSeatId, request)
      .pipe(take(1))
      .subscribe({
        next: (result) => {
          this.storeSeatOrderSummary(result);
          this.applySeatState(result);
          this.pickPlatePendingSeatIdState.set(null);
          this.pickPlatePendingPlateIdState.set(null);
          this.pickPlateFeedbackState.set({
            tone: 'success',
            title: `${seatLabel} picked ${selectedPlate.menuItemName}`,
            detail: 'The running order was updated from the backend response.',
            seatId: selectedSeatId,
            seatLabel,
            plateId,
            outcomeType: 'success',
            orderSummary: result.orderSummary ?? null,
            rejectAnimationShown: false,
          });
          this.refreshAfterWrite();
        },
        error: (error: unknown) => {
          this.handlePickPlateError(selectedSeatId, plateId, error);
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
          this.ensureSelectedSeat(seats);
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

  private handlePickPlateError(seatId: string, plateId: string, error: unknown): void {
    const seatLabel = this.getSeatLabel(seatId);
    const problemDetail = getProblemDetail(error) as SeatActionProblemDetail | null;
    const status = problemDetail?.status ?? null;
    const errorCode = problemDetail?.errorCode ?? null;

    this.pickPlatePendingSeatIdState.set(null);
    this.pickPlatePendingPlateIdState.set(null);

    if (status === 404) {
      this.pickPlateFeedbackState.set({
        tone: 'error',
        title: `${seatLabel} or that plate is no longer available`,
        detail:
          problemDetail?.detail ?? 'The selected seat or plate no longer exists on this belt.',
        seatId,
        seatLabel,
        plateId,
        outcomeType: 'not-found',
        orderSummary: null,
        rejectAnimationShown: false,
      });
      this.refreshAfterWrite();
      return;
    }

    if (status === 409) {
      const shouldRejectPlate = errorCode !== 'SEAT_NOT_OCCUPIED';
      if (shouldRejectPlate) {
        this.triggerPlateReject(plateId);
      }

      this.pickPlateFeedbackState.set({
        tone: 'error',
        title:
          errorCode === 'SEAT_NOT_OCCUPIED'
            ? `${seatLabel} must start dining first`
            : `${seatLabel} could not take that plate`,
        detail:
          problemDetail?.detail ??
          (errorCode === 'SEAT_NOT_OCCUPIED'
            ? 'The selected seat no longer has an open order for adding plates.'
            : 'The plate or seat state changed before the pick could be completed.'),
        seatId,
        seatLabel,
        plateId,
        outcomeType:
          errorCode === 'SEAT_NOT_OCCUPIED'
            ? 'seat-not-occupied'
            : errorCode === 'PLATE_NOT_PICKABLE'
              ? 'plate-not-pickable'
              : errorCode === 'PLATE_OUT_OF_RANGE'
                ? 'out-of-range'
                : 'resource-conflict',
        orderSummary: null,
        rejectAnimationShown: shouldRejectPlate,
      });
      this.refreshAfterWrite();
      return;
    }

    this.pickPlateFeedbackState.set({
      tone: 'error',
      title: `We could not add that plate for ${seatLabel}`,
      detail: this.formatError(error, 'Please try again once the belt service catches up.'),
      seatId,
      seatLabel,
      plateId,
      outcomeType: 'unknown-error',
      orderSummary: null,
      rejectAnimationShown: false,
    });
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

  private ensureSelectedSeat(seats: SeatStateListDto): void {
    const currentSelectedSeatId = this.selectedSeatIdState();

    if (currentSelectedSeatId && seats.some((seat) => seat.seatId === currentSelectedSeatId)) {
      return;
    }

    this.selectedSeatIdState.set(seats[0]?.seatId ?? null);
  }

  private triggerPlateReject(plateId: string): void {
    this.rejectedPlateIdState.set(plateId);

    if (this.rejectAnimationTimerId) {
      clearTimeout(this.rejectAnimationTimerId);
    }

    this.rejectAnimationTimerId = setTimeout(() => {
      this.rejectedPlateIdState.set(null);
      this.rejectAnimationTimerId = null;
    }, 450);
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
