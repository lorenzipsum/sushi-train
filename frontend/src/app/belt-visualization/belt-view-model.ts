import type {
  BeltSnapshotDto,
  BeltSlotSnapshotDto,
  PlateSnapshotDto,
  SeatStateDto,
  SeatStateListDto,
} from '../api/types';
import {
  getCounterSeatPoint,
  getSlotLayoutPoint,
  getStageSizing,
  type BeltTrackSegment,
} from './belt-layout';
import { resolveMenuItemVisual, type MenuItemVisual } from './menu-item-visuals';

export interface BeltStagePlateViewModel {
  id: string;
  menuItemName: string;
  tierClass: string;
  className: string;
  foodClassName: string;
  visual: MenuItemVisual;
  ariaLabel: string;
}

export interface BeltStageSlotViewModel {
  id: string;
  positionIndex: number;
  pathProgress: number;
  xPercent: number;
  yPercent: number;
  tangentDeg: number;
  segment: BeltTrackSegment;
  plate: BeltStagePlateViewModel | null;
  ariaLabel: string;
}

export interface BeltStageSeatViewModel {
  id: string;
  label: string;
  positionIndex: number;
  xPercent: number;
  yPercent: number;
  facingDeg: number;
  isOccupied: boolean;
  presenceCue: 'available' | 'occupied';
  ariaLabel: string;
}

export interface BeltStageKitchenViewModel {
  showChef: boolean;
  chefLabel: string;
  accentLabels: [string, string, string];
}

export interface BeltStageViewModel {
  beltName: string;
  slotCount: number;
  occupiedPlateCount: number;
  slots: BeltStageSlotViewModel[];
  seats: BeltStageSeatViewModel[];
  kitchen: BeltStageKitchenViewModel;
  plateSizePx: number;
  slotMarkerSizePx: number;
  seatSizePx: number;
}

function getOccupiedPlateSizePx(
  basePlateSizePx: number,
  slotCount: number,
  slots: BeltSlotSnapshotDto[],
): number {
  const occupiedPositions = slots
    .filter((slot) => !!slot.plate)
    .map((slot, index) => slot.positionIndex ?? index)
    .sort((left, right) => left - right);

  if (occupiedPositions.length <= 1 || slotCount <= 1) {
    return basePlateSizePx;
  }

  let minimumGap = slotCount;
  for (let index = 0; index < occupiedPositions.length; index += 1) {
    const current = occupiedPositions[index];
    const next = occupiedPositions[(index + 1) % occupiedPositions.length];
    const gap =
      index === occupiedPositions.length - 1 ? next + slotCount - current : next - current;
    minimumGap = Math.min(minimumGap, gap);
  }

  const occupiedRatio = occupiedPositions.length / slotCount;
  const gapScale = minimumGap <= 1 ? 0.62 : minimumGap <= 2 ? 0.74 : minimumGap <= 3 ? 0.86 : 1;
  const ratioScale = occupiedRatio >= 0.5 ? 0.82 : occupiedRatio >= 0.35 ? 0.9 : 1;
  const densityScale = Math.min(gapScale, ratioScale);

  return Math.max(24, Math.round(basePlateSizePx * densityScale));
}

function getSlotId(slot: BeltSlotSnapshotDto, fallbackIndex: number): string {
  return slot.slotId ?? `slot-${fallbackIndex}`;
}

function getSeatId(seat: SeatStateDto, fallbackIndex: number): string {
  return seat.seatId ?? `seat-${fallbackIndex}`;
}

function getTierClass(plate: PlateSnapshotDto | null): string {
  switch (plate?.tier) {
    case 'GREEN':
      return 'tier-green';
    case 'RED':
      return 'tier-red';
    case 'GOLD':
      return 'tier-gold';
    case 'BLACK':
      return 'tier-black';
    default:
      return 'tier-empty';
  }
}

function getPlateLabel(plate: PlateSnapshotDto | null): string {
  if (!plate) {
    return 'Empty slot';
  }

  const price =
    plate.priceAtCreation?.amount != null ? `${plate.priceAtCreation.amount} Yen` : 'unknown price';
  return `${plate.menuItemName ?? 'Plate'} on ${plate.tier?.toLowerCase() ?? 'unknown'} tier, ${price}`;
}

function buildPlateViewModel(plate: PlateSnapshotDto, index: number): BeltStagePlateViewModel {
  const visual = resolveMenuItemVisual(plate.menuItemName);
  const tierClass = getTierClass(plate);

  return {
    id: plate.plateId ?? `plate-${index}`,
    menuItemName: plate.menuItemName ?? 'Chef special',
    tierClass,
    className: `${tierClass} plate--${visual.family} plate--${visual.vesselType}`,
    foodClassName: `food--${visual.family} visual--${visual.visualKey} ${visual.accentClass}`,
    visual,
    ariaLabel: getPlateLabel(plate),
  };
}

export function buildBeltStageViewModel(
  snapshot: BeltSnapshotDto,
  seats: SeatStateListDto,
  renderOffset = 0,
): BeltStageViewModel {
  const slotCount = Math.max(1, snapshot.beltSlotCount ?? snapshot.slots?.length ?? 1);
  const occupiedPlateCount = snapshot.slots?.filter((slot) => !!slot.plate).length ?? 0;
  const sizing = getStageSizing(slotCount, seats.length);
  const sortedSlots = [...(snapshot.slots ?? [])].sort(
    (left, right) => (left.positionIndex ?? 0) - (right.positionIndex ?? 0),
  );
  const sortedSeats = [...seats].sort(
    (left, right) => (left.positionIndex ?? 0) - (right.positionIndex ?? 0),
  );
  return {
    beltName: snapshot.beltName ?? 'Sushi belt',
    slotCount,
    occupiedPlateCount,
    kitchen: {
      showChef: true,
      chefLabel: 'Chef preparing dishes inside the counter',
      accentLabels: ['Prep board', 'Tea lamp', 'Serving trays'],
    },
    plateSizePx: getOccupiedPlateSizePx(sizing.plateSizePx, slotCount, sortedSlots),
    slotMarkerSizePx: sizing.slotMarkerSizePx,
    seatSizePx: sizing.seatSizePx,
    slots: sortedSlots.map((slot, index) => {
      const positionIndex = slot.positionIndex ?? index;
      const { tangentDeg, xPercent, yPercent, segment } = getSlotLayoutPoint(
        positionIndex,
        slotCount,
        renderOffset,
      );
      const plate = slot.plate ? buildPlateViewModel(slot.plate, index) : null;

      return {
        id: getSlotId(slot, index),
        positionIndex,
        pathProgress:
          ((((positionIndex + renderOffset) % slotCount) + slotCount) % slotCount) / slotCount,
        xPercent,
        yPercent,
        tangentDeg,
        segment,
        plate,
        ariaLabel: `Slot ${positionIndex + 1}. ${plate?.ariaLabel ?? 'Empty slot'}`,
      };
    }),
    seats: sortedSeats.map((seat, index) => {
      const positionIndex = seat.positionIndex ?? index;
      const { xPercent, yPercent, facingDeg } = getCounterSeatPoint(index, sortedSeats.length);

      return {
        id: getSeatId(seat, index),
        label: seat.label ?? `Seat ${index + 1}`,
        positionIndex,
        xPercent,
        yPercent,
        facingDeg,
        isOccupied: !!seat.isOccupied,
        presenceCue: seat.isOccupied ? 'occupied' : 'available',
        ariaLabel: `${seat.label ?? `Seat ${index + 1}`} is ${seat.isOccupied ? 'occupied' : 'available'}`,
      };
    }),
  };
}
