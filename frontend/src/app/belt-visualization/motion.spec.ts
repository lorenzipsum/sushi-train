import { describe, expect, it } from 'vitest';

import type { BeltSnapshotDto } from '../api/types';
import {
  getDiscreteOffset,
  getInterpolatedOffset,
  getRotationDegrees,
  unwrapRotationDegrees,
} from './motion';

function createSnapshot(overrides: Partial<BeltSnapshotDto> = {}): BeltSnapshotDto {
  return {
    beltSlotCount: 12,
    beltBaseRotationOffset: 2,
    beltOffsetStartedAt: '2026-03-14T10:00:00.000Z',
    beltTickIntervalMs: 500,
    beltSpeedSlotsPerTick: 1,
    slots: [],
    ...overrides,
  };
}

describe('motion helpers', () => {
  it('calculates discrete offsets from tick boundaries', () => {
    const snapshot = createSnapshot();
    const nowMs = Date.parse('2026-03-14T10:00:01.250Z');

    expect(getDiscreteOffset(snapshot, nowMs)).toBe(4);
  });

  it('interpolates offset between ticks when motion is allowed', () => {
    const snapshot = createSnapshot();
    const nowMs = Date.parse('2026-03-14T10:00:01.250Z');

    expect(getInterpolatedOffset(snapshot, nowMs)).toBeCloseTo(4.5);
  });

  it('returns the base offset when speed is zero', () => {
    const snapshot = createSnapshot({ beltSpeedSlotsPerTick: 0 });
    const nowMs = Date.parse('2026-03-14T10:00:05.000Z');

    expect(getDiscreteOffset(snapshot, nowMs)).toBe(2);
    expect(getInterpolatedOffset(snapshot, nowMs)).toBe(2);
  });

  it('converts slot offsets to degrees', () => {
    const snapshot = createSnapshot();
    const nowMs = Date.parse('2026-03-14T10:00:01.000Z');

    expect(getRotationDegrees(snapshot, nowMs, false)).toBeCloseTo(120);
  });

  it('unwraps a wrapped rotation so the belt keeps moving forward', () => {
    expect(unwrapRotationDegrees(358, 358, 2, 1)).toBe(362);
  });
});
