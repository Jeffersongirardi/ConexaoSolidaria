"use client";

import { useCallback, useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import Voltar from "@/components/Voltar";
import { Alert, EmptyState, SecondaryButton, formatarData } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";
import type { Donation } from "@/lib/types";
import { toast } from "sonner";

function AdminDoacoes() {
  const [lista, setLista] = useState<Donation[]>([]);
  const [erro, setErro] = useState("");
  const [motivos, setMotivos] = useState<Record<number, string>>({});

  const carregar = useCallback(async () => {
    try {
      setLista(await api<Donation[]>("/admin/doacoes"));
    } catch (err) {
      setErro((err as ApiError).message);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const cancelar = async (id: number) => {
    const motivo = (motivos[id] ?? "").trim();
    if (!confirm("Cancelar esta doação? O doador será notificado.")) return;
    try {
      await api(`/admin/doacoes/${id}/cancelar`, { method: "PATCH", body: { motivo } });
      toast.success("Doação cancelada pela moderação");
      await carregar();
    } catch (err) {
      toast.error((err as ApiError).message);
    }
  };

  return (
    <div>
      <Voltar fallback="/painel/admin" />
      <h1 className="mt-2 text-2xl font-bold">Doações (moderação)</h1>
      <p className="mt-1 text-sm text-gray-600">Auditoria somente-leitura. Cancele apenas em caso de denúncia/fraude — o fluxo normal não passa pelo admin.</p>
      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      {lista.length === 0 ? (
        <div className="mt-4"><EmptyState>Nenhuma doação registrada.</EmptyState></div>
      ) : (
        <ul className="mt-4 space-y-2">
          {lista.map((d) => (
            <li key={d.id} className="rounded-xl border p-3 text-sm">
              <p><strong>#{d.id} {d.quantidade} de {d.item}</strong> — {d.doadorNome} → {d.campaignTitulo} ({d.instituicaoNome})</p>
              <p className="mt-1 text-gray-600">Status: <strong>{d.status}</strong> · {formatarData(d.dataIntencao)}</p>
              {d.status !== "cancelado" && (
                <p className="mt-2 flex flex-wrap items-center gap-2">
                  <input
                    placeholder="Motivo (opcional)"
                    value={motivos[d.id] ?? ""}
                    onChange={(e) => setMotivos((m) => ({ ...m, [d.id]: e.target.value }))}
                    className="rounded-lg border px-3 py-1.5 text-sm"
                  />
                  <SecondaryButton onClick={() => void cancelar(d.id)}>Cancelar (moderação)</SecondaryButton>
                </p>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default function Page() {
  return (
    <RequireAuth tipos={["admin"]}>
      <AdminDoacoes />
    </RequireAuth>
  );
}
