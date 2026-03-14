import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { BeltStageComponent } from './belt-visualization/belt-stage.component';
import { BeltVisualizationStore } from './belt-visualization/belt-visualization.store';

@Component({
  selector: 'app-root',
  imports: [BeltStageComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  protected readonly store = inject(BeltVisualizationStore);
}
