import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { take } from 'rxjs';

import { BeltsApi } from '../api/belts.api';
import { getProblemDetail } from '../api/http/problem-detail';
import { MenuItemsApi } from '../api/menu-items.api';
import { SeatsApi } from '../api/seats.api';
import type {
  BeltDto,
  BeltSnapshotDto,
  MenuItemDto,
  OperatorPlacementDraftPatch,
  OperatorPlacementDraftValue,
  OperatorPlacementNotice,
  OperatorPlacementPresentationMode,
  OperatorPlacementViewModel,
  OrderSummaryDto,
  PlatePickFeedback,
  PickPlateRequest,
  SelectedSeatDetailViewModel,
  SeatPendingAction,
  SeatActionProblemDetail,
  SeatOrderDto,
  SeatRestorationState,
  SeatStateDto,
  SeatStateListDto,
} from '../api/types';
import { buildBeltStageViewModel } from './belt-view-model';
import { getRenderOffset } from './motion';

const POLL_INTERVAL_MS = 3000;
const RESTORATION_RETRY_DELAY_MS = 1500;
const OPERATOR_PANEL_BREAKPOINT = '(max-width: 900px)';
const OPERATOR_DEFAULT_EXPIRES_IN_MINUTES = 120;
const SELECTED_SEAT_STORAGE_KEY = 'sushi-train:selected-seat-id';

function toDateTimeLocalValue(value: Date): string {
  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, '0');
  const day = String(value.getDate()).padStart(2, '0');
  const hour = String(value.getHours()).padStart(2, '0');
  const minute = String(value.getMinutes()).padStart(2, '0');

  return `${year}-${month}-${day}T${hour}:${minute}`;
}

function buildOperatorDefaultDraft(menuItem: MenuItemDto | null): OperatorPlacementDraftValue {
  return {
    menuItemId: menuItem?.id ?? null,
    numOfPlates: 1,
    tierSnapshot: menuItem?.defaultTier ?? null,
    priceAtCreation: menuItem ? String(menuItem.basePrice) : '',
    expiresAt: toDateTimeLocalValue(
      new Date(Date.now() + OPERATOR_DEFAULT_EXPIRES_IN_MINUTES * 60 * 1000),
    ),
    isDefaultDraft: !!menuItem,
  };
}

function buildOperatorExpiresAtValue(): string {
  return toDateTimeLocalValue(
    new Date(Date.now() + OPERATOR_DEFAULT_EXPIRES_IN_MINUTES * 60 * 1000),
  );
}

function matchesOperatorSearch(menuItem: MenuItemDto, query: string): boolean {
  if (!query) {
    return true;
  }

  const searchable = `${menuItem.name} ${menuItem.defaultTier} ${menuItem.basePrice}`.toLowerCase();
  return searchable.includes(query);
}

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
  private readonly menuItemsApi = inject(MenuItemsApi);
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
  private readonly seatRestorationBySeatId = signal<Record<string, SeatRestorationState>>({});
  private readonly compactOperatorLayout = signal(false);
  private readonly operatorOpenState = signal(false);
  private readonly operatorMenuItemsState = signal<MenuItemDto[]>([]);
  private readonly operatorMenuLoadingState = signal(false);
  private readonly operatorMenuLoadErrorState = signal<string | null>(null);
  private readonly operatorSearchQueryState = signal('');
  private readonly operatorSelectedMenuItemIdState = signal<string | null>(null);
  private readonly operatorDraftState = signal<OperatorPlacementDraftValue>(
    buildOperatorDefaultDraft(null),
  );
  private readonly operatorNoticeState = signal<OperatorPlacementNotice | null>(null);
  private readonly operatorPendingSubmissionState = signal(false);

  private pollTimerId: ReturnType<typeof setInterval> | null = null;
  private animationFrameId: number | null = null;
  private rejectAnimationTimerId: ReturnType<typeof setTimeout> | null = null;
  private readonly restorationRetryTimerIds = new Map<string, ReturnType<typeof setTimeout>>();
  private readonly restorationInFlightSeatIds = new Set<string>();
  private mediaQuery: MediaQueryList | null = null;
  private operatorLayoutMediaQuery: MediaQueryList | null = null;
  private readonly handleReducedMotionChange = (event: MediaQueryListEvent): void => {
    this.reducedMotion.set(event.matches);
  };
  private readonly handleOperatorLayoutChange = (event: MediaQueryListEvent): void => {
    this.compactOperatorLayout.set(event.matches);
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
        restorationBySeatId: this.seatRestorationBySeatId(),
        selectedSeatId: this.selectedSeatIdState(),
      },
    );
  });
  readonly occupyPendingSeatId = computed(() => this.occupyPendingSeatIdState());
  readonly occupyFeedback = computed(() => this.occupyFeedbackState());
  readonly checkoutPendingSeatId = computed(() => this.checkoutPendingSeatIdState());
  readonly checkoutFeedback = computed(() => this.checkoutFeedbackState());
  readonly operatorPlacement = computed<OperatorPlacementViewModel | null>(() => {
    if (!this.primaryBelt()?.id) {
      return null;
    }

    const query = this.operatorSearchQueryState().trim().toLowerCase();
    const menuItems = this.operatorMenuItemsState();
    const filteredMenuItems = menuItems.filter((menuItem) =>
      matchesOperatorSearch(menuItem, query),
    );
    const selectedMenuItemId = this.operatorSelectedMenuItemIdState();
    const selectedMenuItem =
      menuItems.find((menuItem) => menuItem.id === selectedMenuItemId) ?? null;
    const draft = this.operatorDraftState();
    const submitDisabledReason = this.getOperatorSubmitDisabledReason(
      selectedMenuItem,
      draft,
      this.operatorMenuLoadingState(),
      this.operatorMenuLoadErrorState(),
      this.operatorPendingSubmissionState(),
    );

    return {
      isOpen: this.operatorOpenState(),
      presentationMode: this.compactOperatorLayout() ? 'secondary-surface' : 'inline-kitchen',
      isMenuLoading: this.operatorMenuLoadingState(),
      menuLoadError: this.operatorMenuLoadErrorState(),
      isSubmitting: this.operatorPendingSubmissionState(),
      notice: this.operatorNoticeState(),
      query: this.operatorSearchQueryState(),
      totalMenuCount: menuItems.length,
      filteredMenuItems,
      selectedMenuItemId,
      selectedMenuItemLabel: selectedMenuItem?.name ?? null,
      selectedMenuItemTier: selectedMenuItem?.defaultTier ?? null,
      draft,
      canSubmit: !submitDisabledReason,
      submitDisabledReason,
    };
  });
  readonly selectedSeatDetail = computed<SelectedSeatDetailViewModel | null>(() => {
    const selectedSeatId = this.selectedSeatIdState();
    if (!selectedSeatId) {
      return null;
    }

    const selectedSeat = this.findSeat(selectedSeatId);
    if (!selectedSeat) {
      return null;
    }

    const activeOrderSummary = this.activeOrdersBySeatId()[selectedSeatId] ?? null;
    const checkedOutSummary = this.checkedOutOrdersBySeatId()[selectedSeatId] ?? null;
    const orderSummary = activeOrderSummary ?? checkedOutSummary?.orderSummary ?? null;
    const restoration = this.seatRestorationBySeatId()[selectedSeatId] ?? null;
    const pendingAction = this.pendingSeatId() === selectedSeatId ? this.pendingAction() : null;
    const occupyFeedback = this.occupyFeedbackState();
    const checkoutFeedback = this.checkoutFeedbackState();
    const pickPlateFeedback = this.pickPlateFeedbackState();
    const isCheckoutSummary = !selectedSeat.isOccupied && !!checkedOutSummary?.orderSummary;
    const restorationStatus = isCheckoutSummary
      ? 'checked-out'
      : !selectedSeat.isOccupied
        ? 'available'
        : restoration?.restorationStatus === 'unresolved-retrying'
          ? 'unresolved'
          : restoration?.restorationStatus === 'confirmed-open-order' ||
              !!activeOrderSummary?.orderId
            ? 'occupied'
            : 'syncing';

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
    const canPickPlates =
      !!selectedSeat.isOccupied &&
      !!activeOrderSummary?.orderId &&
      restorationStatus === 'occupied';
    const blockedReason =
      selectedSeat.isOccupied && !canPickPlates
        ? restorationStatus === 'syncing' || restorationStatus === 'unresolved'
          ? 'syncing'
          : 'no-open-order'
        : null;

    const statusLabel = isCheckoutSummary
      ? 'Checked out'
      : restorationStatus === 'unresolved'
        ? 'Retrying dining sync'
        : restorationStatus === 'syncing'
          ? 'Syncing dining state'
          : selectedSeat.isOccupied
            ? 'Occupied'
            : 'Available';

    const helperLabel = isCheckoutSummary
      ? 'This final backend summary remains visible for the seat that just checked out.'
      : restorationStatus === 'unresolved'
        ? (restoration?.resolutionMessage ??
          'We could not confirm this dining state yet. Automatic retry is still running in the background.')
        : restorationStatus === 'syncing'
          ? (restoration?.resolutionMessage ??
            'Dining state is loading from the backend. Reach cues may stay visible, but picks remain blocked until sync completes.')
          : selectedSeat.isOccupied
            ? orderSummary?.lines?.length
              ? 'Pick plates from the highlighted reach area, or check out when the order is complete.'
              : 'Dining is active. Pick the next reachable plate to start building the running order here.'
            : 'Seat clicks only change selection. Start dining here when you are ready.';

    return {
      seatId: selectedSeatId,
      seatLabel: selectedSeat.label ?? 'Selected seat',
      restorationStatus,
      statusLabel,
      helperLabel,
      isOccupied: !!selectedSeat.isOccupied,
      canStartDining,
      canCheckout,
      canPickPlates,
      blockedReason,
      isCheckoutSummary,
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
    this.setupOperatorLayout();
    this.startAnimationClock();
    this.loadBelts();
    this.loadOperatorMenuItems();

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

      this.restorationRetryTimerIds.forEach((timerId) => clearTimeout(timerId));
      this.restorationRetryTimerIds.clear();

      this.mediaQuery?.removeEventListener('change', this.handleReducedMotionChange);
      this.operatorLayoutMediaQuery?.removeEventListener('change', this.handleOperatorLayoutChange);
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

    this.setSelectedSeatId(seatId);
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

  toggleOperatorPlacement(): void {
    if (this.operatorOpenState()) {
      this.operatorOpenState.set(false);
      return;
    }

    this.operatorOpenState.set(true);
    this.operatorNoticeState.set(null);
    this.loadOperatorMenuItems();
  }

  closeOperatorPlacement(): void {
    this.operatorOpenState.set(false);
  }

  retryOperatorMenuLoad(): void {
    this.loadOperatorMenuItems(true);
  }

  setOperatorSearchQuery(query: string): void {
    this.operatorSearchQueryState.set(query);
  }

  selectOperatorMenuItem(menuItemId: string): void {
    const menuItem = this.operatorMenuItemsState().find((item) => item.id === menuItemId) ?? null;
    if (!menuItem) {
      return;
    }

    this.operatorSelectedMenuItemIdState.set(menuItem.id);
    this.operatorDraftState.set(buildOperatorDefaultDraft(menuItem));
    this.operatorNoticeState.set(null);
  }

  updateOperatorDraft(patch: OperatorPlacementDraftPatch): void {
    this.operatorDraftState.update((currentDraft) => ({
      ...currentDraft,
      ...patch,
      menuItemId: currentDraft.menuItemId,
      isDefaultDraft: false,
    }));
  }

  submitOperatorPlacement(): void {
    const beltId = this.primaryBelt()?.id ?? null;
    const selectedMenuItem =
      this.operatorMenuItemsState().find(
        (menuItem) => menuItem.id === this.operatorSelectedMenuItemIdState(),
      ) ?? null;
    const draft = this.operatorDraftState();
    const submitDisabledReason = this.getOperatorSubmitDisabledReason(
      selectedMenuItem,
      draft,
      this.operatorMenuLoadingState(),
      this.operatorMenuLoadErrorState(),
      this.operatorPendingSubmissionState(),
    );

    if (!beltId || !selectedMenuItem || submitDisabledReason) {
      if (submitDisabledReason) {
        this.operatorNoticeState.set({
          tone: 'error',
          title: 'Plate placement is not ready',
          detail: submitDisabledReason,
          outcomeType: 'invalid-values',
          createdCount: null,
          menuItemName: selectedMenuItem?.name ?? null,
        });
      }
      return;
    }

    this.operatorPendingSubmissionState.set(true);
    this.operatorNoticeState.set(null);

    const expiresAt = draft.isDefaultDraft ? buildOperatorExpiresAtValue() : draft.expiresAt;
    if (draft.isDefaultDraft) {
      this.operatorDraftState.update((currentDraft) => ({
        ...currentDraft,
        expiresAt,
      }));
    }

    this.beltsApi
      .createPlatesAndPlaceOnBelt(beltId, {
        menuItemId: selectedMenuItem.id,
        numOfPlates: draft.numOfPlates,
        tierSnapshot: draft.tierSnapshot ?? selectedMenuItem.defaultTier,
        priceAtCreation: Number(draft.priceAtCreation),
        expiresAt: new Date(expiresAt).toISOString(),
      })
      .pipe(take(1))
      .subscribe({
        next: (result) => {
          const createdCount =
            result.createdCount ?? result.placedPlates?.length ?? draft.numOfPlates;
          this.operatorPendingSubmissionState.set(false);
          this.operatorNoticeState.set({
            tone: 'success',
            title: `${createdCount} ${selectedMenuItem.name} plate${createdCount === 1 ? '' : 's'} queued`,
            detail:
              createdCount === 1
                ? 'The kitchen request was accepted, and the belt will refresh immediately.'
                : 'The kitchen request was accepted, and the belt will refresh immediately.',
            outcomeType: 'success',
            createdCount,
            menuItemName: selectedMenuItem.name,
          });
          this.refreshAfterWrite();
        },
        error: (error: unknown) => {
          this.operatorPendingSubmissionState.set(false);
          this.operatorNoticeState.set(
            this.buildOperatorPlacementFailureNotice(error, selectedMenuItem.name),
          );
        },
      });
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

    this.setSelectedSeatId(seatId);
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

    this.setSelectedSeatId(seatId);
    this.checkoutPendingSeatIdState.set(seatId);
    this.checkoutFeedbackState.set(null);

    this.seatsApi
      .checkout(seatId)
      .pipe(take(1))
      .subscribe({
        next: (result) => {
          this.storeSeatOrderSummary(result);
          this.applyRestorationFromSeatOrder(result);
          this.storeCheckedOutSummary(result);
          this.applySeatState(result);
          this.clearSeatRestoration(seatId);
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
    const restoration = this.seatRestorationBySeatId()[selectedSeatId] ?? null;
    const stage = this.stageViewModel();
    const selectedPlate = stage?.slots.find((slot) => slot.plate?.id === plateId)?.plate ?? null;
    const seatLabel = selectedSeat?.label ?? 'Selected seat';
    const hasActiveOrder = !!this.activeOrdersBySeatId()[selectedSeatId]?.orderId;
    const isSyncingSeat =
      !!selectedSeat?.isOccupied &&
      (!hasActiveOrder ||
        restoration?.restorationStatus === 'syncing' ||
        restoration?.restorationStatus === 'unresolved-retrying');

    if (isSyncingSeat) {
      this.pickPlateFeedbackState.set({
        tone: 'error',
        title: `${seatLabel} is still syncing`,
        detail:
          restoration?.resolutionMessage ??
          'Dining state is still loading from the backend. Reach cues may stay visible, but picks remain blocked until sync finishes.',
        seatId: selectedSeatId,
        seatLabel,
        plateId,
        outcomeType: 'syncing',
        orderSummary: restoration?.lastKnownOrderSummary ?? null,
        rejectAnimationShown: false,
      });
      return;
    }

    if (!selectedSeat?.isOccupied || !hasActiveOrder) {
      this.pickPlateFeedbackState.set({
        tone: 'error',
        title: `${seatLabel} has no open order`,
        detail:
          'Start dining for this seat before adding plates, or wait for the next seat refresh if the backend state just changed.',
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
          this.applyRestorationFromSeatOrder(result);
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

  private loadOperatorMenuItems(force = false, page = 0, collected: MenuItemDto[] = []): void {
    if (page === 0) {
      if (!force && (this.operatorMenuLoadingState() || this.operatorMenuItemsState().length > 0)) {
        return;
      }

      this.operatorMenuLoadingState.set(true);
      this.operatorMenuLoadErrorState.set(null);
      if (force) {
        this.operatorMenuItemsState.set([]);
      }
    }

    this.menuItemsApi
      .getMenuItems(page)
      .pipe(take(1))
      .subscribe({
        next: (response) => {
          const nextCollected = [...collected, ...(response.content ?? [])];
          const currentPage = Number(response.page?.number ?? page);
          const totalPages = Math.max(1, Number(response.page?.totalPages ?? 1));

          if (currentPage + 1 < totalPages) {
            this.loadOperatorMenuItems(force, currentPage + 1, nextCollected);
            return;
          }

          this.operatorMenuItemsState.set(nextCollected);
          this.operatorMenuLoadingState.set(false);
          this.reconcileOperatorMenuSelection(nextCollected);
        },
        error: (error: unknown) => {
          this.operatorMenuLoadingState.set(false);
          this.operatorMenuLoadErrorState.set(
            this.formatError(error, 'The menu catalog could not be loaded.'),
          );
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
          this.pruneCheckedOutSummaries(seats);
          this.syncRestorationStates(seats);
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

  private setupOperatorLayout(): void {
    if (typeof window === 'undefined' || typeof window.matchMedia !== 'function') {
      return;
    }

    this.operatorLayoutMediaQuery = window.matchMedia(OPERATOR_PANEL_BREAKPOINT);
    this.compactOperatorLayout.set(this.operatorLayoutMediaQuery.matches);
    this.operatorLayoutMediaQuery.addEventListener('change', this.handleOperatorLayoutChange);
  }

  private formatError(error: unknown, fallback: string): string {
    const problemDetail = getProblemDetail(error);
    return problemDetail?.detail ?? problemDetail?.title ?? fallback;
  }

  private getOperatorSubmitDisabledReason(
    selectedMenuItem: MenuItemDto | null,
    draft: OperatorPlacementDraftValue,
    isLoadingMenu: boolean,
    menuLoadError: string | null,
    isSubmitting: boolean,
  ): string | null {
    if (isSubmitting) {
      return 'An add-plates request is already in flight.';
    }

    if (isLoadingMenu) {
      return 'Loading the menu catalog for the operator flow.';
    }

    if (menuLoadError) {
      return 'Retry the menu load before placing plates.';
    }

    if (!selectedMenuItem) {
      return 'Choose a menu item before placing plates.';
    }

    if (!Number.isInteger(draft.numOfPlates) || draft.numOfPlates < 1 || draft.numOfPlates > 10) {
      return 'Use a whole number of plates between 1 and 10.';
    }

    const priceAtCreation = Number(draft.priceAtCreation);
    if (!Number.isInteger(priceAtCreation) || priceAtCreation < 0) {
      return 'Enter a valid non-negative price snapshot.';
    }

    if (!draft.expiresAt) {
      return 'Choose when the new plates should expire.';
    }

    const expiresAt = new Date(draft.expiresAt);
    if (Number.isNaN(expiresAt.getTime()) || expiresAt.getTime() <= Date.now()) {
      return 'Choose an expiry time in the future.';
    }

    return null;
  }

  private reconcileOperatorMenuSelection(menuItems: MenuItemDto[]): void {
    const selectedMenuItemId = this.operatorSelectedMenuItemIdState();
    if (!selectedMenuItemId) {
      return;
    }

    const selectedMenuItem =
      menuItems.find((menuItem) => menuItem.id === selectedMenuItemId) ?? null;
    if (selectedMenuItem) {
      return;
    }

    this.operatorSelectedMenuItemIdState.set(null);
    this.operatorDraftState.set(buildOperatorDefaultDraft(null));
  }

  private buildOperatorPlacementFailureNotice(
    error: unknown,
    menuItemName: string,
  ): OperatorPlacementNotice {
    const problemDetail = getProblemDetail(error) as
      | ({ errorCode?: string } & { status?: number; title?: string; detail?: string })
      | null;
    const status = problemDetail?.status ?? null;
    const detail = problemDetail?.detail ?? problemDetail?.title ?? 'The kitchen request failed.';
    const message = `${problemDetail?.title ?? ''} ${problemDetail?.detail ?? ''}`.toLowerCase();

    if (status === 409) {
      return {
        tone: 'error',
        title: 'Not enough open belt space',
        detail:
          detail ||
          'The backend could not place every requested plate. Adjust the quantity and try again after the belt advances.',
        outcomeType: 'not-enough-space',
        createdCount: null,
        menuItemName,
      };
    }

    if (status === 404 && message.includes('menu')) {
      return {
        tone: 'error',
        title: 'That menu item is no longer available',
        detail,
        outcomeType: 'invalid-menu-item',
        createdCount: null,
        menuItemName,
      };
    }

    if (status === 404) {
      return {
        tone: 'error',
        title: 'This belt is no longer available',
        detail,
        outcomeType: 'missing-belt',
        createdCount: null,
        menuItemName,
      };
    }

    if (status === 400 && message.includes('menu')) {
      return {
        tone: 'error',
        title: 'That menu item could not be placed',
        detail,
        outcomeType: 'invalid-menu-item',
        createdCount: null,
        menuItemName,
      };
    }

    if (status === 400) {
      return {
        tone: 'error',
        title: 'One or more placement values were rejected',
        detail,
        outcomeType: 'invalid-values',
        createdCount: null,
        menuItemName,
      };
    }

    return {
      tone: 'error',
      title: 'The add-plates request did not finish',
      detail,
      outcomeType: status === 422 ? 'malformed-request' : 'unknown-error',
      createdCount: null,
      menuItemName,
    };
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
          this.applyRestorationFromSeatOrder(seatOrder);
          this.applySeatState(seatOrder);
          this.occupyPendingSeatIdState.set(null);
          this.occupyFeedbackState.set(buildFeedback(seatOrder));
          this.refreshAfterWrite();
        },
        error: () => {
          this.occupyPendingSeatIdState.set(null);
          this.markSeatRestorationUnresolved(
            seatId,
            'The dining record is still syncing after the seat was occupied. Automatic retry is running.',
          );
          this.scheduleRestorationRetry(seatId);
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

    if (seatOrder.isOccupied && orderSummary?.orderId) {
      this.checkedOutOrdersBySeatId.update((current) => {
        if (!(seatId in current)) {
          return current;
        }

        const next = { ...current };
        delete next[seatId];
        return next;
      });
    }
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

  private pruneCheckedOutSummaries(seats: SeatStateListDto): void {
    const visibleSeatIds = new Set(seats.map((seat) => seat.seatId).filter(Boolean) as string[]);

    this.checkedOutOrdersBySeatId.update((current) => {
      const nextEntries = Object.entries(current).filter(([seatId]) => visibleSeatIds.has(seatId));
      return nextEntries.length === Object.keys(current).length
        ? current
        : Object.fromEntries(nextEntries);
    });
  }

  private ensureSelectedSeat(seats: SeatStateListDto): void {
    const currentSelectedSeatId = this.selectedSeatIdState();

    if (currentSelectedSeatId && seats.some((seat) => seat.seatId === currentSelectedSeatId)) {
      this.persistSelectedSeatId(currentSelectedSeatId);
      return;
    }

    const storedSeatId = this.getStoredSelectedSeatId();
    if (storedSeatId && seats.some((seat) => seat.seatId === storedSeatId)) {
      this.setSelectedSeatId(storedSeatId);
      return;
    }

    this.setSelectedSeatId(seats[0]?.seatId ?? null);
  }

  private syncRestorationStates(seats: SeatStateListDto): void {
    const occupiedSeatIds = new Set(
      seats.filter((seat) => seat.isOccupied && seat.seatId).map((seat) => seat.seatId as string),
    );

    this.seatRestorationBySeatId.update((current) => {
      const nextEntries = Object.entries(current).filter(([seatId]) => occupiedSeatIds.has(seatId));
      return nextEntries.length === Object.keys(current).length
        ? current
        : Object.fromEntries(nextEntries);
    });

    Array.from(this.restorationRetryTimerIds.keys()).forEach((seatId) => {
      if (!occupiedSeatIds.has(seatId)) {
        this.clearRestorationRetry(seatId);
      }
    });

    seats.forEach((seat) => {
      const seatId = seat.seatId;
      if (!seatId || !seat.isOccupied) {
        return;
      }

      const current = this.seatRestorationBySeatId()[seatId] ?? null;
      const activeOrder = this.activeOrdersBySeatId()[seatId] ?? null;

      if (activeOrder?.orderId) {
        this.seatRestorationBySeatId.update((currentState) => ({
          ...currentState,
          [seatId]: {
            seatId,
            restorationStatus: 'confirmed-open-order',
            hasRetryInFlight: false,
            lastKnownOrderSummary: activeOrder,
            resolutionMessage: 'Dining state restored from the backend.',
          },
        }));
        return;
      }

      if (this.restorationInFlightSeatIds.has(seatId) || current?.hasRetryInFlight) {
        return;
      }

      this.hydrateSeatContext(seatId, current?.restorationStatus === 'unresolved-retrying');
    });
  }

  private hydrateSeatContext(seatId: string, retryTriggered = false): void {
    if (this.restorationInFlightSeatIds.has(seatId)) {
      return;
    }

    const seat = this.findSeat(seatId);
    if (!seat?.isOccupied) {
      this.clearSeatRestoration(seatId);
      return;
    }

    this.clearRestorationRetry(seatId);
    this.restorationInFlightSeatIds.add(seatId);
    this.seatRestorationBySeatId.update((current) => ({
      ...current,
      [seatId]: {
        seatId,
        restorationStatus: retryTriggered ? 'unresolved-retrying' : 'syncing',
        hasRetryInFlight: false,
        lastKnownOrderSummary:
          current[seatId]?.lastKnownOrderSummary ?? this.activeOrdersBySeatId()[seatId] ?? null,
        resolutionMessage: retryTriggered
          ? 'Retrying dining state automatically.'
          : 'Syncing dining state from the backend.',
      },
    }));

    const seatStateRequest = this.seatsApi.getSeatState(seatId);
    if (!seatStateRequest) {
      this.restorationInFlightSeatIds.delete(seatId);
      return;
    }

    seatStateRequest.pipe(take(1)).subscribe({
      next: (seatOrder) => {
        this.restorationInFlightSeatIds.delete(seatId);
        this.storeSeatOrderSummary(seatOrder);
        this.applyRestorationFromSeatOrder(seatOrder);
        this.applySeatState(seatOrder);
      },
      error: (error: unknown) => {
        this.restorationInFlightSeatIds.delete(seatId);
        this.markSeatRestorationUnresolved(
          seatId,
          this.formatError(
            error,
            'Dining state could not be confirmed yet. Automatic retry is still running.',
          ),
        );
        this.scheduleRestorationRetry(seatId);
      },
    });
  }

  private applyRestorationFromSeatOrder(seatOrder: SeatOrderDto): void {
    const seatId = seatOrder.seatId;
    if (!seatId) {
      return;
    }

    const orderSummary = seatOrder.orderSummary ?? null;
    this.seatRestorationBySeatId.update((current) => ({
      ...current,
      [seatId]: {
        seatId,
        restorationStatus:
          seatOrder.isOccupied && orderSummary?.orderId
            ? 'confirmed-open-order'
            : 'confirmed-no-order',
        hasRetryInFlight: false,
        lastKnownOrderSummary: orderSummary,
        resolutionMessage:
          seatOrder.isOccupied && orderSummary?.orderId
            ? 'Dining state restored from the backend.'
            : 'No active dining record remains for this seat.',
      },
    }));
  }

  private markSeatRestorationUnresolved(seatId: string, resolutionMessage: string): void {
    this.seatRestorationBySeatId.update((current) => ({
      ...current,
      [seatId]: {
        seatId,
        restorationStatus: 'unresolved-retrying',
        hasRetryInFlight: true,
        lastKnownOrderSummary:
          current[seatId]?.lastKnownOrderSummary ?? this.activeOrdersBySeatId()[seatId] ?? null,
        resolutionMessage,
      },
    }));
  }

  private scheduleRestorationRetry(seatId: string): void {
    this.clearRestorationRetry(seatId);
    this.restorationRetryTimerIds.set(
      seatId,
      setTimeout(() => {
        this.restorationRetryTimerIds.delete(seatId);
        this.hydrateSeatContext(seatId, true);
      }, RESTORATION_RETRY_DELAY_MS),
    );
  }

  private clearRestorationRetry(seatId: string): void {
    const timerId = this.restorationRetryTimerIds.get(seatId);
    if (!timerId) {
      return;
    }

    clearTimeout(timerId);
    this.restorationRetryTimerIds.delete(seatId);
  }

  private clearSeatRestoration(seatId: string): void {
    this.clearRestorationRetry(seatId);
    this.restorationInFlightSeatIds.delete(seatId);
    this.seatRestorationBySeatId.update((current) => {
      if (!(seatId in current)) {
        return current;
      }

      const next = { ...current };
      delete next[seatId];
      return next;
    });
  }

  private setSelectedSeatId(seatId: string | null): void {
    this.selectedSeatIdState.set(seatId);
    this.persistSelectedSeatId(seatId);
  }

  private persistSelectedSeatId(seatId: string | null): void {
    if (typeof sessionStorage === 'undefined') {
      return;
    }

    try {
      if (seatId) {
        sessionStorage.setItem(SELECTED_SEAT_STORAGE_KEY, seatId);
      } else {
        sessionStorage.removeItem(SELECTED_SEAT_STORAGE_KEY);
      }
    } catch {
      // Ignore storage failures and fall back to in-memory selection only.
    }
  }

  private getStoredSelectedSeatId(): string | null {
    if (typeof sessionStorage === 'undefined') {
      return null;
    }

    try {
      return sessionStorage.getItem(SELECTED_SEAT_STORAGE_KEY);
    } catch {
      return null;
    }
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
