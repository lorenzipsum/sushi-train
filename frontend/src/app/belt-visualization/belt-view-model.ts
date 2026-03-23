import type {
  BeltSnapshotDto,
  BeltSlotSnapshotDto,
  OrderSummaryDto,
  PlateSnapshotDto,
  SeatRestorationState,
  SeatRestorationStatus,
  SeatPendingAction,
  SeatStateDto,
  SeatStateListDto,
} from '../api/types';
import {
  getCounterSeatPoint,
  getStageReadabilityProfile,
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
  isPickable: boolean;
  isPendingPick: boolean;
  isRejected: boolean;
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
  isWithinReach: boolean;
  isHighlightedByReach: boolean;
}

export interface BeltStageSeatViewModel {
  id: string;
  label: string;
  positionIndex: number;
  xPercent: number;
  yPercent: number;
  facingDeg: number;
  isOccupied: boolean;
  isPending: boolean;
  isSelected: boolean;
  statusLabel: string;
  restorationStatus: SeatRestorationStatus | null;
  orderId: string | null;
  occupiedSince: string | null;
  presenceCue: 'available' | 'occupied' | 'pending';
  secondaryLabel: string | null;
  ariaLabel: string;
}

export interface BeltStageLegendViewModel {
  id: string;
  primaryLabel: string;
  secondaryLabel: string;
  tone: 'warm' | 'reach' | 'operator';
}

export interface BeltStageReachAreaViewModel {
  seatId: string;
  xPercent: number;
  yPercent: number;
  radiusPercent: number;
  slotLightingRadiusPercent: number;
  highlightWidthPercent: number;
  highlightHeightPercent: number;
  rotationDeg: number;
  seatSegment: 'top-row' | 'side-row' | 'bottom-row';
  ownershipLabel: string;
  ariaLabel: string;
}

export interface BeltStageKitchenViewModel {
  showChef: boolean;
  chefLabel: string;
  chefSecondaryLabel: string;
  accentLabels: [string, string, string];
  signLabel: string;
  operatorEntryLabel: string;
  operatorSecondaryLabel: string;
}

export interface BeltStagePresentationViewModel {
  layoutVariant: 'current-balanced';
  ornamentDensity: 'full' | 'trimmed';
  seatLabelMode: 'full' | 'compact';
  primaryLabel: string;
  secondaryLabel: string;
  legends: BeltStageLegendViewModel[];
}

export interface BeltStageViewModel {
  beltName: string;
  slotCount: number;
  occupiedPlateCount: number;
  slots: BeltStageSlotViewModel[];
  seats: BeltStageSeatViewModel[];
  kitchen: BeltStageKitchenViewModel;
  presentation: BeltStagePresentationViewModel;
  plateSizePx: number;
  slotMarkerSizePx: number;
  seatSizePx: number;
  selectedSeatId: string | null;
  reachArea: BeltStageReachAreaViewModel | null;
}

export interface BuildBeltStageViewModelOptions {
  pendingSeatId?: string | null;
  pendingAction?: SeatPendingAction;
  pendingPlateId?: string | null;
  rejectedPlateId?: string | null;
  activeOrdersBySeatId?: Record<string, OrderSummaryDto | undefined>;
  restorationBySeatId?: Record<string, SeatRestorationState | undefined>;
  selectedSeatId?: string | null;
}

interface SeatLayoutContext {
  seat: SeatStateDto;
  seatId: string;
  point: ReturnType<typeof getCounterSeatPoint>;
  activeOrder?: OrderSummaryDto;
}

function getSeatSegment(point: SeatLayoutContext['point']): 'top-row' | 'side-row' | 'bottom-row' {
  if (point.facingDeg === 180) {
    return 'top-row';
  }

  if (point.facingDeg === 0) {
    return 'bottom-row';
  }

  return 'side-row';
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

function getDistance(
  left: Pick<
    BeltStageSeatViewModel | BeltStageSlotViewModel | BeltStageReachAreaViewModel,
    'xPercent' | 'yPercent'
  >,
  right: Pick<
    BeltStageSeatViewModel | BeltStageSlotViewModel | BeltStageReachAreaViewModel,
    'xPercent' | 'yPercent'
  >,
): number {
  return Math.hypot(left.xPercent - right.xPercent, left.yPercent - right.yPercent);
}

function getPlateLabel(plate: PlateSnapshotDto | null): string {
  if (!plate) {
    return 'Empty slot';
  }

  const price =
    plate.priceAtCreation?.amount != null ? `${plate.priceAtCreation.amount} Yen` : 'unknown price';
  return `${plate.menuItemName ?? 'Plate'} on ${plate.tier?.toLowerCase() ?? 'unknown'} tier, ${price}`;
}

function buildPlateViewModel(
  plate: PlateSnapshotDto,
  index: number,
  isPickable: boolean,
  isPendingPick: boolean,
  isRejected: boolean,
): BeltStagePlateViewModel {
  const visual = resolveMenuItemVisual(plate.menuItemName);
  const tierClass = getTierClass(plate);
  const pickabilityLabel = isPickable ? 'Pickable now.' : 'Not pickable right now.';

  return {
    id: plate.plateId ?? `plate-${index}`,
    menuItemName: plate.menuItemName ?? 'Chef special',
    tierClass,
    className: `${tierClass} plate--${visual.family} plate--${visual.vesselType}`,
    foodClassName: `food--${visual.family} visual--${visual.visualKey} ${visual.accentClass}`,
    visual,
    ariaLabel: `${getPlateLabel(plate)} ${pickabilityLabel}`,
    isPickable,
    isPendingPick,
    isRejected,
  };
}

function getReachArea(
  seatContexts: SeatLayoutContext[],
  selectedSeatId: string | null,
): BeltStageReachAreaViewModel | null {
  if (!selectedSeatId) {
    return null;
  }

  const selectedIndex = seatContexts.findIndex((context) => context.seatId === selectedSeatId);
  if (selectedIndex === -1) {
    return null;
  }

  const selectedSeat = seatContexts[selectedIndex];
  const seatSegment = getSeatSegment(selectedSeat.point);
  let baseRadiusPercent = 12;

  if (seatContexts.length > 1) {
    const previousSeat =
      seatContexts[(selectedIndex - 1 + seatContexts.length) % seatContexts.length];
    const nextSeat = seatContexts[(selectedIndex + 1) % seatContexts.length];
    const neighborDistances = [
      getDistance(selectedSeat.point, previousSeat.point),
      getDistance(selectedSeat.point, nextSeat.point),
    ];
    baseRadiusPercent = Math.max(10, Math.min(20, Math.max(...neighborDistances) * 0.58));
  }

  let radiusPercent = baseRadiusPercent;
  const slotLightingRadiusPercent = Math.min(22, Math.max(12.5, baseRadiusPercent * 1.18 + 1.25));
  if (seatSegment === 'top-row') {
    radiusPercent = slotLightingRadiusPercent;
  }

  const seatLabel = selectedSeat.seat.label ?? 'Selected seat';
  const highlightRadiusPercent = baseRadiusPercent;
  const highlightWidthPercent =
    highlightRadiusPercent * (seatSegment === 'top-row' || seatSegment === 'bottom-row' ? 1.14 : 1.36) * 2;
  const highlightHeightPercent =
    (seatSegment === 'top-row' || seatSegment === 'bottom-row' ? 0.72 : 0.96) *
    highlightRadiusPercent *
    2;
  const rotationDeg = seatSegment === 'top-row' || seatSegment === 'bottom-row' ? 90 : 0;

  return {
    seatId: selectedSeatId,
    xPercent: selectedSeat.point.xPercent,
    yPercent: selectedSeat.point.yPercent,
    radiusPercent,
    slotLightingRadiusPercent,
    highlightWidthPercent,
    highlightHeightPercent,
    rotationDeg,
    seatSegment,
    ownershipLabel: `${seatLabel} currently owns the lit pickup reach.`,
    ariaLabel: `${seatLabel} pickup reach. Plates inside this ring can be picked now.`,
  };
}

export function buildBeltStageViewModel(
  snapshot: BeltSnapshotDto,
  seats: SeatStateListDto,
  renderOffset = 0,
  options: BuildBeltStageViewModelOptions = {},
): BeltStageViewModel {
  const slotCount = Math.max(1, snapshot.beltSlotCount ?? snapshot.slots?.length ?? 1);
  const occupiedPlateCount = snapshot.slots?.filter((slot) => !!slot.plate).length ?? 0;
  const sizing = getStageSizing(slotCount, seats.length);
  const readabilityProfile = getStageReadabilityProfile(slotCount, seats.length);
  const sortedSlots = [...(snapshot.slots ?? [])].sort(
    (left, right) => (left.positionIndex ?? 0) - (right.positionIndex ?? 0),
  );
  const sortedSeats = [...seats].sort(
    (left, right) => (left.positionIndex ?? 0) - (right.positionIndex ?? 0),
  );
  const pendingSeatId = options.pendingSeatId ?? null;
  const pendingAction = options.pendingAction ?? null;
  const pendingPlateId = options.pendingPlateId ?? null;
  const rejectedPlateId = options.rejectedPlateId ?? null;
  const activeOrdersBySeatId = options.activeOrdersBySeatId ?? {};
  const restorationBySeatId = options.restorationBySeatId ?? {};

  const seatContexts = sortedSeats.map((seat, index) => ({
    seat,
    seatId: getSeatId(seat, index),
    point: getCounterSeatPoint(index, sortedSeats.length),
    activeOrder: seat.seatId ? activeOrdersBySeatId[seat.seatId] : undefined,
  }));

  const selectedSeatId = options.selectedSeatId ?? seatContexts[0]?.seatId ?? null;
  const reachArea = getReachArea(seatContexts, selectedSeatId);
  const selectedSeatContext =
    seatContexts.find((context) => context.seatId === selectedSeatId) ?? null;
  const selectedSeatRestorationStatus = selectedSeatContext
    ? (restorationBySeatId[selectedSeatContext.seatId]?.restorationStatus ?? null)
    : null;
  const selectedSeatCanPick =
    !!selectedSeatContext?.seat.isOccupied &&
    !!selectedSeatContext.activeOrder?.orderId &&
    selectedSeatRestorationStatus !== 'syncing' &&
    selectedSeatRestorationStatus !== 'unresolved-retrying';

  return {
    beltName: snapshot.beltName ?? 'Sushi belt',
    slotCount,
    occupiedPlateCount,
    selectedSeatId,
    reachArea,
    kitchen: {
      showChef: true,
      chefLabel: 'Chef preparing dishes inside the counter',
      chefSecondaryLabel: 'Knife skills: precise. Ego: respectfully medium.',
      accentLabels: ['Prep board', 'Tea lamp', 'Serving trays'],
      signLabel: 'House special: tiny drama, stable workflows',
      operatorEntryLabel: 'Add plates to the belt',
      operatorSecondaryLabel: 'Backstage hatch for plate logistics',
    },
    presentation: {
      ...readabilityProfile,
      primaryLabel: 'Counter loop overview',
      secondaryLabel: 'Warm cafe energy, same belt logic, zero workflow plot twists.',
      legends: [
        {
          id: 'seats',
          primaryLabel: 'Seats stay literal',
          secondaryLabel: 'Tap a stool to focus the dining panel.',
          tone: 'warm',
        },
        {
          id: 'reach',
          primaryLabel: 'Dashed ring = pickup reach',
          secondaryLabel: 'Only that zone is fair game for plate picking.',
          tone: 'reach',
        },
        {
          id: 'operator',
          primaryLabel: 'Kitchen hatch opens plate controls',
          secondaryLabel: 'Chef chaos is decorative. The form is still strict.',
          tone: 'operator',
        },
      ],
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
      const slotPoint = { xPercent, yPercent };
      const isWithinReach = reachArea
        ? getDistance(slotPoint, reachArea) <= reachArea.radiusPercent
        : false;
      const isHighlightedByReach = reachArea
        ? getDistance(slotPoint, reachArea) <= reachArea.slotLightingRadiusPercent
        : false;
      const isPickable = !!slot.plate && isWithinReach && selectedSeatCanPick && !pendingAction;
      const plate = slot.plate
        ? buildPlateViewModel(
            slot.plate,
            index,
            isPickable,
            slot.plate.plateId === pendingPlateId,
            slot.plate.plateId === rejectedPlateId,
          )
        : null;

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
        isWithinReach,
        isHighlightedByReach,
        ariaLabel: `Slot ${positionIndex + 1}. ${plate?.ariaLabel ?? 'Empty slot'}`,
      };
    }),
    seats: seatContexts.map((context) => {
      const isPending = context.seatId === pendingSeatId;
      const isSelected = context.seatId === selectedSeatId;
      const isOccupied = !!context.seat.isOccupied;
      const restorationStatus = restorationBySeatId[context.seatId]?.restorationStatus ?? null;
      const presenceCue = isPending ? 'pending' : isOccupied ? 'occupied' : 'available';
      const statusLabel = isPending
        ? pendingAction === 'checkout'
          ? 'Checking out'
          : pendingAction === 'occupy'
            ? 'Starting dining'
            : 'Updating'
        : isSelected && restorationStatus === 'unresolved-retrying'
          ? 'Retrying sync'
          : isSelected && restorationStatus === 'syncing'
            ? 'Syncing'
            : isOccupied
              ? 'Occupied'
              : 'Available';
      const ariaParts = [context.seat.label ?? 'Seat'];
      const secondaryLabel = isPending ? statusLabel : isOccupied ? 'Dining now' : 'Tap to start';

      if (isSelected) {
        ariaParts.push('Currently selected.');
      }
      if (isSelected && restorationStatus === 'syncing') {
        ariaParts.push('Dining state is syncing.');
      }
      if (isSelected && restorationStatus === 'unresolved-retrying') {
        ariaParts.push('Dining state is retrying automatically.');
      }
      ariaParts.push(isOccupied ? 'Occupied.' : 'Available.');
      if (context.activeOrder?.orderId) {
        ariaParts.push(`Active order ${context.activeOrder.orderId}.`);
      }
      ariaParts.push('Activate to select this seat.');

      return {
        id: context.seatId,
        label: context.seat.label ?? 'Seat',
        positionIndex: context.seat.positionIndex ?? 0,
        xPercent: context.point.xPercent,
        yPercent: context.point.yPercent,
        facingDeg: context.point.facingDeg,
        isOccupied,
        isPending,
        isSelected,
        statusLabel,
        restorationStatus,
        orderId: context.activeOrder?.orderId ?? null,
        occupiedSince: context.activeOrder?.createdAt ?? null,
        presenceCue,
        secondaryLabel,
        ariaLabel: ariaParts.join(' '),
      };
    }),
  };
}
