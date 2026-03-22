import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';

import type {
  MenuItemDto,
  MenuItemTier,
  OperatorPlacementDraftPatch,
  OperatorPlacementViewModel,
} from '../api/types';

@Component({
  selector: 'app-operator-plate-placement',
  templateUrl: './operator-plate-placement.component.html',
  styleUrl: './operator-plate-placement.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    '[attr.data-mode]': 'placement()?.presentationMode ?? null',
  },
})
export class OperatorPlatePlacementComponent {
  readonly placement = input<OperatorPlacementViewModel | null>(null);

  readonly dismiss = output<void>();
  readonly searchChange = output<string>();
  readonly selectMenuItem = output<string>();
  readonly draftChange = output<OperatorPlacementDraftPatch>();
  readonly submitPlacement = output<void>();
  readonly retryMenuLoad = output<void>();

  protected readonly tierOptions: MenuItemTier[] = ['GREEN', 'RED', 'GOLD', 'BLACK'];

  protected trackMenuItem(_index: number, item: MenuItemDto): string {
    return item.id;
  }

  protected onSearchInput(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    this.searchChange.emit(target?.value ?? '');
  }

  protected onPlateCountInput(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    this.draftChange.emit({ numOfPlates: Number(target?.value ?? 1) });
  }

  protected onTierChange(event: Event): void {
    const target = event.target as HTMLSelectElement | null;
    this.draftChange.emit({
      tierSnapshot: (target?.value as MenuItemTier | '') || null,
    });
  }

  protected onPriceInput(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    this.draftChange.emit({ priceAtCreation: target?.value ?? '' });
  }

  protected onExpiresAtInput(event: Event): void {
    const target = event.target as HTMLInputElement | null;
    this.draftChange.emit({ expiresAt: target?.value ?? '' });
  }
}
