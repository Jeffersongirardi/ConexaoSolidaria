"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import RequireAuth from "@/components/RequireAuth";
import { Alert, EmptyState, SecondaryButton, Spinner, formatarData } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";
import type { NotificationItem } from "@/lib/types";

function NotificacoesConteudo() {
  const [lista, setLista] = useState<NotificationItem[] | null>(null);
  const [erro, setErro] = useState("");

  const carregar = useCallback(async () => {
    try {
      setLista(await api<NotificationItem[]>("/notifications"));
    } catch (err) {
      setErro((err as ApiError).message);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const ler = async (id: number) => {
    await api(`/notifications/${id}/ler`, { method: "PATCH" });
    await carregar();
  };

  const lerTodas = async () => {
    await api("/notifications/ler-todas", { method: "PATCH" });
    await carregar();
  };

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!lista) return <Spinner />;

  return (
    <div className="mx-auto max-w-2xl">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Notificações</h1>
        {lista.some((n) => !n.lida) && (
          <SecondaryButton onClick={() => void lerTodas()}>Marcar todas como lidas</SecondaryButton>
        )}
      </div>
      {lista.length === 0 && <div className="mt-4"><EmptyState>Nenhuma notificação.</EmptyState></div>}
      <ul className="mt-4 space-y-2">
        {lista.map((n) => (
          <li key={n.id} className={`rounded-xl border p-3 text-sm ${n.lida ? "" : "border-blue-400 bg-blue-50/50"}`}>
            <p>{n.mensagem}</p>
            <p className="mt-1 text-xs text-gray-500">{formatarData(n.dataCriacao)}</p>
            <p className="mt-2 flex gap-2">
              {!n.lida && <SecondaryButton onClick={() => void ler(n.id)}>Marcar como lida</SecondaryButton>}
              {n.link && <Link href={n.link} className="rounded-lg border border-gray-300 px-4 py-2 text-sm hover:bg-gray-50">Abrir</Link>}
            </p>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default function Page() {
  return (
    <RequireAuth>
      <NotificacoesConteudo />
    </RequireAuth>
  );
}
