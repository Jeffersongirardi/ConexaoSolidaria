"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import { useRouter } from "next/navigation";
import { api, clearTokens, setTokens, type ApiError } from "./api";
import type { AuthResponse, User } from "./types";

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  login: (email: string, senha: string) => Promise<User>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();
  const [idleWarning, setIdleWarning] = useState(false);

  useEffect(() => {
    try {
      const raw = localStorage.getItem("cs_user");
      if (raw) setUser(JSON.parse(raw));
    } catch {
      /* ignora */
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (email: string, senha: string) => {
    const data = await api<AuthResponse>("/auth/login", {
      body: { email, senha },
      token: null,
    });
    localStorage.setItem("cs_activity", String(Date.now()));
    setTokens(data.accessToken, data.refreshToken);
    localStorage.setItem("cs_user", JSON.stringify(data.user));
    setUser(data.user);
    return data.user;
  }, []);

  const logout = useCallback(() => {
    void api("/auth/logout", { method: "POST", body: {} }).catch(() => {});
    clearTokens();
    setIdleWarning(false);
    setUser(null);
    router.push("/");
  }, [router]);

  const refreshUser = useCallback(async () => {
    try {
      const session = localStorage.getItem("cs_refresh");
      const me = await api<User>("/users/me");
      if (!session || !localStorage.getItem("cs_access")) return;
      localStorage.setItem("cs_user", JSON.stringify(me));
      setUser(me);
    } catch {
      /* mantém estado atual */
    }
  }, []);

  useEffect(() => {
    if (!user) return;
    const limit = 30 * 60_000;
    if (!localStorage.getItem("cs_activity")) localStorage.setItem("cs_activity", String(Date.now()));
    let ended = false;
    const check = () => {
      if (ended) return false;
      if (!localStorage.getItem("cs_access")) {
        ended = true;
        setUser(null);
        setIdleWarning(false);
        router.push("/");
        return false;
      }
      const elapsed = Date.now() - Number(localStorage.getItem("cs_activity") || 0);
      if (elapsed >= limit) {
        ended = true;
        logout();
        return false;
      }
      setIdleWarning(elapsed >= limit - 60_000);
      return true;
    };
    const activity = () => {
      // Uma interação após o prazo não deve reativar uma sessão expirada.
      if (!check()) return;
      localStorage.setItem("cs_activity", String(Date.now()));
      setIdleWarning(false);
    };
    const events = ["pointerdown", "keydown", "scroll", "touchstart"] as const;
    events.forEach(event => window.addEventListener(event, activity, { passive: true }));
    window.addEventListener("storage", check);
    window.addEventListener("focus", check);
    document.addEventListener("visibilitychange", check);
    const timer = window.setInterval(check, 1000);
    check();
    return () => {
      clearInterval(timer);
      events.forEach(event => window.removeEventListener(event, activity));
      window.removeEventListener("storage", check);
      window.removeEventListener("focus", check);
      document.removeEventListener("visibilitychange", check);
    };
  }, [user, logout, router]);

  const value = useMemo(
    () => ({ user, loading, login, logout, refreshUser }),
    [user, loading, login, logout, refreshUser]
  );

  return <AuthContext.Provider value={value}>
    {children}
    {idleWarning && <div role="alert" className="fixed bottom-20 left-4 right-4 z-50 md:bottom-4 rounded-xl border border-amber-400 bg-amber-50 p-4 text-gray-900 shadow-lg">
      <p>Sua sessão será encerrada em até 1 minuto por inatividade.</p>
      <button type="button" className="mt-2 rounded bg-emerald-900 px-4 py-2 text-white" onClick={() => {
        const elapsed = Date.now() - Number(localStorage.getItem("cs_activity") || 0);
        if (elapsed >= 30 * 60_000) { logout(); return; }
        localStorage.setItem("cs_activity", String(Date.now()));
        setIdleWarning(false);
      }}>Continuar conectado</button>
    </div>}
  </AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth deve ser usado dentro de AuthProvider");
  return ctx;
}

export function painelPorTipo(tipo: User["tipo"]): string {
  if (tipo === "admin") return "/painel/admin";
  if (tipo === "instituicao") return "/painel/instituicao";
  return "/painel/doador";
}

export type { ApiError };
