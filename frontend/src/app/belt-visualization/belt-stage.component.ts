import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import type { BeltStageViewModel } from './belt-view-model';

@Component({
  selector: 'app-belt-stage',
  templateUrl: './belt-stage.html',
  styleUrl: './belt-stage.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BeltStageComponent {
  readonly stage = input.required<BeltStageViewModel>();
  readonly reducedMotion = input(false);
  readonly paused = input(false);
  readonly degraded = input(false);
}
