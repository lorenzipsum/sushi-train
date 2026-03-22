import type { components } from './generated/openapi.types';

export type BeltDto = components['schemas']['BeltDto'];
export type BeltListDto = BeltDto[];
export type CreatePlateAndPlaceOnBeltRequest =
  components['schemas']['CreatePlateAndPlaceOnBeltRequest'];
export type CreatedPlatesOnBeltResponse = components['schemas']['CreatedPlatesOnBeltResponse'];
export type BeltSlotSnapshotDto = components['schemas']['BeltSlotSnapshotDto'];
export type BeltSnapshotDto = components['schemas']['BeltSnapshotDto'];
export type FullBeltDto = components['schemas']['FullBeltDto'];
export type MenuItemDto = components['schemas']['MenuItemDto'];
export type MenuItemTier = MenuItemDto['defaultTier'];
export type OrderLineDto = components['schemas']['OrderLineDto'];
export type PageMetadata = components['schemas']['PageMetadata'];
export type PagedMenuItemDto = components['schemas']['PagedMenuItemDto'];
export type PagedOrderSummaryDto = components['schemas']['PagedOrderSummaryDto'];
export type PagedPlateDto = components['schemas']['PagedPlateDto'];
export type PlacedPlateDto = components['schemas']['PlacedPlateDto'];
export type OrderSummaryDto = components['schemas']['OrderSummaryDto'];
export type PickPlateRequest = components['schemas']['PickPlateRequest'];
export type PlateSnapshotDto = components['schemas']['PlateSnapshotDto'];
export type PlateDto = components['schemas']['PlateDto'];
export type ProblemDetail = components['schemas']['ProblemDetail'];
export type SeatActionProblemDetail = ProblemDetail & {
  action?: string;
  errorCode?: string;
  seatId?: string;
};
export type SeatOrderDto = components['schemas']['SeatOrderDto'];
export type SeatStateDto = components['schemas']['SeatStateDto'];
export type SeatStateListDto = SeatStateDto[];
export type YenAmount = components['schemas']['YenAmount'];

export type SeatRestorationStatus =
  | 'syncing'
  | 'confirmed-open-order'
  | 'confirmed-no-order'
  | 'unresolved-retrying';

export interface SeatRestorationState {
  seatId: string;
  restorationStatus: SeatRestorationStatus;
  hasRetryInFlight: boolean;
  lastKnownOrderSummary: OrderSummaryDto | null;
  resolutionMessage: string | null;
}

export type SeatPendingAction = 'occupy' | 'checkout' | 'pick-plate' | null;

export type PlatePickOutcomeType =
  | 'success'
  | 'syncing'
  | 'seat-not-occupied'
  | 'out-of-range'
  | 'plate-not-pickable'
  | 'resource-conflict'
  | 'not-found'
  | 'unknown-error';

export type SelectedSeatStatus =
  | 'available'
  | 'occupied'
  | 'syncing'
  | 'unresolved'
  | 'checked-out';

export interface PlatePickFeedback {
  tone: 'success' | 'error';
  title: string;
  detail: string;
  seatId: string | null;
  seatLabel: string | null;
  plateId: string | null;
  outcomeType: PlatePickOutcomeType;
  orderSummary: OrderSummaryDto | null;
  rejectAnimationShown: boolean;
}

export interface SelectedSeatDetailViewModel {
  seatId: string;
  seatLabel: string;
  restorationStatus: SelectedSeatStatus;
  statusLabel: string;
  helperLabel: string;
  isOccupied: boolean;
  canStartDining: boolean;
  canCheckout: boolean;
  canPickPlates: boolean;
  blockedReason: 'syncing' | 'no-open-order' | null;
  isCheckoutSummary: boolean;
  pendingAction: SeatPendingAction;
  orderSummary: OrderSummaryDto | null;
  feedbackTone: 'success' | 'error' | null;
  feedbackTitle: string | null;
  feedbackDetail: string | null;
}

export type OperatorPlacementPresentationMode = 'inline-kitchen' | 'secondary-surface';

export type OperatorPlacementOutcomeType =
  | 'success'
  | 'not-enough-space'
  | 'invalid-menu-item'
  | 'invalid-values'
  | 'missing-belt'
  | 'malformed-request'
  | 'unknown-error';

export interface OperatorPlacementNotice {
  tone: 'success' | 'error';
  title: string;
  detail: string;
  outcomeType: OperatorPlacementOutcomeType;
  createdCount: number | null;
  menuItemName: string | null;
}

export interface OperatorPlacementDraftValue {
  menuItemId: string | null;
  numOfPlates: number;
  tierSnapshot: MenuItemTier | null;
  priceAtCreation: string;
  expiresAt: string;
  isDefaultDraft: boolean;
}

export interface OperatorPlacementDraftPatch {
  numOfPlates?: number;
  tierSnapshot?: MenuItemTier | null;
  priceAtCreation?: string;
  expiresAt?: string;
}

export interface OperatorPlacementViewModel {
  isOpen: boolean;
  presentationMode: OperatorPlacementPresentationMode;
  isMenuLoading: boolean;
  menuLoadError: string | null;
  isSubmitting: boolean;
  notice: OperatorPlacementNotice | null;
  query: string;
  totalMenuCount: number;
  filteredMenuItems: MenuItemDto[];
  selectedMenuItemId: string | null;
  selectedMenuItemLabel: string | null;
  selectedMenuItemTier: MenuItemTier | null;
  draft: OperatorPlacementDraftValue;
  canSubmit: boolean;
  submitDisabledReason: string | null;
}
