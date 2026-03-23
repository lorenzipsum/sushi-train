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
    expect(result.selectedSeatId).toBe('seat-1');
    expect(result.reachArea?.seatId).toBe('seat-1');
    expect(result.seats[0].isSelected).toBe(true);
    expect(result.seats[0].statusLabel).toBe('Available');
    expect(result.seats[1].statusLabel).toBe('Occupied');
    expect(result.seats[0].secondaryLabel).toBe('Tap to start');
    expect(result.presentation.layoutVariant).toBe('current-balanced');
    expect(result.presentation.primaryLabel).toBe('Counter loop overview');
    expect(result.presentation.legends).toHaveLength(3);
    expect(result.kitchen.showChef).toBe(true);
    expect(result.kitchen.chefSecondaryLabel).toContain('Knife skills');
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

    const seats: SeatStateListDto = [
      { seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true },
    ];

    const atRest = buildBeltStageViewModel(snapshot, seats, 0, {
      selectedSeatId: 'seat-1',
      activeOrdersBySeatId: {
        'seat-1': {
          orderId: 'order-1',
          createdAt: '2026-03-15T03:00:00Z',
          seatId: 'seat-1',
          status: 'OPEN',
        },
      },
    });
    const shifted = buildBeltStageViewModel(snapshot, [], 2);

    expect(atRest.slots[0].plate?.visual.family).toBe('nigiri');
    expect(atRest.slots[0].plate?.visual.visualKey).toBe('tamago-nigiri');
    expect(atRest.reachArea?.seatId).toBe('seat-1');
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

  it('marks pending seats as non-actionable and exposes durable occupancy context for occupied seats', () => {
    const snapshot: BeltSnapshotDto = {
      beltName: 'Main Belt',
      beltSlotCount: 8,
      slots: [{ slotId: 'slot-1', positionIndex: 0 }],
    };
    const seats: SeatStateListDto = [
      { seatId: 'seat-1', label: 'A1', positionIndex: 0, isOccupied: false },
      { seatId: 'seat-2', label: 'A2', positionIndex: 1, isOccupied: true },
    ];

    const result = buildBeltStageViewModel(snapshot, seats, 0, {
      pendingSeatId: 'seat-1',
      pendingAction: 'occupy',
      selectedSeatId: 'seat-1',
      activeOrdersBySeatId: {
        'seat-2': {
          orderId: 'order-2',
          createdAt: '2026-03-15T03:00:00Z',
          seatId: 'seat-2',
          status: 'OPEN',
        },
      },
    });

    expect(result.seats[0].isPending).toBe(true);
    expect(result.seats[0].presenceCue).toBe('pending');
    expect(result.seats[0].statusLabel).toBe('Starting dining');
    expect(result.seats[0].secondaryLabel).toBe('Starting dining');
    expect(result.seats[0].isSelected).toBe(true);
    expect(result.seats[1].orderId).toBe('order-2');
    expect(result.seats[1].occupiedSince).toBe('2026-03-15T03:00:00Z');
    expect(result.seats[1].statusLabel).toBe('Occupied');
    expect(result.seats[1].ariaLabel).toContain('Active order order-2');
    expect(result.seats[1].ariaLabel).toContain('Activate to select this seat.');
  });

  it('keeps reach cues visible but blocks pickability while the selected occupied seat is still syncing', () => {
    const snapshot: BeltSnapshotDto = {
      beltName: 'Main Belt',
      beltSlotCount: 8,
      slots: [
        {
          slotId: 'slot-1',
          positionIndex: 0,
          plate: { plateId: 'plate-1', menuItemName: 'Salmon Nigiri', tier: 'RED' },
        },
      ],
    };
    const seats: SeatStateListDto = [
      { seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: true },
    ];

    const result = buildBeltStageViewModel(snapshot, seats, 0, {
      selectedSeatId: 'seat-1',
      restorationBySeatId: {
        'seat-1': {
          seatId: 'seat-1',
          restorationStatus: 'syncing',
          hasRetryInFlight: false,
          lastKnownOrderSummary: null,
          resolutionMessage: 'Syncing dining state from the backend.',
        },
      },
    });

    expect(result.reachArea?.seatId).toBe('seat-1');
    expect(result.seats[0].restorationStatus).toBe('syncing');
    expect(result.seats[0].statusLabel).toBe('Syncing');
    expect(result.slots[0].plate?.isPickable).toBe(false);
  });

  it('keeps the reconciled selected seat available and non-pickable after no active order is confirmed', () => {
    const snapshot: BeltSnapshotDto = {
      beltName: 'Main Belt',
      beltSlotCount: 8,
      slots: [
        {
          slotId: 'slot-1',
          positionIndex: 0,
          plate: { plateId: 'plate-1', menuItemName: 'Salmon Nigiri', tier: 'RED' },
        },
      ],
    };
    const seats: SeatStateListDto = [
      { seatId: 'seat-1', label: 'Seat 1', positionIndex: 0, isOccupied: false },
    ];

    const result = buildBeltStageViewModel(snapshot, seats, 0, {
      selectedSeatId: 'seat-1',
      restorationBySeatId: {
        'seat-1': {
          seatId: 'seat-1',
          restorationStatus: 'confirmed-no-order',
          hasRetryInFlight: false,
          lastKnownOrderSummary: null,
          resolutionMessage: 'No active dining record remains for this seat.',
        },
      },
    });

    expect(result.seats[0].isOccupied).toBe(false);
    expect(result.seats[0].restorationStatus).toBe('confirmed-no-order');
    expect(result.seats[0].statusLabel).toBe('Available');
    expect(result.slots[0].plate?.isPickable).toBe(false);
  });
});
