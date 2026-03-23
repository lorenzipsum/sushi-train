export type BeltTrackSegment =
  | 'top-straight'
  | 'top-right-corner'
  | 'right-straight'
  | 'bottom-right-corner'
  | 'bottom-straight'
  | 'bottom-left-corner'
  | 'left-straight'
  | 'top-left-corner';

export interface BeltLayoutPoint {
  xPercent: number;
  yPercent: number;
  tangentDeg: number;
  segment: BeltTrackSegment;
}

export interface CounterSeatPoint {
  xPercent: number;
  yPercent: number;
  facingDeg: number;
}

export interface BeltStageSizing {
  plateSizePx: number;
  slotMarkerSizePx: number;
  seatSizePx: number;
}

export interface BeltStageReadabilityProfile {
  layoutVariant: 'current-balanced';
  ornamentDensity: 'full' | 'trimmed';
  seatLabelMode: 'full' | 'compact';
}

const TRACK_LEFT_PERCENT = 10.5;
const TRACK_RIGHT_PERCENT = 89.5;
const TRACK_TOP_PERCENT = 17.5;
const TRACK_BOTTOM_PERCENT = 82.5;
const TRACK_CORNER_RADIUS_PERCENT = 4;

const SEAT_LEFT_PERCENT = 5;
const SEAT_RIGHT_PERCENT = 95;
const SEAT_TOP_PERCENT = 7.5;
const SEAT_BOTTOM_PERCENT = 91.5;
const SEAT_SIDE_WEIGHTS = [2, 1, 2, 1] as const;

function clamp(value: number, min: number, max: number): number {
  return Math.min(max, Math.max(min, value));
}

function normalizeProgress(progress: number): number {
  if (!Number.isFinite(progress)) {
    return 0;
  }

  return ((progress % 1) + 1) % 1;
}

function getSeatSideCounts(seatCount: number): [number, number, number, number] {
  const safeSeatCount = Math.max(seatCount, 1);

  if (safeSeatCount === 1) {
    return [1, 0, 0, 0];
  }

  if (safeSeatCount === 2) {
    return [1, 0, 1, 0];
  }

  if (safeSeatCount === 3) {
    return [1, 1, 1, 0];
  }

  const counts: [number, number, number, number] = [1, 1, 1, 1];
  const remainingSeats = safeSeatCount - 4;
  const totalWeight = SEAT_SIDE_WEIGHTS.reduce((sum, weight) => sum + weight, 0);
  const rawExtras = SEAT_SIDE_WEIGHTS.map((weight) => (remainingSeats * weight) / totalWeight);
  const extraCounts = rawExtras.map((value) => Math.floor(value));

  let assignedExtras = 0;
  for (let index = 0; index < extraCounts.length; index += 1) {
    counts[index] += extraCounts[index];
    assignedExtras += extraCounts[index];
  }

  const rankedRemainders = rawExtras
    .map((value, index) => ({
      index,
      remainder: value - extraCounts[index],
      priority: index === 0 ? 2 : index === 2 ? 1 : 0,
    }))
    .sort((left, right) => {
      if (right.remainder !== left.remainder) {
        return right.remainder - left.remainder;
      }

      if (right.priority !== left.priority) {
        return right.priority - left.priority;
      }

      return left.index - right.index;
    });

  for (let index = 0; index < remainingSeats - assignedExtras; index += 1) {
    counts[rankedRemainders[index].index] += 1;
  }

  return counts;
}

export function getRacetrackPoint(progress: number): BeltLayoutPoint {
  const normalizedProgress = normalizeProgress(progress);
  const straightWidth = TRACK_RIGHT_PERCENT - TRACK_LEFT_PERCENT - TRACK_CORNER_RADIUS_PERCENT * 2;
  const straightHeight = TRACK_BOTTOM_PERCENT - TRACK_TOP_PERCENT - TRACK_CORNER_RADIUS_PERCENT * 2;
  const cornerLength = (Math.PI * TRACK_CORNER_RADIUS_PERCENT) / 2;
  const totalLength = straightWidth * 2 + straightHeight * 2 + cornerLength * 4;
  const travel = normalizedProgress * totalLength;
  const topRightCornerStart = straightWidth;
  const rightStraightStart = topRightCornerStart + cornerLength;
  const bottomRightCornerStart = rightStraightStart + straightHeight;
  const bottomStraightStart = bottomRightCornerStart + cornerLength;
  const bottomLeftCornerStart = bottomStraightStart + straightWidth;
  const leftStraightStart = bottomLeftCornerStart + cornerLength;
  const topLeftCornerStart = leftStraightStart + straightHeight;

  if (travel <= topRightCornerStart) {
    return {
      xPercent: TRACK_LEFT_PERCENT + TRACK_CORNER_RADIUS_PERCENT + travel,
      yPercent: TRACK_TOP_PERCENT,
      tangentDeg: 0,
      segment: 'top-straight',
    };
  }

  if (travel <= rightStraightStart) {
    const arcTravel = travel - topRightCornerStart;
    const angleRad = -Math.PI / 2 + arcTravel / TRACK_CORNER_RADIUS_PERCENT;

    return {
      xPercent:
        TRACK_RIGHT_PERCENT -
        TRACK_CORNER_RADIUS_PERCENT +
        Math.cos(angleRad) * TRACK_CORNER_RADIUS_PERCENT,
      yPercent:
        TRACK_TOP_PERCENT +
        TRACK_CORNER_RADIUS_PERCENT +
        Math.sin(angleRad) * TRACK_CORNER_RADIUS_PERCENT,
      tangentDeg: (angleRad * 180) / Math.PI + 90,
      segment: 'top-right-corner',
    };
  }

  if (travel <= bottomRightCornerStart) {
    const straightTravel = travel - rightStraightStart;

    return {
      xPercent: TRACK_RIGHT_PERCENT,
      yPercent: TRACK_TOP_PERCENT + TRACK_CORNER_RADIUS_PERCENT + straightTravel,
      tangentDeg: 90,
      segment: 'right-straight',
    };
  }

  if (travel <= bottomStraightStart) {
    const arcTravel = travel - bottomRightCornerStart;
    const angleRad = arcTravel / TRACK_CORNER_RADIUS_PERCENT;

    return {
      xPercent:
        TRACK_RIGHT_PERCENT -
        TRACK_CORNER_RADIUS_PERCENT +
        Math.cos(angleRad) * TRACK_CORNER_RADIUS_PERCENT,
      yPercent:
        TRACK_BOTTOM_PERCENT -
        TRACK_CORNER_RADIUS_PERCENT +
        Math.sin(angleRad) * TRACK_CORNER_RADIUS_PERCENT,
      tangentDeg: (angleRad * 180) / Math.PI + 90,
      segment: 'bottom-right-corner',
    };
  }

  if (travel <= bottomLeftCornerStart) {
    const straightTravel = travel - bottomStraightStart;

    return {
      xPercent: TRACK_RIGHT_PERCENT - TRACK_CORNER_RADIUS_PERCENT - straightTravel,
      yPercent: TRACK_BOTTOM_PERCENT,
      tangentDeg: 180,
      segment: 'bottom-straight',
    };
  }

  if (travel <= leftStraightStart) {
    const arcTravel = travel - bottomLeftCornerStart;
    const angleRad = Math.PI / 2 + arcTravel / TRACK_CORNER_RADIUS_PERCENT;

    return {
      xPercent:
        TRACK_LEFT_PERCENT +
        TRACK_CORNER_RADIUS_PERCENT +
        Math.cos(angleRad) * TRACK_CORNER_RADIUS_PERCENT,
      yPercent:
        TRACK_BOTTOM_PERCENT -
        TRACK_CORNER_RADIUS_PERCENT +
        Math.sin(angleRad) * TRACK_CORNER_RADIUS_PERCENT,
      tangentDeg: (angleRad * 180) / Math.PI + 90,
      segment: 'bottom-left-corner',
    };
  }

  if (travel <= topLeftCornerStart) {
    const straightTravel = travel - leftStraightStart;

    return {
      xPercent: TRACK_LEFT_PERCENT,
      yPercent: TRACK_BOTTOM_PERCENT - TRACK_CORNER_RADIUS_PERCENT - straightTravel,
      tangentDeg: 270,
      segment: 'left-straight',
    };
  }

  const arcTravel = travel - topLeftCornerStart;
  const angleRad = Math.PI + arcTravel / TRACK_CORNER_RADIUS_PERCENT;

  return {
    xPercent:
      TRACK_LEFT_PERCENT +
      TRACK_CORNER_RADIUS_PERCENT +
      Math.cos(angleRad) * TRACK_CORNER_RADIUS_PERCENT,
    yPercent:
      TRACK_TOP_PERCENT +
      TRACK_CORNER_RADIUS_PERCENT +
      Math.sin(angleRad) * TRACK_CORNER_RADIUS_PERCENT,
    tangentDeg: (angleRad * 180) / Math.PI + 90,
    segment: 'top-left-corner',
  };
}

export function getSlotLayoutPoint(
  positionIndex: number,
  slotCount: number,
  renderOffset: number,
): BeltLayoutPoint {
  if (!Number.isFinite(positionIndex) || slotCount <= 0) {
    return getRacetrackPoint(0);
  }

  return getRacetrackPoint((positionIndex + renderOffset) / slotCount);
}

export function getCounterSeatPoint(positionIndex: number, seatCount: number): CounterSeatPoint {
  const safeSeatCount = Math.max(seatCount, 1);
  if (safeSeatCount === 1) {
    return {
      xPercent: 50,
      yPercent: SEAT_BOTTOM_PERCENT,
      facingDeg: 0,
    };
  }

  const safeIndex = clamp(Math.floor(positionIndex), 0, safeSeatCount - 1);
  const [bottomCount, rightCount, topCount, leftCount] = getSeatSideCounts(safeSeatCount);

  if (safeIndex < bottomCount) {
    const sideProgress = (safeIndex + 1) / (bottomCount + 1);

    return {
      xPercent: SEAT_LEFT_PERCENT + sideProgress * (SEAT_RIGHT_PERCENT - SEAT_LEFT_PERCENT),
      yPercent: SEAT_BOTTOM_PERCENT,
      facingDeg: 0,
    };
  }

  if (safeIndex < bottomCount + rightCount) {
    const sideOrder = safeIndex - bottomCount;
    const sideProgress = (sideOrder + 1) / (rightCount + 1);

    return {
      xPercent: SEAT_RIGHT_PERCENT,
      yPercent: SEAT_BOTTOM_PERCENT - sideProgress * (SEAT_BOTTOM_PERCENT - SEAT_TOP_PERCENT),
      facingDeg: -90,
    };
  }

  if (safeIndex < bottomCount + rightCount + topCount) {
    const sideOrder = safeIndex - bottomCount - rightCount;
    const sideProgress = (sideOrder + 1) / (topCount + 1);

    return {
      xPercent: SEAT_RIGHT_PERCENT - sideProgress * (SEAT_RIGHT_PERCENT - SEAT_LEFT_PERCENT),
      yPercent: SEAT_TOP_PERCENT,
      facingDeg: 180,
    };
  }

  const sideOrder = safeIndex - bottomCount - rightCount - topCount;
  const sideProgress = (sideOrder + 1) / (leftCount + 1);

  return {
    xPercent: SEAT_LEFT_PERCENT,
    yPercent: SEAT_TOP_PERCENT + sideProgress * (SEAT_BOTTOM_PERCENT - SEAT_TOP_PERCENT),
    facingDeg: 90,
  };
}

export function getStageSizing(slotCount: number, seatCount: number): BeltStageSizing {
  const safeSlotCount = Math.max(slotCount, 1);
  const safeSeatCount = Math.max(seatCount, 1);

  return {
    plateSizePx: clamp(Math.round(48 - safeSlotCount * 1.4), 28, 40),
    slotMarkerSizePx: clamp(Math.round(22 - safeSlotCount * 0.45), 10, 18),
    seatSizePx: clamp(Math.round(72 - safeSeatCount * 1.8), 44, 64),
  };
}

export function getStageReadabilityProfile(
  slotCount: number,
  seatCount: number,
): BeltStageReadabilityProfile {
  const safeSlotCount = Math.max(slotCount, 1);
  const safeSeatCount = Math.max(seatCount, 1);
  const denseCounter = safeSlotCount >= 16 || safeSeatCount >= 10;

  return {
    layoutVariant: 'current-balanced',
    ornamentDensity: denseCounter ? 'trimmed' : 'full',
    seatLabelMode: safeSeatCount >= 10 ? 'compact' : 'full',
  };
}
