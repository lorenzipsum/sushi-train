import type { BeltSnapshotDto } from '../api/types';

function getSlotCount(snapshot: BeltSnapshotDto | null): number {
  return Math.max(0, snapshot?.beltSlotCount ?? 0);
}

export function normalizeOffset(value: number, slotCount: number): number {
  if (!Number.isFinite(value) || slotCount <= 0) {
    return 0;
  }

  return ((value % slotCount) + slotCount) % slotCount;
}

function getStartedAtMs(snapshot: BeltSnapshotDto | null): number | null {
  if (!snapshot?.beltOffsetStartedAt) {
    return null;
  }

  const startedAtMs = Date.parse(snapshot.beltOffsetStartedAt);
  return Number.isFinite(startedAtMs) ? startedAtMs : null;
}

export function getDiscreteOffset(snapshot: BeltSnapshotDto | null, nowMs: number): number {
  const slotCount = getSlotCount(snapshot);
  if (slotCount === 0) {
    return 0;
  }

  const baseOffset = snapshot?.beltBaseRotationOffset ?? 0;
  const tickIntervalMs = snapshot?.beltTickIntervalMs ?? 0;
  const speedSlotsPerTick = snapshot?.beltSpeedSlotsPerTick ?? 0;
  const startedAtMs = getStartedAtMs(snapshot);

  if (!startedAtMs || tickIntervalMs <= 0 || speedSlotsPerTick === 0) {
    return normalizeOffset(baseOffset, slotCount);
  }

  const elapsedMs = Math.max(0, nowMs - startedAtMs);
  const elapsedTicks = Math.floor(elapsedMs / tickIntervalMs);

  return normalizeOffset(baseOffset + elapsedTicks * speedSlotsPerTick, slotCount);
}

export function getInterpolatedOffset(snapshot: BeltSnapshotDto | null, nowMs: number): number {
  const slotCount = getSlotCount(snapshot);
  if (slotCount === 0) {
    return 0;
  }

  const baseOffset = snapshot?.beltBaseRotationOffset ?? 0;
  const tickIntervalMs = snapshot?.beltTickIntervalMs ?? 0;
  const speedSlotsPerTick = snapshot?.beltSpeedSlotsPerTick ?? 0;
  const startedAtMs = getStartedAtMs(snapshot);

  if (!startedAtMs || tickIntervalMs <= 0 || speedSlotsPerTick === 0) {
    return normalizeOffset(baseOffset, slotCount);
  }

  const elapsedMs = Math.max(0, nowMs - startedAtMs);
  const interpolatedTicks = elapsedMs / tickIntervalMs;

  return normalizeOffset(baseOffset + interpolatedTicks * speedSlotsPerTick, slotCount);
}

export function getRenderOffset(
  snapshot: BeltSnapshotDto | null,
  nowMs: number,
  reducedMotion: boolean,
): number {
  return reducedMotion
    ? getDiscreteOffset(snapshot, nowMs)
    : getInterpolatedOffset(snapshot, nowMs);
}

export function getRotationDegrees(
  snapshot: BeltSnapshotDto | null,
  nowMs: number,
  reducedMotion: boolean,
): number {
  const slotCount = getSlotCount(snapshot);
  if (slotCount === 0) {
    return 0;
  }

  return (getRenderOffset(snapshot, nowMs, reducedMotion) / slotCount) * 360;
}

export function unwrapRotationDegrees(
  previousRawDegrees: number | null,
  previousContinuousDegrees: number | null,
  nextRawDegrees: number,
  direction: number,
): number {
  if (previousRawDegrees == null || previousContinuousDegrees == null) {
    return nextRawDegrees;
  }

  let delta = nextRawDegrees - previousRawDegrees;

  if (direction > 0 && delta < -180) {
    delta += 360;
  } else if (direction < 0 && delta > 180) {
    delta -= 360;
  }

  return previousContinuousDegrees + delta;
}
