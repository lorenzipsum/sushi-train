import { ChangeDetectionStrategy, Component, computed, effect, input, signal } from '@angular/core';

import { unwrapRotationDegrees } from './motion';
import type { BeltStageViewModel } from './belt-view-model';

@Component({
  selector: 'app-belt-stage',
  templateUrl: './belt-stage.html',
  styleUrl: './belt-stage.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BeltStageComponent {
  readonly stage = input.required<BeltStageViewModel>();
  readonly rotationDegrees = input.required<number>();
  readonly rotationDirection = input(0);
  readonly reducedMotion = input(false);
  readonly paused = input(false);

  private readonly continuousRotationDegrees = signal(0);
  private previousRawRotationDegrees: number | null = null;
  private previousContinuousRotationDegrees: number | null = null;

  protected readonly beltRotation = computed(() => `${this.continuousRotationDegrees()}deg`);

  constructor() {
    effect(() => {
      const rawRotationDegrees = this.rotationDegrees();
      const direction = this.rotationDirection();
      const reducedMotion = this.reducedMotion();

      const nextRotationDegrees = reducedMotion
        ? rawRotationDegrees
        : unwrapRotationDegrees(
            this.previousRawRotationDegrees,
            this.previousContinuousRotationDegrees,
            rawRotationDegrees,
            direction,
          );

      this.previousRawRotationDegrees = rawRotationDegrees;
      this.previousContinuousRotationDegrees = nextRotationDegrees;
      this.continuousRotationDegrees.set(nextRotationDegrees);
    });
  }
}
