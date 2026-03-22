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
    selectedSeatId: 'seat-1',
    reachArea: {
      seatId: 'seat-1',
      xPercent: 25,
      yPercent: 70,
      radiusPercent: 12,
      ariaLabel: 'Seat 1 pickup reach',
    },
    slots: [
      {
        id: 'slot-1',
        positionIndex: 0,
        pathProgress: 0,
        xPercent: 20,
        yPercent: 20,
        tangentDeg: 0,
        segment: 'top-straight',
        isWithinReach: true,
        ariaLabel: 'Slot 1. Salmon on red tier, pickable now.',
        plate: {
          id: 'plate-1',
          menuItemName: 'Salmon',
          tierClass: 'tier-red',
          className: 'tier-red plate--nigiri plate--plate',
          foodClassName: 'food--nigiri visual--salmon-nigiri accent-red',
          visual: {
            family: 'nigiri',
            vesselType: 'plate',
            visualKey: 'salmon-nigiri',
            accentClass: 'accent-red',
            overrideName: null,
            fallbackReason: null,
          },
          ariaLabel: 'Salmon on red tier, pickable now.',
          isPickable: true,
          isPendingPick: false,
          isRejected: false,
        },
      },
    ],
    seats: [
      {
        id: 'seat-1',
        label: 'Seat 1',
        positionIndex: 0,
        xPercent: 25,
        yPercent: 70,
        facingDeg: 0,
        isOccupied: false,
        isPending: false,
        isSelected: true,
        statusLabel: 'Available',
        orderId: null,
        occupiedSince: null,
        presenceCue: 'available',
        ariaLabel: 'Seat 1. Currently selected. Available. Activate to select this seat.',
      },
      {
        id: 'seat-2',
        label: 'Seat 2',
        positionIndex: 1,
        xPercent: 60,
        yPercent: 72,
        facingDeg: 0,
        isOccupied: true,
        isPending: false,
        isSelected: false,
        statusLabel: 'Occupied',
        orderId: 'order-2',
        occupiedSince: '2026-03-15T03:00:00Z',
        presenceCue: 'occupied',
        ariaLabel: 'Seat 2. Occupied. Active order order-2. Activate to select this seat.',
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
      selectedSeatDetail: () => ({
        seatId: 'seat-1',
        seatLabel: 'Seat 1',
        statusLabel: 'Available',
        helperLabel: 'Seat clicks only change selection. Start dining here when you are ready.',
        isOccupied: false,
        canStartDining: true,
        canCheckout: false,
        canPickPlates: false,
        pendingAction: null,
        orderSummary: null,
        feedbackTone: null,
        feedbackTitle: null,
        feedbackDetail: null,
      }),
      selectSeat: vi.fn(),
      startDiningForSelectedSeat: vi.fn(),
      checkoutSelectedSeat: vi.fn(),
      pickPlate: vi.fn(),
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

  it('routes seat clicks from the stage to selected-seat browsing', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('.seat--actionable');
    button.click();

    expect(storeMock.selectSeat).toHaveBeenCalledWith('seat-1');
  });

  it('routes plate picks from the stage to the pick action', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('.plate-hitbox');
    button.click();

    expect(storeMock.pickPlate).toHaveBeenCalledWith('plate-1');
  });

  it('renders selected-seat detail feedback when the store exposes a notice', () => {
    storeMock = {
      ...storeMock,
      selectedSeatDetail: () => ({
        seatId: 'seat-1',
        seatLabel: 'Seat 1',
        statusLabel: 'Available',
        helperLabel: 'Seat clicks only change selection. Start dining here when you are ready.',
        isOccupied: false,
        canStartDining: true,
        canCheckout: false,
        canPickPlates: false,
        pendingAction: null,
        orderSummary: null,
        feedbackTone: 'error',
        feedbackTitle: 'Seat 1 was already taken',
        feedbackDetail: 'Another guest occupied this seat first.',
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

        expect(compiled.querySelector('.selected-seat-detail__feedback--error')).toBeTruthy();
        expect(compiled.textContent).toContain('Seat 1 was already taken');
        expect(compiled.textContent).toContain('Another guest occupied this seat first.');
      });
  });

  it('renders the running order inside the selected-seat detail area', async () => {
    storeMock = {
      ...storeMock,
      selectedSeatDetail: () => ({
        seatId: 'seat-2',
        seatLabel: 'Seat 2',
        statusLabel: 'Occupied',
        helperLabel:
          'Pick plates from the highlighted reach area, or check out when the order is complete.',
        isOccupied: true,
        canStartDining: false,
        canCheckout: true,
        canPickPlates: true,
        pendingAction: null,
        orderSummary: {
          orderId: 'order-2',
          seatId: 'seat-2',
          status: 'OPEN',
          createdAt: '2026-03-15T03:00:00Z',
          lines: [],
          totalPrice: 0,
        },
        feedbackTone: null,
        feedbackTitle: null,
        feedbackDetail: null,
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

    expect(compiled.textContent).toContain('Seat 2');
    expect(compiled.textContent).toContain('Order order-2');
    expect(compiled.textContent).toContain('OPEN');
    expect(compiled.textContent).toContain('No plates picked yet.');
    expect(compiled.textContent).toContain('0 Yen');
  });
});
