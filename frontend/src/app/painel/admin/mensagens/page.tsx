"use client";

import { useCallback, useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import { Alert, DangerButton, EmptyState, SecondaryButton, Spinner, formatarData } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";
import type { ContactMessage } from "@/lib/types";

function AdminMensagens() {
  const [lista, setLista] = useState<ContactMessage[] | null>(null);
  const [erro, setErro] = useState("");

  const carregar = useCallback(async () => {
    try {
      setLista(await api<ContactMessage[]>("/admin/messages"));
    } catch (err) {
      setErro((err as ApiError).message);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const marcar = async (id: number) => {
    await api(`/admin/messages/${id}/ler`, { method: "PATCH" });
    await carregar();
  };

  const remover = async (id: number) => {
    if (!confirm("Remover esta mensagem?")) return;
    await api(`/admin/messages/${id}`, { method: "DELETE" });
    await carregar();
  };

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!lista) return <Spinner />;

  return (
    <div>
      <h1 className="text-2xl font-bold">Mensagens de contato</h1>
      {lista.length === 0 && <div className="mt-4"><EmptyState>Nenhuma mensagem.</EmptyState></div>}
      <ul className="mt-4 space-y-2">
        {lista.map((m) => (
          <li key={m.id} className={`rounded-xl border p-3 text-sm ${m.lido ? "" : "border-blue-400 bg-blue-50/50"}`}>
            <p><strong>{m.nome}</strong> ({m.email}) · {formatarData(m.dataEnvio)} {!m.lido && <strong>· nova</strong>}</p>
            {m.assunto && <p className="text-gray-600">Assunto: {m.assunto}</p>}
            <p className="mt-1 whitespace-pre-line">{m.mensagem}</p>
            <p className="mt-2 flex gap-2">
              {!m.lido && <SecondaryButton onClick={() => void marcar(m.id)}>Marcar como lida</SecondaryButton>}
              <DangerButton onClick={() => void remover(m.id)}>Remover</DangerButton>
            </p>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default function Page() {
  return (
    <RequireAuth tipos={["admin"]}>
      <AdminMensagens />
    </RequireAuth>
  );
}
