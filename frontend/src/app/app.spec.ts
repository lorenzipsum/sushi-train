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
        orderId: null,
        occupiedSince: null,
        presenceCue: 'available',
        ariaLabel: 'Seat 1 is available. Activate to occupy this seat.',
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
      occupySeat: vi.fn(),
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
});
