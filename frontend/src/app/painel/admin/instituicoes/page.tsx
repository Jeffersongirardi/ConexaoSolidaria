"use client";

import { Suspense, useCallback, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import RequireAuth from "@/components/RequireAuth";
import { Alert, EmptyState, Field, PrimaryButton, SecondaryButton, Select, Spinner } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";
import type { Institution } from "@/lib/types";

function AdminInstituicoes() {
  const searchParams = useSearchParams();
  const [filtro, setFiltro] = useState(searchParams.get("filtro") ?? "pendentes");
  const [lista, setLista] = useState<Institution[] | null>(null);
  const [erro, setErro] = useState("");
  const [motivos, setMotivos] = useState<Record<number, string>>({});

  const carregar = useCallback(async () => {
    try {
      setLista(await api<Institution[]>(`/admin/institutions?filtro=${filtro}`));
    } catch (err) {
      setErro((err as ApiError).message);
    }
  }, [filtro]);

  useEffect(() => {
    setLista(null);
    void carregar();
  }, [carregar]);

  const aprovar = async (id: number) => {
    await api(`/admin/institutions/${id}/aprovar`, { method: "PATCH" });
    await carregar();
  };

  const recusar = async (id: number) => {
    await api(`/admin/institutions/${id}/recusar`, { method: "PATCH", body: { motivo: motivos[id] ?? "" } });
    await carregar();
  };

  return (
    <div>
      <h1 className="text-2xl font-bold">Validar instituições</h1>
      <div className="mt-4 max-w-xs">
        <Field label="Filtro" name="filtro">
          <Select id="filtro" value={filtro} onChange={(e) => setFiltro(e.target.value)}>
            <option value="pendentes">Pendentes</option>
            <option value="aprovadas">Aprovadas</option>
            <option value="todas">Todas</option>
          </Select>
        </Field>
      </div>
      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      {!lista && !erro && <Spinner />}
      {lista && lista.length === 0 && <div className="mt-4"><EmptyState>Nenhuma instituição neste filtro.</EmptyState></div>}
      {lista && lista.length > 0 && (
        <ul className="mt-4 space-y-3">
          {lista.map((inst) => (
            <li key={inst.id} className="rounded-xl border p-4 text-sm">
              <p><strong>{inst.razaoSocial}</strong> {inst.aprovado ? "(aprovada)" : "(pendente)"}</p>
              <p className="text-gray-600">CNPJ {inst.cnpj} · {inst.email} · {inst.cidade}{inst.estado ? `/${inst.estado}` : ""}</p>
              {inst.descricao && <p className="mt-1 text-gray-700">{inst.descricao}</p>}
              {inst.motivoRecusa && <p className="mt-1 text-red-700">Motivo anterior: {inst.motivoRecusa}</p>}
              <div className="mt-2 flex flex-wrap items-center gap-2">
                {!inst.aprovado && <PrimaryButton onClick={() => void aprovar(inst.id)}>Aprovar</PrimaryButton>}
                <input
                  aria-label={`Motivo da recusa para ${inst.razaoSocial}`}
                  placeholder="Motivo da recusa (opcional)"
                  value={motivos[inst.id] ?? ""}
                  onChange={(e) => setMotivos((m) => ({ ...m, [inst.id]: e.target.value }))}
                  className="min-w-52 flex-1 rounded-lg border border-gray-300 px-3 py-2 text-sm"
                />
                <SecondaryButton onClick={() => void recusar(inst.id)}>Recusar</SecondaryButton>
              </div>
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
      <Suspense fallback={<Spinner />}>
        <AdminInstituicoes />
      </Suspense>
    </RequireAuth>
  );
}
