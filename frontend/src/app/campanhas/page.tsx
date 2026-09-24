"use client";

import { Suspense, useCallback, useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import CampaignCard from "@/components/CampaignCard";
import { Alert, EmptyState, Field, Pagination, Select, Spinner, TextInput } from "@/components/ui";
import { api } from "@/lib/api";
import type { Campaign, Page } from "@/lib/types";

const categorias = ["todas", "alimento", "roupa", "higiene", "material_escolar", "outro"];
const urgencias = ["todas", "alta", "media", "baixa"];

function CampanhasConteudo() {
  const searchParams = useSearchParams();
  const [data, setData] = useState<Page<Campaign> | null>(null);
  const [erro, setErro] = useState("");
  const [categoria, setCategoria] = useState(searchParams.get("categoria") ?? "todas");
  const [urgencia, setUrgencia] = useState("todas");
  const [busca, setBusca] = useState("");
  const [page, setPage] = useState(0);

  const carregar = useCallback(async () => {
    setErro("");
    try {
      const params = new URLSearchParams({
        page: String(page),
        size: "12",
        ...(categoria !== "todas" ? { categoria } : {}),
        ...(urgencia !== "todas" ? { urgencia } : {}),
        ...(busca.trim() ? { busca: busca.trim() } : {}),
      });
      setData(await api<Page<Campaign>>(`/campaigns?${params}`, { token: null }));
    } catch {
      setErro("Não foi possível carregar as campanhas. Tente novamente.");
    }
  }, [categoria, urgencia, busca, page]);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  return (
    <div>
      <h1 className="text-2xl font-bold">Campanhas de arrecadação</h1>
      <p className="mt-1 text-sm text-gray-600">Alimentos, roupas e itens essenciais de instituições validadas.</p>

      <form
        aria-label="Filtros de campanha"
        className="mt-4 grid gap-3 rounded-xl border bg-gray-50 p-4 sm:grid-cols-4"
        onSubmit={(e) => {
          e.preventDefault();
          setPage(0);
          void carregar();
        }}
      >
        <Field label="Buscar" name="busca">
          <TextInput id="busca" value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Ex.: cesta básica" />
        </Field>
        <Field label="Categoria" name="categoria">
          <Select id="categoria" value={categoria} onChange={(e) => { setCategoria(e.target.value); setPage(0); }}>
            {categorias.map((c) => <option key={c} value={c}>{c === "todas" ? "Todas" : c.replace("_", " ")}</option>)}
          </Select>
        </Field>
        <Field label="Urgência" name="urgencia">
          <Select id="urgencia" value={urgencia} onChange={(e) => { setUrgencia(e.target.value); setPage(0); }}>
            {urgencias.map((u) => <option key={u} value={u}>{u === "todas" ? "Todas" : u}</option>)}
          </Select>
        </Field>
        <div className="flex items-end">
          <button type="submit" className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700">
            Filtrar
          </button>
        </div>
      </form>

      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      {!data && !erro && <Spinner />}
      {data && data.content.length === 0 && (
        <div className="mt-4"><EmptyState>Nenhuma campanha encontrada com esses filtros.</EmptyState></div>
      )}
      {data && data.content.length > 0 && (
        <>
          <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {data.content.map((c) => <CampaignCard key={c.id} campaign={c} />)}
          </div>
          <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}

export default function CampanhasPage() {
  return (
    <Suspense fallback={<Spinner />}>
      <CampanhasConteudo />
    </Suspense>
  );
}
