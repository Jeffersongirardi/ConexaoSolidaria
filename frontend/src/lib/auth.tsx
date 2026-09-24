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
    setTokens(data.accessToken, data.refreshToken);
    localStorage.setItem("cs_user", JSON.stringify(data.user));
    setUser(data.user);
    return data.user;
  }, []);

  const logout = useCallback(() => {
    clearTokens();
    setUser(null);
    router.push("/");
  }, [router]);

  const refreshUser = useCallback(async () => {
    try {
      const me = await api<User>("/users/me");
      localStorage.setItem("cs_user", JSON.stringify(me));
      setUser(me);
    } catch {
      /* mantém estado atual */
    }
  }, []);

  const value = useMemo(
    () => ({ user, loading, login, logout, refreshUser }),
    [user, loading, login, logout, refreshUser]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
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
