"use client";

import { Suspense, useCallback, useEffect, useState } from "react";
import Link from "next/link";
import RequireAuth from "@/components/RequireAuth";
import Voltar from "@/components/Voltar";
import { useSearchParams } from "next/navigation";
import { Alert, EmptyState, Field, Select, Spinner, TextInput } from "@/components/ui";
import SafeImage from "@/components/SafeImage";
import { api, fileUrl } from "@/lib/api";
import { CATEGORIAS, categoriaLabel } from "@/lib/categorias";
import type { Oferta } from "@/lib/types";

export function estadoOfertaLabel(e: string) {
  return e === "novo" ? "Novo" : e === "bom_estado" ? "Bom estado" : "Usado";
}

function OfertasConteudo() {
  const searchParams = useSearchParams();
  const [lista, setLista] = useState<Oferta[] | null>(null);
  const [erro, setErro] = useState("");
  const [categoria, setCategoria] = useState(searchParams.get("categoria") ?? "todas");
  const [busca, setBusca] = useState("");

  const carregar = useCallback(async () => {
    setErro("");
    try {
      const params = new URLSearchParams({
        ...(categoria !== "todas" ? { categoria } : {}),
        ...(busca.trim() ? { busca: busca.trim() } : {}),
      });
      setLista(await api<Oferta[]>(`/ofertas?${params}`));
    } catch {
      setErro("Não foi possível carregar as ofertas. Tente novamente.");
    }
  }, [categoria, busca]);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  return (
    <div>
      <Voltar fallback="/painel/instituicao" />
      <div className="mt-2 rounded-2xl bg-gradient-to-r from-[var(--primary)] to-[var(--primary-light)] p-5 text-white">
        <h1 className="text-2xl font-bold">🤝 Ofertas de doadores</h1>
        <p className="mt-1 text-sm text-white/90">Sofá, piano, violão, móveis — reivindicados por instituições com coleta em até 7 dias.</p>
      </div>

      <form
        aria-label="Filtros de ofertas"
        className="mt-4 grid gap-3 rounded-xl border bg-gray-50 p-4 sm:grid-cols-3"
        onSubmit={(e) => { e.preventDefault(); void carregar(); }}
      >
        <Field label="Buscar" name="busca">
          <TextInput id="busca" value={busca} onChange={(e) => setBusca(e.target.value)} placeholder="Ex.: sofá, violão" />
        </Field>
        <Field label="Categoria" name="categoria">
          <Select id="categoria" value={categoria} onChange={(e) => setCategoria(e.target.value)}>
            <option value="todas">Todas</option>
            {CATEGORIAS.map((c) => <option key={c.value} value={c.value}>{c.label}</option>)}
          </Select>
        </Field>
        <div className="flex items-end gap-2">
          <button type="submit" className="w-full rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700">
            Filtrar
          </button>
          <Link href="/ofertas/nova" className="w-full rounded-lg bg-[var(--accent)] px-4 py-2 text-center text-sm font-bold text-[var(--primary)] hover:brightness-95">
            Ofertar item
          </Link>
        </div>
      </form>

      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      {!lista && !erro && <Spinner />}
      {lista && lista.length === 0 && (
        <div className="mt-4"><EmptyState>Nenhuma oferta disponível agora. <Link href="/ofertas/nova" className="text-[var(--primary)] underline">Oferte um item</Link>!</EmptyState></div>
      )}
      {lista && lista.length > 0 && (
        <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {lista.map((o) => {
            const img = o.imagens?.[0]?.url ? fileUrl(o.imagens[0].url) : null;
            return (
              <article key={o.id} className="flex flex-col overflow-hidden rounded-[var(--radius-card)] border bg-[var(--surface)] shadow-sm transition hover:shadow-md">
                {img ? (
                  <SafeImage src={img} alt="" className="h-44 w-full object-cover" loading="lazy" />
                ) : (
                  <div aria-hidden="true" className="flex h-44 w-full items-center justify-center bg-[var(--accent-soft)] text-4xl">🎁</div>
                )}
                <div className="flex flex-1 flex-col gap-2 p-4">
                  <div className="flex items-center gap-2 text-xs">
                    <span className="rounded-full bg-green-100 px-2 py-0.5 font-semibold text-green-800">↗ Disponível</span>
                    <span className="text-[var(--text-soft)]">{categoriaLabel(o.categoria)} · {estadoOfertaLabel(o.estadoItem)}</span>
                  </div>
                  <h3 className="font-semibold leading-snug">
                    <Link href={`/ofertas/${o.id}`} className="hover:text-[var(--primary)]">{o.titulo}</Link>
                  </h3>
                  <p className="text-sm text-[var(--text-soft)]">
                    {o.cidade ?? "Local a combinar"} · até {o.disponivelAte}
                    {o.precisaColeta ? " · 🚚 precisa coleta" : ""}
                  </p>
                  <Link href={`/ofertas/${o.id}`} className="mt-auto rounded-lg bg-[var(--primary)] px-4 py-2 text-center text-sm font-semibold text-white hover:brightness-95">
                    Ver e reivindicar
                  </Link>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default function OfertasPage() {
  return (
    <RequireAuth tipos={["instituicao"]}>
      <Suspense fallback={<Spinner />}>
        <OfertasConteudo />
      </Suspense>
    </RequireAuth>
  );
}
