import { describe, expect, it } from 'vitest';

import type { BeltSnapshotDto, SeatStateListDto } from '../api/types';
import { buildBeltStageViewModel } from './belt-view-model';

describe('buildBeltStageViewModel', () => {
  it('preserves slot and seat counts from backend data and derives larger counter-loop sizing', () => {
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
    expect(result.plateSizePx).toBeGreaterThan(18);
    expect(result.slots[0].segment).toBeTruthy();
    expect(result.slots[0].pathProgress).toBe(0);
    expect(result.seats[1].positionIndex).toBe(4);
    expect(result.seats[0].yPercent).toBeGreaterThan(result.seats[1].yPercent);
    expect(result.seats[1].xPercent).toBeGreaterThanOrEqual(5);
    expect(result.seats[1].xPercent).toBeLessThanOrEqual(95);
    expect([0, -90, 90, 180]).toContain(result.seats[1].facingDeg);
    expect(result.seats[1].presenceCue).toBe('occupied');
    expect(result.kitchen.showChef).toBe(true);
  });

  it('adds menu-item family metadata and render-offset-aware slot positions', () => {
    const snapshot: BeltSnapshotDto = {
      beltName: 'Main Belt',
      beltSlotCount: 8,
      slots: [
        {
          slotId: 'slot-1',
          positionIndex: 0,
          plate: { plateId: 'plate-1', menuItemName: 'Tamago Nigiri', tier: 'GREEN' },
        },
      ],
    };

    const atRest = buildBeltStageViewModel(snapshot, [], 0);
    const shifted = buildBeltStageViewModel(snapshot, [], 2);

    expect(atRest.slots[0].plate?.visual.family).toBe('nigiri');
    expect(atRest.slots[0].plate?.visual.visualKey).toBe('tamago-nigiri');
    expect(shifted.slots[0].xPercent).not.toBe(atRest.slots[0].xPercent);
  });

  it('shrinks plate sizing when occupied slots cluster tightly together', () => {
    const snapshot: BeltSnapshotDto = {
      beltName: 'Main Belt',
      beltSlotCount: 12,
      slots: [
        {
          slotId: 'slot-1',
          positionIndex: 0,
          plate: { plateId: 'plate-1', menuItemName: 'Salmon Nigiri', tier: 'RED' },
        },
        {
          slotId: 'slot-2',
          positionIndex: 1,
          plate: { plateId: 'plate-2', menuItemName: 'Tuna Nigiri', tier: 'RED' },
        },
        {
          slotId: 'slot-3',
          positionIndex: 2,
          plate: { plateId: 'plate-3', menuItemName: 'Tamago Nigiri', tier: 'GREEN' },
        },
        { slotId: 'slot-4', positionIndex: 8 },
      ],
    };

    const sparseSnapshot: BeltSnapshotDto = {
      beltName: 'Main Belt',
      beltSlotCount: 12,
      slots: [
        {
          slotId: 'slot-1',
          positionIndex: 0,
          plate: { plateId: 'plate-1', menuItemName: 'Salmon Nigiri', tier: 'RED' },
        },
        {
          slotId: 'slot-2',
          positionIndex: 4,
          plate: { plateId: 'plate-2', menuItemName: 'Tuna Nigiri', tier: 'RED' },
        },
        {
          slotId: 'slot-3',
          positionIndex: 8,
          plate: { plateId: 'plate-3', menuItemName: 'Tamago Nigiri', tier: 'GREEN' },
        },
      ],
    };

    const crowded = buildBeltStageViewModel(snapshot, []);
    const sparse = buildBeltStageViewModel(sparseSnapshot, []);

    expect(crowded.plateSizePx).toBeLessThan(sparse.plateSizePx);
    expect(crowded.plateSizePx).toBeGreaterThanOrEqual(24);
  });
});
