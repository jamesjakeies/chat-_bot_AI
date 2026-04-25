import type { ApiEnvelope, AuthResponse } from './types';

const TOKEN_KEY = 'xinyu_admin_access_token';
const REFRESH_TOKEN_KEY = 'xinyu_admin_refresh_token';
const API_BASE_URL_KEY = 'xinyu_admin_api_base_url';

export function defaultApiBaseUrl(): string {
  return process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:3000';
}

export function getApiBaseUrl(): string {
  if (typeof window === 'undefined') return defaultApiBaseUrl();
  return localStorage.getItem(API_BASE_URL_KEY) ?? defaultApiBaseUrl();
}

export function setApiBaseUrl(value: string): void {
  localStorage.setItem(API_BASE_URL_KEY, value.replace(/\/+$/, ''));
}

export function getAccessToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
}

export function clearAuth(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export async function login(input: {
  apiBaseUrl: string;
  email: string;
  password: string;
}): Promise<AuthResponse> {
  setApiBaseUrl(input.apiBaseUrl);

  const response = await fetch(`${getApiBaseUrl()}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      email: input.email,
      password: input.password,
    }),
  });

  const envelope = (await response.json()) as ApiEnvelope<AuthResponse>;
  if (!response.ok || !envelope.success || !envelope.data) {
    throw new Error(envelope.message ?? '登录失败，请检查账号或后端服务。');
  }

  localStorage.setItem(TOKEN_KEY, envelope.data.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, envelope.data.refreshToken);
  return envelope.data;
}

export async function adminGet<T>(path: string): Promise<T> {
  return adminRequest<T>(path, { method: 'GET' });
}

export async function adminPatch<T>(path: string, body: unknown): Promise<T> {
  return adminRequest<T>(path, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(body),
  });
}

async function adminRequest<T>(path: string, init: RequestInit): Promise<T> {
  const token = getAccessToken();
  if (!token) {
    throw new Error('请先登录管理后台。');
  }

  const headers = new Headers(init.headers);
  headers.set('Authorization', `Bearer ${token}`);

  const response = await fetch(`${getApiBaseUrl()}${path}`, {
    ...init,
    headers,
  });

  const envelope = (await response.json()) as ApiEnvelope<T>;
  if (!response.ok || !envelope.success || envelope.data === undefined) {
    throw new Error(envelope.message ?? '请求失败。');
  }

  return envelope.data;
}
