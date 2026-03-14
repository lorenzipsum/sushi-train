import { EnvironmentProviders, InjectionToken, makeEnvironmentProviders } from '@angular/core';

export const DEFAULT_API_BASE_URL = '';

export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL');

export function buildApiUrl(baseUrl: string, path: string): string {
  const normalizedBaseUrl = baseUrl.endsWith('/') ? baseUrl.slice(0, -1) : baseUrl;
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;

  return `${normalizedBaseUrl}${normalizedPath}`;
}

export function provideApiConfig(baseUrl = DEFAULT_API_BASE_URL): EnvironmentProviders {
  return makeEnvironmentProviders([
    {
      provide: API_BASE_URL,
      useValue: baseUrl,
    },
  ]);
}
