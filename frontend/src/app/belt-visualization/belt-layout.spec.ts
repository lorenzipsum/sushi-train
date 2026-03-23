import { describe, expect, it } from 'vitest';

import {
  getCounterSeatPoint,
  getRacetrackPoint,
  getStageReadabilityProfile,
  getSlotLayoutPoint,
  getStageSizing,
} from './belt-layout';

describe('belt-layout', () => {
  it('maps progress onto the rounded-square straights and corners', () => {
    expect(getRacetrackPoint(0)).toMatchObject({ segment: 'top-straight', yPercent: 17.5 });
    expect(getRacetrackPoint(0.26).segment).toBe('top-right-corner');
    expect(getRacetrackPoint(0.34).segment).toBe('right-straight');
    expect(getRacetrackPoint(0.6).segment).toBe('bottom-straight');
    expect(getRacetrackPoint(0.9).segment).toBe('left-straight');
  });

  it('applies render offset without reordering slot identity', () => {
    const basePoint = getSlotLayoutPoint(0, 8, 0);
    const shiftedPoint = getSlotLayoutPoint(0, 8, 3.5);

    expect(shiftedPoint.xPercent).not.toBe(basePoint.xPercent);
    expect(shiftedPoint.segment).not.toBe(basePoint.segment);
  });

  it('distributes counter seats across all four sides while keeping them belt-facing', () => {
    const bottomSeat = getCounterSeatPoint(0, 8);
    const rightSeat = getCounterSeatPoint(3, 8);
    const topSeat = getCounterSeatPoint(4, 8);
    const leftSeat = getCounterSeatPoint(7, 8);

    expect(bottomSeat.yPercent).toBeGreaterThan(topSeat.yPercent);
    expect(rightSeat.xPercent).toBeGreaterThan(bottomSeat.xPercent);
    expect(leftSeat.xPercent).toBeLessThan(bottomSeat.xPercent);
    expect(bottomSeat.facingDeg).toBe(0);
    expect(rightSeat.facingDeg).toBe(-90);
    expect(topSeat.facingDeg).toBe(180);
    expect(leftSeat.facingDeg).toBe(90);
  });

  it('keeps the bottom-seat clearance aligned with the top-seat clearance from the belt edge', () => {
    const bottomSeat = getCounterSeatPoint(0, 8);
    const topSeat = getCounterSeatPoint(4, 8);
    const bottomBeltEdge = getRacetrackPoint(0.6).yPercent;
    const topBeltEdge = getRacetrackPoint(0).yPercent;

    expect(bottomSeat.yPercent - bottomBeltEdge).toBe(topBeltEdge - topSeat.yPercent);
  });

  it('weights seat counts toward the longer top and bottom sides', () => {
    const seats = Array.from({ length: 24 }, (_, index) => getCounterSeatPoint(index, 24));
    const bottomSeats = seats.filter((seat) => seat.facingDeg === 0).length;
    const rightSeats = seats.filter((seat) => seat.facingDeg === -90).length;
    const topSeats = seats.filter((seat) => seat.facingDeg === 180).length;
    const leftSeats = seats.filter((seat) => seat.facingDeg === 90).length;

    expect(bottomSeats).toBe(8);
    expect(rightSeats).toBe(4);
    expect(topSeats).toBe(8);
    expect(leftSeats).toBe(4);
  });

  it('shrinks plate and seat sizing as density increases while keeping the redesign larger', () => {
    const roomy = getStageSizing(8, 6);
    const dense = getStageSizing(20, 12);

    expect(roomy.plateSizePx).toBeGreaterThan(18);
    expect(roomy.plateSizePx).toBeGreaterThan(dense.plateSizePx);
    expect(roomy.seatSizePx).toBeGreaterThan(dense.seatSizePx);
    expect(dense.slotMarkerSizePx).toBeGreaterThanOrEqual(10);
  });

  it('keeps the current-balanced layout while trimming ornaments on denser counters', () => {
    const roomy = getStageReadabilityProfile(8, 6);
    const dense = getStageReadabilityProfile(20, 12);

    expect(roomy.layoutVariant).toBe('current-balanced');
    expect(dense.layoutVariant).toBe('current-balanced');
    expect(roomy.ornamentDensity).toBe('full');
    expect(dense.ornamentDensity).toBe('trimmed');
    expect(dense.seatLabelMode).toBe('compact');
  });
});
