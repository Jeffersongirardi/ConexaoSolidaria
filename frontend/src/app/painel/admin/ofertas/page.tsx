"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import RequireAuth from "@/components/RequireAuth";
import Voltar from "@/components/Voltar";
import { Alert, EmptyState, PrimaryButton, SecondaryButton, Select, Field } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";
import type { Oferta } from "@/lib/types";
import { toast } from "sonner";

function AdminOfertas() {
  const [lista, setLista] = useState<Oferta[]>([]);
  const [filtro, setFiltro] = useState("pendentes");
  const [erro, setErro] = useState("");
  const [motivos, setMotivos] = useState<Record<number, string>>({});

  const carregar = useCallback(async () => {
    try {
      setLista(await api<Oferta[]>(`/admin/ofertas?filtro=${filtro}`));
    } catch (err) {
      setErro((err as ApiError).message);
    }
  }, [filtro]);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const aprovar = async (id: number) => {
    try {
      await api(`/admin/ofertas/${id}/aprovar`, { method: "PATCH" });
      toast.success("Oferta aprovada e visível às instituições");
      await carregar();
    } catch (err) {
      toast.error((err as ApiError).message);
    }
  };

  const recusar = async (id: number) => {
    const motivo = (motivos[id] ?? "").trim();
    if (!motivo) {
      toast.error("Informe o motivo da recusa");
      return;
    }
    try {
      await api(`/admin/ofertas/${id}/recusar`, { method: "PATCH", body: { motivo } });
      toast.success("Oferta recusada");
      await carregar();
    } catch (err) {
      toast.error((err as ApiError).message);
    }
  };

  return (
    <div>
      <Voltar fallback="/painel/admin" />
      <h1 className="mt-2 text-2xl font-bold">Ofertas de doadores</h1>
      <p className="mt-1 text-sm text-gray-600">Aprove para exibir às instituições. Recusas exigem motivo (notificamos o doador).</p>
      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      <div className="mt-4 max-w-xs">
        <Field label="Filtro" name="filtro">
          <Select id="filtro" value={filtro} onChange={(e) => setFiltro(e.target.value)}>
            <option value="pendentes">Pendentes</option>
            <option value="aprovadas">Aprovadas</option>
            <option value="todas">Todas</option>
          </Select>
        </Field>
      </div>
      {lista.length === 0 ? (
        <div className="mt-4"><EmptyState>Nenhuma oferta neste filtro.</EmptyState></div>
      ) : (
        <ul className="mt-4 space-y-2">
          {lista.map((o) => (
            <li key={o.id} className="rounded-xl border p-3 text-sm">
              <p><strong>{o.titulo}</strong> — {o.doadorNome} · {o.status} · até {o.disponivelAte}</p>
              <p className="mt-1 text-gray-600">{o.descricao}</p>
              {o.motivoRecusa && <p className="mt-1 text-red-700">Recusada: {o.motivoRecusa}</p>}
              <p className="mt-2 flex flex-wrap items-center gap-2">
                <Link href={`/ofertas/${o.id}`} className="rounded-lg border px-3 py-1.5 hover:bg-gray-50">Ver</Link>
                {!o.aprovado && (
                  <>
                    <PrimaryButton onClick={() => void aprovar(o.id)}>Aprovar</PrimaryButton>
                    <input
                      placeholder="Motivo da recusa (obrigatório)"
                      value={motivos[o.id] ?? ""}
                      onChange={(e) => setMotivos((m) => ({ ...m, [o.id]: e.target.value }))}
                      className="rounded-lg border px-3 py-1.5 text-sm"
                    />
                    <SecondaryButton onClick={() => void recusar(o.id)}>Recusar</SecondaryButton>
                  </>
                )}
              </p>
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
      <AdminOfertas />
    </RequireAuth>
  );
}
