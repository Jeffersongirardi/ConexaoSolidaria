export const API_URL =
  process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

export const API_ORIGIN =
  process.env.NEXT_PUBLIC_API_ORIGIN ?? "http://localhost:8080";

export class ApiError extends Error {
  status: number;
  errors?: Record<string, string>;

  constructor(status: number, message: string, errors?: Record<string, string>) {
    super(message);
    this.status = status;
    this.errors = errors;
  }
}

export function fileUrl(path?: string | null): string | null {
  if (!path) return null;
  if (path.startsWith("http") || path.startsWith("data:")) return path;
  return `${API_ORIGIN}${path}`;
}

const ACCESS_KEY = "cs_access";
const REFRESH_KEY = "cs_refresh";

export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(ACCESS_KEY);
}

export function getRefreshToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(REFRESH_KEY);
}

export function setTokens(access: string, refresh: string) {
  localStorage.setItem(ACCESS_KEY, access);
  localStorage.setItem(REFRESH_KEY, refresh);
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
  localStorage.removeItem("cs_user");
}

interface ApiOptions {
  method?: string;
  body?: unknown;
  form?: FormData;
  token?: string | null;
  retry?: boolean;
}

export async function api<T>(path: string, opts: ApiOptions = {}): Promise<T> {
  const token = opts.token !== undefined ? opts.token : getAccessToken();
  const headers: Record<string, string> = {};
  if (!(opts.form instanceof FormData)) headers["Content-Type"] = "application/json";
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch(`${API_URL}${path}`, {
    method: opts.method ?? (opts.body || opts.form ? "POST" : "GET"),
    headers,
    body:
      opts.form instanceof FormData
        ? opts.form
        : opts.body !== undefined
          ? JSON.stringify(opts.body)
          : undefined,
  });

  if (res.status === 401 && opts.retry !== false && getRefreshToken()) {
    const refreshed = await tryRefresh();
    if (refreshed) {
      return api<T>(path, { ...opts, token: refreshed, retry: false });
    }
    clearTokens();
  }

  if (!res.ok) {
    let message = `Erro ${res.status}`;
    let errors: Record<string, string> | undefined;
    try {
      const data = await res.json();
      if (typeof data?.message === "string") message = data.message;
      if (data?.errors) errors = data.errors;
    } catch {
      /* mantém mensagem padrão */
    }
    throw new ApiError(res.status, message, errors);
  }

  if (res.status === 204) return undefined as T;
  const text = await res.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

async function tryRefresh(): Promise<string | null> {
  const refresh = getRefreshToken();
  if (!refresh) return null;
  try {
    const res = await fetch(`${API_URL}/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken: refresh }),
    });
    if (!res.ok) return null;
    const data = await res.json();
    setTokens(data.accessToken, data.refreshToken);
    if (data.user) localStorage.setItem("cs_user", JSON.stringify(data.user));
    return data.accessToken as string;
  } catch {
    return null;
  }
}
