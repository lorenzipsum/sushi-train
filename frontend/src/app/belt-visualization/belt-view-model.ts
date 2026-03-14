import type {
  BeltSnapshotDto,
  BeltSlotSnapshotDto,
  PlateSnapshotDto,
  SeatStateDto,
  SeatStateListDto,
} from '../api/types';

export interface BeltStageSlotViewModel {
  id: string;
  angleDeg: number;
  xPercent: number;
  yPercent: number;
  plate: PlateSnapshotDto | null;
  tierClass: string;
  ariaLabel: string;
}

export interface BeltStageSeatViewModel {
  id: string;
  label: string;
  angleDeg: number;
  xPercent: number;
  yPercent: number;
  isOccupied: boolean;
  ariaLabel: string;
}

export interface BeltStageViewModel {
  beltName: string;
  slotCount: number;
  occupiedPlateCount: number;
  slots: BeltStageSlotViewModel[];
  seats: BeltStageSeatViewModel[];
  plateSizePx: number;
  slotMarkerSizePx: number;
  seatSizePx: number;
}

function toPolarPosition(
  index: number,
  slotCount: number,
  radiusPercent: number,
): { xPercent: number; yPercent: number; angleDeg: number } {
  const angleDeg = (index / slotCount) * 360 - 90;
  const angleRad = (angleDeg * Math.PI) / 180;

  return {
    angleDeg,
    xPercent: 50 + Math.cos(angleRad) * radiusPercent,
    yPercent: 50 + Math.sin(angleRad) * radiusPercent,
  };
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

export function buildBeltStageViewModel(
  snapshot: BeltSnapshotDto,
  seats: SeatStateListDto,
): BeltStageViewModel {
  const slotCount = Math.max(1, snapshot.beltSlotCount ?? snapshot.slots?.length ?? 1);
  const occupiedPlateCount = snapshot.slots?.filter((slot) => !!slot.plate).length ?? 0;
  const plateSizePx = Math.max(10, Math.min(18, Math.round(156 / Math.sqrt(slotCount))));
  const slotMarkerSizePx = Math.max(4, Math.min(8, Math.round(72 / Math.sqrt(slotCount))));
  const seatSizePx = Math.max(22, Math.min(34, Math.round(18 + seats.length / 2)));

  return {
    beltName: snapshot.beltName ?? 'Sushi belt',
    slotCount,
    occupiedPlateCount,
    plateSizePx,
    slotMarkerSizePx,
    seatSizePx,
    slots: (snapshot.slots ?? []).map((slot, index) => {
      const positionIndex = slot.positionIndex ?? index;
      const { angleDeg, xPercent, yPercent } = toPolarPosition(positionIndex, slotCount, 39);

      return {
        id: getSlotId(slot, index),
        angleDeg,
        xPercent,
        yPercent,
        plate: slot.plate ?? null,
        tierClass: getTierClass(slot.plate ?? null),
        ariaLabel: `Slot ${positionIndex + 1}. ${getPlateLabel(slot.plate ?? null)}`,
      };
    }),
    seats: seats.map((seat, index) => {
      const positionIndex = seat.positionIndex ?? index;
      const { angleDeg, xPercent, yPercent } = toPolarPosition(positionIndex, slotCount, 49);

      return {
        id: getSeatId(seat, index),
        label: seat.label ?? `Seat ${index + 1}`,
        angleDeg,
        xPercent,
        yPercent,
        isOccupied: !!seat.isOccupied,
        ariaLabel: `${seat.label ?? `Seat ${index + 1}`} is ${seat.isOccupied ? 'occupied' : 'available'}`,
      };
    }),
  };
}
