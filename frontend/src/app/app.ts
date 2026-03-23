import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';

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
  protected readonly isGuideOpen = signal(false);
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
        'Belt speed shows the current movement timing. The gear in that card is a disabled placeholder for future counter controls.',
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
  protected openGuide(): void {
    this.isGuideOpen.set(true);
  }

  protected closeGuide(): void {
    this.isGuideOpen.set(false);
  }
}
