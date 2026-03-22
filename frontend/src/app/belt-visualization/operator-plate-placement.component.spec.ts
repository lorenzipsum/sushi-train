import { TestBed } from '@angular/core/testing';
import { describe, expect, it, vi } from 'vitest';

import type { OperatorPlacementViewModel } from '../api/types';
import { OperatorPlatePlacementComponent } from './operator-plate-placement.component';

function createPlacement(
  overrides: Partial<OperatorPlacementViewModel> = {},
): OperatorPlacementViewModel {
  return {
    isOpen: true,
    presentationMode: 'secondary-surface',
    isMenuLoading: false,
    menuLoadError: null,
    isSubmitting: false,
    notice: null,
    query: '',
    totalMenuCount: 2,
    filteredMenuItems: [
      {
        id: 'menu-1',
        name: 'Salmon Nigiri',
        defaultTier: 'RED',
        basePrice: 320,
        createdAt: '2026-03-22T09:00:00Z',
      },
      {
        id: 'menu-2',
        name: 'Tamago Nigiri',
        defaultTier: 'GREEN',
        basePrice: 180,
        createdAt: '2026-03-22T09:00:00Z',
      },
    ],
    selectedMenuItemId: 'menu-1',
    selectedMenuItemLabel: 'Salmon Nigiri',
    selectedMenuItemTier: 'RED',
    draft: {
      menuItemId: 'menu-1',
      numOfPlates: 1,
      tierSnapshot: 'RED',
      priceAtCreation: '320',
      expiresAt: '2026-03-22T11:00',
      isDefaultDraft: true,
    },
    canSubmit: true,
    submitDisabledReason: null,
    ...overrides,
  };
}

describe('OperatorPlatePlacementComponent', () => {
  it('emits menu selection and submit actions', () => {
    TestBed.configureTestingModule({ imports: [OperatorPlatePlacementComponent] });
    const fixture = TestBed.createComponent(OperatorPlatePlacementComponent);
    fixture.componentRef.setInput('placement', createPlacement());
    const selectSpy = vi.fn();
    const submitSpy = vi.fn();
    fixture.componentInstance.selectMenuItem.subscribe(selectSpy);
    fixture.componentInstance.submitPlacement.subscribe(submitSpy);
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.operator-placement__result');
    buttons[1].click();
    fixture.nativeElement.querySelector('.operator-placement__submit').click();

    expect(selectSpy).toHaveBeenCalledWith('menu-2');
    expect(submitSpy).toHaveBeenCalledTimes(1);
  });

  it('disables submit while placement is pending', () => {
    TestBed.configureTestingModule({ imports: [OperatorPlatePlacementComponent] });
    const fixture = TestBed.createComponent(OperatorPlatePlacementComponent);
    fixture.componentRef.setInput(
      'placement',
      createPlacement({ isSubmitting: true, canSubmit: false }),
    );
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.operator-placement__submit').disabled).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Placing plates...');
  });

  it('exposes selection state on the menu item buttons', () => {
    TestBed.configureTestingModule({ imports: [OperatorPlatePlacementComponent] });
    const fixture = TestBed.createComponent(OperatorPlatePlacementComponent);
    fixture.componentRef.setInput('placement', createPlacement());
    fixture.detectChanges();

    const results = fixture.nativeElement.querySelector('.operator-placement__results');
    const buttons = fixture.nativeElement.querySelectorAll('.operator-placement__result');

    expect(results.getAttribute('role')).toBeNull();
    expect(buttons[0].getAttribute('aria-pressed')).toBe('true');
    expect(buttons[1].getAttribute('aria-pressed')).toBe('false');
  });
});
