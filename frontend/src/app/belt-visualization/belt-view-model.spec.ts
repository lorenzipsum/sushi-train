import { describe, expect, it } from 'vitest';

import type { BeltSnapshotDto, SeatStateListDto } from '../api/types';
import { buildBeltStageViewModel } from './belt-view-model';

describe('buildBeltStageViewModel', () => {
  it('preserves slot and seat counts from backend data', () => {
    const snapshot: BeltSnapshotDto = {
      beltName: 'Main Belt',
      beltSlotCount: 8,
      slots: [
        { slotId: 'slot-1', positionIndex: 0 },
        {
          slotId: 'slot-2',
          positionIndex: 1,
          plate: { plateId: 'plate-1', menuItemName: 'Salmon', tier: 'RED' },
        },
      ],
    };
    const seats: SeatStateListDto = [
      { seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: false },
      { seatId: 'seat-2', label: 'Seat 2', positionIndex: 4, isOccupied: true },
    ];

    const result = buildBeltStageViewModel(snapshot, seats);

    expect(result.slotCount).toBe(8);
    expect(result.occupiedPlateCount).toBe(1);
    expect(result.slots).toHaveLength(2);
    expect(result.seats).toHaveLength(2);
    expect(result.seats[1].isOccupied).toBe(true);
  });
});
