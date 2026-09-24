"use client";

import { useCallback, useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import { Alert, EmptyState, SecondaryButton, Spinner } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";
import type { User } from "@/lib/types";

function AdminUsuarios() {
  const [lista, setLista] = useState<User[] | null>(null);
  const [erro, setErro] = useState("");

  const carregar = useCallback(async () => {
    try {
      setLista(await api<User[]>("/admin/users"));
    } catch (err) {
      setErro((err as ApiError).message);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const toggle = async (id: number) => {
    try {
      await api(`/admin/users/${id}/toggle`, { method: "PATCH" });
      await carregar();
    } catch (err) {
      setErro((err as ApiError).message);
    }
  };

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!lista) return <Spinner />;

  return (
    <div>
      <h1 className="text-2xl font-bold">Usuários</h1>
      {lista.length === 0 && <div className="mt-4"><EmptyState>Nenhum usuário.</EmptyState></div>}
      <ul className="mt-4 space-y-2">
        {lista.map((u) => (
          <li key={u.id} className="flex flex-wrap items-center gap-2 rounded-xl border p-3 text-sm">
            <div className="min-w-0 flex-1">
              <p><strong>{u.nome}</strong> ({u.email}) · {u.tipo} {u.ativo ? "" : "· desativado"}</p>
            </div>
            {u.tipo !== "admin" && (
              <SecondaryButton onClick={() => void toggle(u.id)}>
                {u.ativo ? "Desativar" : "Ativar"}
              </SecondaryButton>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

export default function Page() {
  return (
    <RequireAuth tipos={["admin"]}>
      <AdminUsuarios />
    </RequireAuth>
  );
}
