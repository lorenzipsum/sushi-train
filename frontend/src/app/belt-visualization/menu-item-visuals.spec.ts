import { describe, expect, it } from 'vitest';

import { resolveMenuItemVisual } from './menu-item-visuals';

describe('menu-item-visuals', () => {
  it('maps seeded dishes to specific visual families and overrides', () => {
    expect(resolveMenuItemVisual('Tamago Nigiri')).toMatchObject({
      family: 'nigiri',
      visualKey: 'tamago-nigiri',
      vesselType: 'plate',
    });
    expect(resolveMenuItemVisual('Ikura (Salmon Roe) Gunkan')).toMatchObject({
      family: 'gunkan',
      visualKey: 'ikura-gunkan',
    });
    expect(resolveMenuItemVisual('Asahi Super Dry Beer')).toMatchObject({
      family: 'drink',
      vesselType: 'cup',
    });
  });

  it('falls back by family before using the generic chef special', () => {
    expect(resolveMenuItemVisual('Dragon Roll')).toMatchObject({
      family: 'roll',
      fallbackReason: 'family fallback',
    });
    expect(resolveMenuItemVisual('Mystery Bento')).toMatchObject({
      family: 'fallback',
      visualKey: 'chef-special',
    });
  });
});
