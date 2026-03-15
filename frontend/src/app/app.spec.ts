import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { App } from './app';
import { BeltVisualizationStore } from './belt-visualization/belt-visualization.store';
import type { BeltStageViewModel } from './belt-visualization/belt-view-model';

function createStageViewModel(): BeltStageViewModel {
  return {
    beltName: 'Main Belt',
    slotCount: 8,
    occupiedPlateCount: 1,
    slots: [],
    seats: [
      {
        id: 'seat-1',
        label: 'Seat 1',
        positionIndex: 0,
        xPercent: 25,
        yPercent: 70,
        facingDeg: 0,
        isOccupied: false,
        isActionable: true,
        isPending: false,
        seatAction: 'occupy',
        orderId: null,
        occupiedSince: null,
        presenceCue: 'available',
        ariaLabel: 'Seat 1 is available. Activate to occupy this seat.',
      },
      {
        id: 'seat-2',
        label: 'Seat 2',
        positionIndex: 1,
        xPercent: 60,
        yPercent: 72,
        facingDeg: 0,
        isOccupied: true,
        isActionable: true,
        isPending: false,
        seatAction: 'checkout',
        orderId: 'order-2',
        occupiedSince: '2026-03-15T03:00:00Z',
        presenceCue: 'occupied',
        ariaLabel: 'Seat 2 is occupied. Active order order-2. Activate to check out this seat.',
      },
    ],
    kitchen: {
      showChef: true,
      chefLabel: 'Chef preparing dishes',
      accentLabels: ['Prep board', 'Tea lamp', 'Serving trays'],
    },
    plateSizePx: 28,
    slotMarkerSizePx: 10,
    seatSizePx: 34,
  };
}

describe('App', () => {
  let storeMock: BeltVisualizationStore;

  beforeEach(async () => {
    storeMock = {
      fatalMessage: () => null,
      hasNoBelts: () => false,
      isLoading: () => false,
      stageViewModel: () => createStageViewModel(),
      isReducedMotion: () => false,
      isPaused: () => false,
      isDegraded: () => false,
      speedLabel: () => '1 slot every 500ms',
      occupiedPlateCount: () => 1,
      occupiedSeatCount: () => 0,
      occupyFeedback: () => null,
      occupyPendingLabel: () => null,
      checkoutFeedback: () => null,
      checkoutPendingLabel: () => null,
      occupySeat: vi.fn(),
      checkoutSeat: vi.fn(),
    } as unknown as BeltVisualizationStore;

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [{ provide: BeltVisualizationStore, useValue: storeMock }],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('renders the simplified title-led stage shell', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('h1')?.textContent).toContain('Kaitenzushi');
    expect(compiled.querySelector('.counter-stage')).toBeTruthy();
    expect(compiled.querySelector('.support-panels')).toBeFalsy();
    expect(compiled.textContent).toContain('Service pace');
    expect(compiled.textContent).not.toContain('Guest seats');
  });

  it('routes a free-seat click from the stage to the occupy action', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('.seat--actionable');
    button.click();

    expect(storeMock.occupySeat).toHaveBeenCalledWith('seat-1');
  });

  it('routes an occupied-seat click from the stage to the checkout action', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('.seat--checkout');
    button.click();

    expect(storeMock.checkoutSeat).toHaveBeenCalledWith('seat-2');
  });

  it('renders occupy feedback when the store exposes a conflict or success notice', () => {
    storeMock = {
      ...storeMock,
      occupyFeedback: () => ({
        tone: 'error',
        title: 'Seat 1 was already taken',
        detail: 'Another guest occupied this seat first.',
        seatId: 'seat-1',
        seatLabel: 'Seat 1',
        orderId: 'order-1',
        createdAtLabel: 'Mar 15, 3:00 AM',
      }),
    } as unknown as BeltVisualizationStore;

    TestBed.resetTestingModule();

    return TestBed.configureTestingModule({
      imports: [App],
      providers: [{ provide: BeltVisualizationStore, useValue: storeMock }],
    })
      .compileComponents()
      .then(() => {
        const fixture = TestBed.createComponent(App);
        fixture.detectChanges();
        const compiled = fixture.nativeElement as HTMLElement;

        expect(compiled.querySelector('.counter-stage__feedback--error')).toBeTruthy();
        expect(compiled.textContent).toContain('Seat 1 was already taken');
        expect(compiled.textContent).toContain('Open order order-1');
      });
  });

  it('renders checkout feedback with final summary details and empty-order totals', async () => {
    storeMock = {
      ...storeMock,
      checkoutFeedback: () => ({
        tone: 'success',
        title: 'Seat 2 is checked out',
        detail:
          'Checkout is complete. No plates were recorded for this order, and the seat is free again.',
        seatId: 'seat-2',
        seatLabel: 'Seat 2',
        finalSummary: {
          seatId: 'seat-2',
          label: 'Seat 2',
          positionIndex: 1,
          isOccupied: false,
          orderSummary: {
            orderId: 'order-2',
            seatId: 'seat-2',
            status: 'CHECKED_OUT',
            createdAt: '2026-03-15T03:00:00Z',
            closedAt: '2026-03-15T03:42:00Z',
            lines: [],
            totalPrice: 0,
          },
        },
        statusLabel: 'CHECKED_OUT',
        createdAtLabel: 'Mar 15, 3:00 AM',
        closedAtLabel: 'Mar 15, 3:42 AM',
        totalPriceLabel: '0 Yen',
      }),
    } as unknown as BeltVisualizationStore;

    TestBed.resetTestingModule();

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [{ provide: BeltVisualizationStore, useValue: storeMock }],
    }).compileComponents();

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('Seat 2 is checked out');
    expect(compiled.textContent).toContain('Checked out order order-2');
    expect(compiled.textContent).toContain('Status CHECKED_OUT');
    expect(compiled.textContent).toContain('No plates were recorded for this order.');
    expect(compiled.textContent).toContain('Total 0 Yen');
  });
});
