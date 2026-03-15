import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import type { BeltStageSeatViewModel, BeltStageViewModel } from './belt-view-model';

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
  readonly occupySeat = output<string>();
  readonly checkoutSeat = output<string>();

  handleSeatClick(seat: BeltStageSeatViewModel): void {
    if (!seat.isActionable) {
      return;
    }

    if (seat.seatAction === 'checkout') {
      this.checkoutSeat.emit(seat.id);
      return;
    }

    this.occupySeat.emit(seat.id);
  }
}
