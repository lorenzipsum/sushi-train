import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import type {
  BeltStagePlateViewModel,
  BeltStageSeatViewModel,
  BeltStageViewModel,
} from './belt-view-model';

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
  readonly selectSeat = output<string>();
  readonly pickPlate = output<string>();

  handleSeatClick(seat: BeltStageSeatViewModel): void {
    this.selectSeat.emit(seat.id);
  }

  handlePlateClick(plate: BeltStagePlateViewModel): void {
    this.pickPlate.emit(plate.id);
  }
}
