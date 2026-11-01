"use client";

import { useCallback, useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import Voltar from "@/components/Voltar";
import { Alert, EmptyState, SecondaryButton, Spinner } from "@/components/ui";
import { useConfirm } from "@/components/useConfirm";
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

  const { confirmar: confirmarModal, dialog: dialogoConfirm } = useConfirm();

  const toggle = async (id: number, ativo: boolean | undefined) => {
    const desativando = Boolean(ativo);
    if (!(await confirmarModal({
      title: desativando ? "Desativar usuário?" : "Reativar usuário?",
      description: desativando
        ? "Se for instituição, as campanhas dela também serão desativadas."
        : "O acesso do usuário será restabelecido.",
      confirmLabel: desativando ? "Desativar" : "Ativar",
    }))) return;
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
      <Voltar fallback="/painel/admin" />
      <h1 className="mt-2 text-2xl font-bold">Usuários</h1>
      {lista.length === 0 && <div className="mt-4"><EmptyState>Nenhum usuário.</EmptyState></div>}
      <ul className="mt-4 space-y-2">
        {lista.map((u) => (
          <li key={u.id} className="flex flex-wrap items-center gap-2 rounded-xl border p-3 text-sm break-words">
            <div className="min-w-0 flex-1">
              <p><strong>{u.nome}</strong> ({u.email}) · {u.tipo} {u.ativo ? "" : "· desativado"}</p>
            </div>
            {u.tipo !== "admin" && (
              <SecondaryButton onClick={() => void toggle(u.id, u.ativo)}>
                {u.ativo ? "Desativar" : "Ativar"}
              </SecondaryButton>
            )}
          </li>
        ))}
      </ul>
      {dialogoConfirm}
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
