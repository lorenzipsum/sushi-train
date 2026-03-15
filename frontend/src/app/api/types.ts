import type { components } from './generated/openapi.types';

export type BeltDto = components['schemas']['BeltDto'];
export type BeltListDto = BeltDto[];
export type BeltSlotSnapshotDto = components['schemas']['BeltSlotSnapshotDto'];
export type BeltSnapshotDto = components['schemas']['BeltSnapshotDto'];
export type FullBeltDto = components['schemas']['FullBeltDto'];
export type MenuItemDto = components['schemas']['MenuItemDto'];
export type OrderLineDto = components['schemas']['OrderLineDto'];
export type PageMetadata = components['schemas']['PageMetadata'];
export type PagedMenuItemDto = components['schemas']['PagedMenuItemDto'];
export type PagedOrderSummaryDto = components['schemas']['PagedOrderSummaryDto'];
export type PagedPlateDto = components['schemas']['PagedPlateDto'];
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
