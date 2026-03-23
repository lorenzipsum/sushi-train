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
      slotLightingRadiusPercent: 15.41,
      highlightWidthPercent: 32.64,
      highlightHeightPercent: 23.04,
      rotationDeg: 90,
      seatSegment: 'bottom-row',
      ownershipLabel: 'Seat 1 currently owns the lit pickup reach.',
      ariaLabel: 'Seat 1 pickup reach. Plates inside this ring can be picked now.',
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
        isHighlightedByReach: true,
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
        restorationStatus: null,
        orderId: null,
        occupiedSince: null,
        presenceCue: 'available',
        secondaryLabel: 'Tap to start',
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
        restorationStatus: 'confirmed-open-order',
        orderId: 'order-2',
        occupiedSince: '2026-03-15T03:00:00Z',
        presenceCue: 'occupied',
        secondaryLabel: 'Dining now',
        ariaLabel: 'Seat 2. Occupied. Active order order-2. Activate to select this seat.',
      },
    ],
    kitchen: {
      showChef: true,
      chefLabel: 'Chef preparing dishes',
      chefSecondaryLabel: 'Knife skills: precise. Ego: respectfully medium.',
      accentLabels: ['Prep board', 'Tea lamp', 'Serving trays'],
      signLabel: 'House special: tiny drama, stable workflows',
      operatorEntryLabel: 'Add plates to the belt',
      operatorSecondaryLabel: 'Backstage hatch for plate logistics',
    },
    presentation: {
      layoutVariant: 'current-balanced',
      ornamentDensity: 'full',
      seatLabelMode: 'full',
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
      speedLabel: () => '1/500ms',
      occupiedPlateCount: () => 1,
      occupiedSeatCount: () => 0,
      occupyFeedback: () => null,
      occupyPendingLabel: () => null,
      checkoutFeedback: () => null,
      checkoutPendingLabel: () => null,
      operatorPlacement: () => ({
        isOpen: false,
        presentationMode: 'inline-kitchen',
        secondaryLabel:
          'Kitchen hatch controls with enough ceremony to feel fun, not enough to slow you down.',
        isMenuLoading: false,
        menuLoadError: null,
        isSubmitting: false,
        notice: null,
        query: '',
        totalMenuCount: 2,
        resultsSummaryLabel: '0 of 2 menu items currently match the search',
        filteredMenuItems: [],
        selectedMenuItemId: null,
        selectedMenuItemLabel: null,
        selectedMenuItemTier: null,
        draft: {
          menuItemId: null,
          numOfPlates: 1,
          tierSnapshot: null,
          priceAtCreation: '',
          expiresAt: '2026-03-22T11:00',
          isDefaultDraft: false,
        },
        canSubmit: false,
        submitDisabledReason: 'Choose a menu item before placing plates.',
        submitSecondaryHint:
          'Primary guidance stays literal so the operator flow never turns into a guessing game.',
      }),
      beltSpeedDialog: () => null,
      beltSpeedFeedback: () => null,
      selectedSeatDetail: () => ({
        seatId: 'seat-1',
        seatLabel: 'Seat 1',
        restorationStatus: 'available',
        statusLabel: 'Available',
        helperLabel: null,
        secondaryLabel: null,
        isOccupied: false,
        canStartDining: true,
        canCheckout: false,
        canPickPlates: false,
        blockedReason: null,
        isCheckoutSummary: false,
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
      toggleOperatorPlacement: vi.fn(),
      openBeltSpeedDialog: vi.fn(),
      closeBeltSpeedDialog: vi.fn(),
      selectBeltSpeed: vi.fn(),
      stepBeltSpeedSelection: vi.fn(),
      nudgeBeltSpeed: vi.fn(),
      submitBeltSpeedDialog: vi.fn(),
      closeOperatorPlacement: vi.fn(),
      retryOperatorMenuLoad: vi.fn(),
      setOperatorSearchQuery: vi.fn(),
      selectOperatorMenuItem: vi.fn(),
      updateOperatorDraft: vi.fn(),
      submitOperatorPlacement: vi.fn(),
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
    expect(compiled.querySelector('.hero__subtitle')?.textContent).toContain(
      'Sushi Train Simulator, grab a seat and eat',
    );
    expect(compiled.querySelector('.counter-stage')).toBeTruthy();
    expect(compiled.querySelector('.counter-stage__guide-button')).toBeTruthy();
    expect(compiled.querySelector('.counter-stage__active-seat-hint')).toBeNull();
    expect(compiled.textContent).toContain('Belt speed');
    expect(compiled.textContent).not.toContain('Warm cafe staging, unchanged service logic');
  });

  it('opens and closes the centralized counter notes dialog', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const openButton = compiled.querySelector('.counter-stage__guide-button') as HTMLButtonElement;

    expect(compiled.querySelector('.guide-dialog')).toBeNull();

    openButton.click();
    fixture.detectChanges();

    expect(compiled.querySelector('.guide-dialog')).toBeTruthy();
    expect(compiled.textContent).toContain('How this sushi bar behaves');
    expect(compiled.textContent).toContain(
      'One place for the demo rules, so the stage can breathe a little.',
    );

    const closeButton = compiled.querySelector('.guide-dialog__close') as HTMLButtonElement;
    closeButton.click();
    fixture.detectChanges();

    expect(compiled.querySelector('.guide-dialog')).toBeNull();
  });

  it('renders the loading state with literal primary copy and playful secondary copy', async () => {
    storeMock = {
      ...storeMock,
      stageViewModel: () => null,
      isLoading: () => true,
    } as unknown as BeltVisualizationStore;

    TestBed.resetTestingModule();

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [{ provide: BeltVisualizationStore, useValue: storeMock }],
    }).compileComponents();

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('.state-card--loading')).toBeTruthy();
    expect(compiled.textContent).toContain('Setting the belt in motion');
    expect(compiled.textContent).toContain(
      'Tiny lanterns are imaginary. The loading state is unfortunately real.',
    );
  });

  it('renders the fatal state with an authored error card', async () => {
    storeMock = {
      ...storeMock,
      stageViewModel: () => null,
      fatalMessage: () => 'Backend unavailable',
    } as unknown as BeltVisualizationStore;

    TestBed.resetTestingModule();

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [{ provide: BeltVisualizationStore, useValue: storeMock }],
    }).compileComponents();

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('.state-card--error')).toBeTruthy();
    expect(compiled.textContent).toContain('We could not set the sushi belt table');
    expect(compiled.textContent).toContain('Backend unavailable');
    expect(compiled.textContent).toContain(
      'The rice is innocent. The data path is the one being dramatic.',
    );
  });

  it('keeps the primary seat and kitchen controls visible without filler seat copy', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('.belt-stage__kitchen-action')).toBeTruthy();
    expect(compiled.textContent).toContain('Start dining');
    expect(compiled.textContent).not.toContain('The rice will not judge you');
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

  it('routes the kitchen plates tile to the placement toggle', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('.belt-stage__kitchen-action');
    button.click();

    expect(storeMock.toggleOperatorPlacement).toHaveBeenCalledTimes(1);
  });

  it('routes the belt speed settings tile to the speed dialog action', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.belt-stage__kitchen-action');
    const settingsButton = buttons[1] as HTMLButtonElement;
    settingsButton.click();

    expect(storeMock.openBeltSpeedDialog).toHaveBeenCalledTimes(1);
  });

  it('nudges the live belt speed from global + and - key presses when the dialog is closed', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    window.dispatchEvent(new KeyboardEvent('keydown', { key: '+' }));
    window.dispatchEvent(new KeyboardEvent('keydown', { key: '-' }));

    expect(storeMock.nudgeBeltSpeed).toHaveBeenNthCalledWith(1, 1);
    expect(storeMock.nudgeBeltSpeed).toHaveBeenNthCalledWith(2, -1);
  });

  it('renders plates as ring-only visuals', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.querySelector('.plate__rim')).toBeTruthy();
    expect(compiled.querySelector('.plate__center')).toBeNull();
    expect(compiled.querySelector('.food')).toBeNull();
  });

  it('renders the speed dialog and routes select, dismiss, and submit interactions', async () => {
    storeMock = {
      ...storeMock,
      beltSpeedDialog: () => ({
        isOpen: true,
        options: [
          { value: 1, label: 'Slow glide', detail: '1 slot per 500ms' },
          { value: 2, label: 'Counter pace', detail: '2 slots per 500ms' },
        ],
        currentSpeed: 1,
        selectedSpeed: 1,
        currentSpeedLabel: 'Slow glide (1 slot per 500ms)',
        selectedSpeedLabel: 'Slow glide',
        helperLabel: '',
        canSubmit: true,
        submitDisabledReason: null,
        isSubmitting: false,
        feedback: null,
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
    const select = compiled.querySelector('#belt-speed-select') as HTMLSelectElement;

    expect(compiled.querySelector('.belt-speed-dialog__label')).toBeNull();
    expect(select.selectedIndex).toBe(0);
    expect((select.options[0] as HTMLOptionElement).selected).toBe(true);

    const dialogCard = compiled.querySelector('.belt-speed-dialog__card') as HTMLElement;
    dialogCard.dispatchEvent(new KeyboardEvent('keydown', { key: '+' }));
    dialogCard.dispatchEvent(new KeyboardEvent('keydown', { key: '-' }));
    fixture.detectChanges();

    expect(storeMock.stepBeltSpeedSelection).toHaveBeenNthCalledWith(1, 1);
    expect(storeMock.stepBeltSpeedSelection).toHaveBeenNthCalledWith(2, -1);
    expect(storeMock.nudgeBeltSpeed).not.toHaveBeenCalled();

    select.value = '2';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();

    expect(compiled.querySelector('.belt-speed-dialog')).toBeTruthy();
    expect(compiled.textContent).toContain('Current belt speed: Slow glide (1 slot per 500ms)');
    expect((select.options[0] as HTMLOptionElement).text).toContain('Slow glide - 1 slot per 500ms');
    expect(compiled.textContent).not.toContain('Slow glide. 1 slot per 500ms');
    expect(select.value).toBe('2');
    expect(storeMock.selectBeltSpeed).toHaveBeenCalledWith(2);
    dialogCard.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }));
    fixture.detectChanges();
    expect(storeMock.closeBeltSpeedDialog).toHaveBeenCalledTimes(1);

    const applyButton = compiled.querySelector(
      '.belt-speed-dialog__action--primary',
    ) as HTMLButtonElement;
    applyButton.click();

    expect(storeMock.submitBeltSpeedDialog).toHaveBeenCalledTimes(1);
  });

  it('renders selected-seat detail feedback when the store exposes a notice', () => {
    storeMock = {
      ...storeMock,
      selectedSeatDetail: () => ({
        seatId: 'seat-1',
        seatLabel: 'Seat 1',
        restorationStatus: 'available',
        statusLabel: 'Available',
        helperLabel: null,
        secondaryLabel: null,
        isOccupied: false,
        canStartDining: true,
        canCheckout: false,
        canPickPlates: false,
        blockedReason: null,
        isCheckoutSummary: false,
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
        restorationStatus: 'occupied',
        statusLabel: 'Occupied',
        helperLabel: null,
        secondaryLabel: null,
        isOccupied: true,
        canStartDining: false,
        canCheckout: true,
        canPickPlates: true,
        blockedReason: null,
        isCheckoutSummary: false,
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

  it('renders syncing-specific selected-seat detail messaging without changing the shell layout', async () => {
    storeMock = {
      ...storeMock,
      selectedSeatDetail: () => ({
        seatId: 'seat-1',
        seatLabel: 'Seat 1',
        restorationStatus: 'syncing',
        statusLabel: 'Syncing dining state',
        helperLabel:
          'Dining state is loading from the backend. Reach cues may stay visible, but picks remain blocked until sync completes.',
        secondaryLabel: 'The reach ring may linger, but the backend still has the final word.',
        isOccupied: true,
        canStartDining: false,
        canCheckout: true,
        canPickPlates: false,
        blockedReason: 'syncing',
        isCheckoutSummary: false,
        pendingAction: null,
        orderSummary: null,
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

    expect(compiled.querySelector('.counter-stage')).toBeTruthy();
    expect(compiled.querySelector('.selected-seat-detail__status--syncing')).toBeTruthy();
    expect(compiled.textContent).toContain('Syncing dining state');
    expect(compiled.textContent).toContain('picks remain blocked until sync completes');
  });

  it('renders the final checkout summary inside the selected-seat detail area', async () => {
    storeMock = {
      ...storeMock,
      selectedSeatDetail: () => ({
        seatId: 'seat-1',
        seatLabel: 'Seat 1',
        restorationStatus: 'checked-out',
        statusLabel: 'Checked out',
        helperLabel: null,
        secondaryLabel: null,
        isOccupied: false,
        canStartDining: true,
        canCheckout: false,
        canPickPlates: false,
        blockedReason: null,
        isCheckoutSummary: true,
        pendingAction: null,
        orderSummary: {
          orderId: 'order-1',
          seatId: 'seat-1',
          status: 'CHECKED_OUT',
          createdAt: '2026-03-15T03:00:00Z',
          closedAt: '2026-03-15T03:42:00Z',
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

    expect(compiled.querySelector('.selected-seat-detail__status--checked-out')).toBeTruthy();
    expect(compiled.textContent).toContain('Order total: 0 Yen');
    expect(compiled.textContent).not.toContain('respectful encore');
    expect(compiled.textContent).not.toContain('final backend summary remains visible');
  });
});
