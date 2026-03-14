import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { take } from 'rxjs';

import { BeltsApi } from '../api/belts.api';
import { getProblemDetail } from '../api/http/problem-detail';
import type { BeltDto, BeltSnapshotDto, SeatStateListDto } from '../api/types';
import { buildBeltStageViewModel } from './belt-view-model';
import { getRotationDegrees } from './motion';

const POLL_INTERVAL_MS = 3000;

@Injectable({ providedIn: 'root' })
export class BeltVisualizationStore {
  private readonly beltsApi = inject(BeltsApi);
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

    return buildBeltStageViewModel(snapshot, this.seats());
  });
  readonly rotationDegrees = computed(() =>
    getRotationDegrees(this.snapshot(), this.now(), this.reducedMotion()),
  );
  readonly rotationDirection = computed(() =>
    Math.sign(this.snapshot()?.beltSpeedSlotsPerTick ?? 0),
  );
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

    return this.isPaused() ? 'Paused at the belt' : 'Gliding around the belt';
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
}
