import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { BeltStageComponent } from './belt-visualization/belt-stage.component';
import { BeltVisualizationStore } from './belt-visualization/belt-visualization.store';
import { OperatorPlatePlacementComponent } from './belt-visualization/operator-plate-placement.component';
import { SelectedSeatDetailComponent } from './belt-visualization/selected-seat-detail.component';

@Component({
  selector: 'app-root',
  imports: [BeltStageComponent, OperatorPlatePlacementComponent, SelectedSeatDetailComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  protected readonly store = inject(BeltVisualizationStore);
  protected readonly selectedSeatDetail = this.store.selectedSeatDetail;
  protected readonly operatorPlacement = this.store.operatorPlacement;
}
