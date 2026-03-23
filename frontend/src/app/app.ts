import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  HostListener,
  computed,
  effect,
  inject,
  signal,
  viewChild,
} from '@angular/core';

import { BeltStageComponent } from './belt-visualization/belt-stage.component';
import { BeltVisualizationStore } from './belt-visualization/belt-visualization.store';
import { OperatorPlatePlacementComponent } from './belt-visualization/operator-plate-placement.component';
import { SelectedSeatDetailComponent } from './belt-visualization/selected-seat-detail.component';

interface HeroPresentation {
  title: string;
  subtitle: string;
}

interface ShellStateCard {
  tone: 'loading' | 'empty' | 'error';
  eyebrow: string;
  title: string;
  detail: string;
  quip: string;
}

interface GuideItem {
  title: string;
  detail: string;
}

@Component({
  selector: 'app-root',
  imports: [BeltStageComponent, OperatorPlatePlacementComponent, SelectedSeatDetailComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {
  protected readonly store = inject(BeltVisualizationStore);
  protected readonly selectedSeatDetail = this.store.selectedSeatDetail;
  protected readonly operatorPlacement = this.store.operatorPlacement;
  protected readonly beltSpeedDialog = this.store.beltSpeedDialog;
  protected readonly isGuideOpen = signal(false);
  protected readonly beltSpeedSelect = viewChild<ElementRef<HTMLSelectElement>>('beltSpeedSelect');
  protected readonly guideItems: GuideItem[] = [
    {
      title: 'Seats',
      detail:
        'Tap any seat to focus it. The selected seat card below handles start dining and checkout.',
    },
    {
      title: 'Reach ring',
      detail: 'The dashed ring marks which plates the selected seat can pick right now.',
    },
    {
      title: 'Kitchen cards',
      detail:
        'Plates counts dishes on the belt, while Seats shows how many guests are currently dining.',
    },
    {
      title: 'Plates control',
      detail:
        'Use the plus button in the Plates card to open the add-plates panel for the demo flow.',
    },
    {
      title: 'Selected seat card',
      detail:
        'The card on the right always describes the currently focused seat, including checkout and the running order summary.',
    },
    {
      title: 'Belt speed and settings',
      detail:
        'Belt speed shows the current movement timing. The gear opens the counter-speed dialog for the current belt.',
    },
  ];
  protected readonly heroPresentation = computed<HeroPresentation>(() => ({
    title: 'Kaitenzushi',
    subtitle: 'Sushi Train Simulator, grab a seat and eat',
  }));
  protected readonly shellStateCard = computed<ShellStateCard | null>(() => {
    const fatalMessage = this.store.fatalMessage();

    if (fatalMessage) {
      return {
        tone: 'error',
        eyebrow: 'Counter offline',
        title: 'We could not set the sushi belt table',
        detail: fatalMessage,
        quip: 'The rice is innocent. The data path is the one being dramatic.',
      };
    }

    if (this.store.hasNoBelts()) {
      return {
        tone: 'empty',
        eyebrow: 'Waiting for the first counter',
        title: 'No belts are ready yet',
        detail: 'The overview will appear as soon as the backend returns at least one belt.',
        quip: 'Even the chef refuses to mime conveyor-belt sounds before service starts.',
      };
    }

    if (this.store.isLoading()) {
      return {
        tone: 'loading',
        eyebrow: 'Setting the cafe mood',
        title: 'Setting the belt in motion',
        detail: 'Fetching the belt, its seats, and the latest movement snapshot.',
        quip: 'Tiny lanterns are imaginary. The loading state is unfortunately real.',
      };
    }

    return null;
  });

  private lastFocusedElement: HTMLElement | null = null;
  private shouldRestoreDialogFocus = false;

  constructor() {
    effect(() => {
      const dialog = this.beltSpeedDialog();

      if (dialog?.isOpen) {
        queueMicrotask(() => {
          const selectElement = this.beltSpeedSelect()?.nativeElement;
          if (!selectElement) {
            return;
          }

          selectElement.value = String(dialog.selectedSpeed);
          selectElement.focus();
        });
        return;
      }

      if (this.shouldRestoreDialogFocus) {
        const previousElement = this.lastFocusedElement;
        this.shouldRestoreDialogFocus = false;
        this.lastFocusedElement = null;
        queueMicrotask(() => previousElement?.focus());
      }
    });
  }

  protected openGuide(): void {
    this.isGuideOpen.set(true);
  }

  protected closeGuide(): void {
    this.isGuideOpen.set(false);
  }

  protected openBeltSpeedDialog(): void {
    this.captureFocusedElement();
    this.shouldRestoreDialogFocus = true;
    this.store.openBeltSpeedDialog();
  }

  protected closeBeltSpeedDialog(): void {
    this.store.closeBeltSpeedDialog();
  }

  protected updateBeltSpeedSelection(rawValue: string): void {
    const nextSpeed = Number(rawValue);
    if (!Number.isInteger(nextSpeed)) {
      return;
    }

    this.store.selectBeltSpeed(nextSpeed);
  }

  protected submitBeltSpeedDialog(): void {
    this.store.submitBeltSpeedDialog();
  }

  protected handleBeltSpeedDialogKeydown(event: KeyboardEvent): void {
    if (event.key === '+' || event.key === 'Add') {
      event.preventDefault();
      event.stopPropagation();
      this.store.stepBeltSpeedSelection(1);
      return;
    }

    if (event.key === '-' || event.key === 'Subtract') {
      event.preventDefault();
      event.stopPropagation();
      this.store.stepBeltSpeedSelection(-1);
    }
  }

  @HostListener('window:keydown', ['$event'])
  protected handleGlobalBeltSpeedKeydown(event: KeyboardEvent): void {
    if (this.beltSpeedDialog()?.isOpen || event.defaultPrevented) {
      return;
    }

    if (event.ctrlKey || event.altKey || event.metaKey) {
      return;
    }

    const target = event.target;
    if (target instanceof HTMLElement) {
      const tagName = target.tagName;
      if (target.isContentEditable || tagName === 'INPUT' || tagName === 'TEXTAREA' || tagName === 'SELECT') {
        return;
      }
    }

    if (event.key === '+' || event.key === 'Add') {
      event.preventDefault();
      this.store.nudgeBeltSpeed(1);
      return;
    }

    if (event.key === '-' || event.key === 'Subtract') {
      event.preventDefault();
      this.store.nudgeBeltSpeed(-1);
    }
  }

  private captureFocusedElement(): void {
    if (typeof document === 'undefined') {
      this.lastFocusedElement = null;
      return;
    }

    const activeElement = document.activeElement;
    this.lastFocusedElement = activeElement instanceof HTMLElement ? activeElement : null;
  }
}
