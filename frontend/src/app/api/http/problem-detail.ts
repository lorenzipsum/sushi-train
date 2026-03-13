import type { ProblemDetail } from '../types';

export function isProblemDetail(value: unknown): value is ProblemDetail {
  if (!value || typeof value !== 'object') {
    return false;
  }

  return 'status' in value || 'title' in value || 'detail' in value;
}

export function getProblemDetail(error: unknown): ProblemDetail | null {
  if (!error || typeof error !== 'object' || !('error' in error)) {
    return null;
  }

  const payload = error.error;
  return isProblemDetail(payload) ? payload : null;
}
