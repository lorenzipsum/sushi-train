import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import type { OrderSummaryDto, SelectedSeatDetailViewModel } from '../api/types';

@Component({
  selector: 'app-selected-seat-detail',
  templateUrl: './selected-seat-detail.component.html',
  styleUrl: './selected-seat-detail.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SelectedSeatDetailComponent {
  readonly detail = input<SelectedSeatDetailViewModel | null>(null);

  readonly startDining = output<void>();
  readonly checkout = output<void>();

  protected readonly trackOrderLine = (
    _index: number,
    line: NonNullable<OrderSummaryDto['lines']>[number],
  ) => `${line.menuItemName ?? 'line'}-${_index}`;
}
