export type MenuItemVisualFamily =
  | 'nigiri'
  | 'sashimi'
  | 'roll'
  | 'gunkan'
  | 'side'
  | 'dessert'
  | 'drink'
  | 'fallback';

export type VesselType = 'plate' | 'bowl' | 'cup' | 'board';

export interface MenuItemVisual {
  visualKey: string;
  family: MenuItemVisualFamily;
  overrideName: string | null;
  vesselType: VesselType;
  accentClass: string;
  fallbackReason: string | null;
}

interface MenuItemVisualDefinition {
  family: MenuItemVisualFamily;
  visualKey: string;
  vesselType: VesselType;
  accentClass: string;
}

const EXACT_VISUALS: Record<string, MenuItemVisualDefinition> = {
  'salmon nigiri': {
    family: 'nigiri',
    visualKey: 'salmon-nigiri',
    vesselType: 'plate',
    accentClass: 'accent-coral',
  },
  'tuna nigiri': {
    family: 'nigiri',
    visualKey: 'tuna-nigiri',
    vesselType: 'plate',
    accentClass: 'accent-ruby',
  },
  'yellowtail nigiri': {
    family: 'nigiri',
    visualKey: 'yellowtail-nigiri',
    vesselType: 'plate',
    accentClass: 'accent-amber',
  },
  'ebi (shrimp) nigiri': {
    family: 'nigiri',
    visualKey: 'ebi-nigiri',
    vesselType: 'plate',
    accentClass: 'accent-shrimp',
  },
  'unagi nigiri': {
    family: 'nigiri',
    visualKey: 'unagi-nigiri',
    vesselType: 'plate',
    accentClass: 'accent-smoke',
  },
  'tamago nigiri': {
    family: 'nigiri',
    visualKey: 'tamago-nigiri',
    vesselType: 'plate',
    accentClass: 'accent-egg',
  },
  'saba nigiri': {
    family: 'nigiri',
    visualKey: 'saba-nigiri',
    vesselType: 'plate',
    accentClass: 'accent-silver',
  },
  'ika (squid) nigiri': {
    family: 'nigiri',
    visualKey: 'ika-nigiri',
    vesselType: 'plate',
    accentClass: 'accent-pearl',
  },
  'hotate (scallop) nigiri': {
    family: 'nigiri',
    visualKey: 'hotate-nigiri',
    vesselType: 'plate',
    accentClass: 'accent-shell',
  },
  'kani (crab) nigiri': {
    family: 'nigiri',
    visualKey: 'kani-nigiri',
    vesselType: 'plate',
    accentClass: 'accent-crab',
  },
  'salmon sashimi': {
    family: 'sashimi',
    visualKey: 'salmon-sashimi',
    vesselType: 'board',
    accentClass: 'accent-coral',
  },
  'tuna sashimi': {
    family: 'sashimi',
    visualKey: 'tuna-sashimi',
    vesselType: 'board',
    accentClass: 'accent-ruby',
  },
  'california roll': {
    family: 'roll',
    visualKey: 'california-roll',
    vesselType: 'plate',
    accentClass: 'accent-avocado',
  },
  'spicy tuna roll': {
    family: 'roll',
    visualKey: 'spicy-tuna-roll',
    vesselType: 'plate',
    accentClass: 'accent-ruby',
  },
  'kappa maki (cucumber)': {
    family: 'roll',
    visualKey: 'kappa-maki',
    vesselType: 'plate',
    accentClass: 'accent-matcha',
  },
  'tekka maki (tuna)': {
    family: 'roll',
    visualKey: 'tekka-maki',
    vesselType: 'plate',
    accentClass: 'accent-ruby',
  },
  futomaki: {
    family: 'roll',
    visualKey: 'futomaki',
    vesselType: 'plate',
    accentClass: 'accent-plum',
  },
  'ikura (salmon roe) gunkan': {
    family: 'gunkan',
    visualKey: 'ikura-gunkan',
    vesselType: 'plate',
    accentClass: 'accent-roe',
  },
  'negitoro gunkan': {
    family: 'gunkan',
    visualKey: 'negitoro-gunkan',
    vesselType: 'plate',
    accentClass: 'accent-ruby',
  },
  'corn mayo gunkan': {
    family: 'gunkan',
    visualKey: 'corn-mayo-gunkan',
    vesselType: 'plate',
    accentClass: 'accent-corn',
  },
  'miso soup': {
    family: 'side',
    visualKey: 'miso-soup',
    vesselType: 'bowl',
    accentClass: 'accent-soy',
  },
  edamame: {
    family: 'side',
    visualKey: 'edamame',
    vesselType: 'bowl',
    accentClass: 'accent-matcha',
  },
  'gyoza (5pc)': {
    family: 'side',
    visualKey: 'gyoza',
    vesselType: 'board',
    accentClass: 'accent-toast',
  },
  'chicken karaage': {
    family: 'side',
    visualKey: 'karaage',
    vesselType: 'board',
    accentClass: 'accent-amber',
  },
  'agedashi tofu': {
    family: 'side',
    visualKey: 'agedashi-tofu',
    vesselType: 'bowl',
    accentClass: 'accent-soy',
  },
  'mochi ice cream': {
    family: 'dessert',
    visualKey: 'mochi-ice-cream',
    vesselType: 'plate',
    accentClass: 'accent-pink',
  },
  'purin (custard)': {
    family: 'dessert',
    visualKey: 'purin',
    vesselType: 'plate',
    accentClass: 'accent-caramel',
  },
  'cheesecake slice': {
    family: 'dessert',
    visualKey: 'cheesecake-slice',
    vesselType: 'plate',
    accentClass: 'accent-cream',
  },
  'green tea (hot)': {
    family: 'drink',
    visualKey: 'green-tea',
    vesselType: 'cup',
    accentClass: 'accent-matcha',
  },
  'matcha latte': {
    family: 'drink',
    visualKey: 'matcha-latte',
    vesselType: 'cup',
    accentClass: 'accent-matcha',
  },
  cola: { family: 'drink', visualKey: 'cola', vesselType: 'cup', accentClass: 'accent-cola' },
  'mineral water': {
    family: 'drink',
    visualKey: 'mineral-water',
    vesselType: 'cup',
    accentClass: 'accent-mizu',
  },
  'asahi super dry beer': {
    family: 'drink',
    visualKey: 'asahi-beer',
    vesselType: 'cup',
    accentClass: 'accent-gold',
  },
};

function normalizeName(menuItemName: string | null | undefined): string {
  return (menuItemName ?? '').trim().toLowerCase();
}

function buildVisual(
  menuItemName: string | null | undefined,
  definition: MenuItemVisualDefinition,
  fallbackReason: string | null,
): MenuItemVisual {
  return {
    visualKey: definition.visualKey,
    family: definition.family,
    overrideName: menuItemName ?? null,
    vesselType: definition.vesselType,
    accentClass: definition.accentClass,
    fallbackReason,
  };
}

export function resolveMenuItemVisual(menuItemName: string | null | undefined): MenuItemVisual {
  const normalizedName = normalizeName(menuItemName);
  const exactVisual = EXACT_VISUALS[normalizedName];

  if (exactVisual) {
    return buildVisual(menuItemName, exactVisual, null);
  }

  if (normalizedName.includes('nigiri')) {
    if (normalizedName.includes('salmon')) {
      return buildVisual(menuItemName, EXACT_VISUALS['salmon nigiri'], 'family fallback');
    }

    if (normalizedName.includes('tuna')) {
      return buildVisual(menuItemName, EXACT_VISUALS['tuna nigiri'], 'family fallback');
    }

    if (normalizedName.includes('tamago') || normalizedName.includes('egg')) {
      return buildVisual(menuItemName, EXACT_VISUALS['tamago nigiri'], 'family fallback');
    }

    return buildVisual(menuItemName, EXACT_VISUALS['salmon nigiri'], 'family fallback');
  }

  if (normalizedName.includes('sashimi')) {
    return buildVisual(menuItemName, EXACT_VISUALS['salmon sashimi'], 'family fallback');
  }

  if (
    normalizedName.includes('roll') ||
    normalizedName.includes('maki') ||
    normalizedName.includes('futomaki')
  ) {
    return buildVisual(menuItemName, EXACT_VISUALS['california roll'], 'family fallback');
  }

  if (normalizedName.includes('gunkan')) {
    return buildVisual(menuItemName, EXACT_VISUALS['ikura (salmon roe) gunkan'], 'family fallback');
  }

  if (
    normalizedName.includes('tea') ||
    normalizedName.includes('latte') ||
    normalizedName.includes('cola') ||
    normalizedName.includes('ramune') ||
    normalizedName.includes('water') ||
    normalizedName.includes('beer')
  ) {
    if (normalizedName.includes('cola') || normalizedName.includes('ramune')) {
      return buildVisual(menuItemName, EXACT_VISUALS['cola'], 'family fallback');
    }

    if (normalizedName.includes('water') || normalizedName.includes('mizu')) {
      return buildVisual(menuItemName, EXACT_VISUALS['mineral water'], 'family fallback');
    }

    if (normalizedName.includes('beer')) {
      return buildVisual(menuItemName, EXACT_VISUALS['asahi super dry beer'], 'family fallback');
    }

    return buildVisual(menuItemName, EXACT_VISUALS['green tea (hot)'], 'family fallback');
  }

  if (
    normalizedName.includes('mochi') ||
    normalizedName.includes('custard') ||
    normalizedName.includes('pudding') ||
    normalizedName.includes('cheesecake') ||
    normalizedName.includes('ice cream') ||
    normalizedName.includes('dessert')
  ) {
    if (normalizedName.includes('custard') || normalizedName.includes('pudding')) {
      return buildVisual(menuItemName, EXACT_VISUALS['purin (custard)'], 'family fallback');
    }

    if (normalizedName.includes('cheesecake')) {
      return buildVisual(menuItemName, EXACT_VISUALS['cheesecake slice'], 'family fallback');
    }

    return buildVisual(menuItemName, EXACT_VISUALS['mochi ice cream'], 'family fallback');
  }

  if (
    normalizedName.includes('soup') ||
    normalizedName.includes('edamame') ||
    normalizedName.includes('gyoza') ||
    normalizedName.includes('karaage') ||
    normalizedName.includes('tofu')
  ) {
    return buildVisual(menuItemName, EXACT_VISUALS['miso soup'], 'family fallback');
  }

  return {
    visualKey: 'chef-special',
    family: 'fallback',
    overrideName: menuItemName ?? null,
    vesselType: 'plate',
    accentClass: 'accent-neutral',
    fallbackReason: normalizedName ? 'unknown menu item' : 'missing menu item name',
  };
}
