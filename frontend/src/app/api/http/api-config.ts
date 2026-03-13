import { EnvironmentProviders, InjectionToken, makeEnvironmentProviders } from '@angular/core';

export const DEFAULT_API_BASE_URL = 'http://localhost:8088';

export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL');

export function provideApiConfig(baseUrl = DEFAULT_API_BASE_URL): EnvironmentProviders {
  return makeEnvironmentProviders([
    {
      provide: API_BASE_URL,
      useValue: baseUrl,
    },
  ]);
}
