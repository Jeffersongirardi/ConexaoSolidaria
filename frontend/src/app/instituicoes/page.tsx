"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Alert, EmptyState, Spinner } from "@/components/ui";
import { api, fileUrl } from "@/lib/api";
import type { Institution } from "@/lib/types";

export default function InstituicoesPage() {
  const [lista, setLista] = useState<Institution[] | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    api<Institution[]>("/institutions", { token: null })
      .then(setLista)
      .catch(() => setErro("Não foi possível carregar as instituições."));
  }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold">Instituições parceiras</h1>
      <p className="mt-1 text-sm text-gray-600">Organizações validadas que atuam em Curitiba/PR.</p>
      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      {!lista && !erro && <Spinner />}
      {lista && lista.length === 0 && <div className="mt-4"><EmptyState>Nenhuma instituição aprovada ainda.</EmptyState></div>}
      {lista && lista.length > 0 && (
        <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {lista.map((inst) => {
            const foto = fileUrl(inst.fotoUrl);
            return (
              <article key={inst.id} className="flex flex-col rounded-2xl border border-[var(--border)] bg-white p-4 shadow-[0_10px_28px_-14px_rgba(26,77,62,0.32)] transition-shadow hover:shadow-[0_16px_32px_-14px_rgba(26,77,62,0.36)]">
                {foto ? (
                  <img src={foto} alt={`Foto de ${inst.nomeFantasia || inst.razaoSocial}`} className="h-36 w-full rounded-lg object-cover" loading="lazy" />
                ) : (
                  <div aria-hidden="true" className="flex h-36 w-full items-center justify-center rounded-lg bg-blue-50 text-5xl">🏠</div>
                )}
                <h2 className="mt-3 font-semibold">{inst.nomeFantasia || inst.razaoSocial}</h2>
                <p className="mt-1 line-clamp-2 text-sm text-gray-600">{inst.descricao || "Sem descrição."}</p>
                <p className="mt-1 text-xs text-gray-500">{inst.cidade}{inst.cidade && inst.estado ? "/" : ""}{inst.estado}</p>
                <Link href={`/instituicoes/${inst.id}`} className="mt-4 flex min-h-11 items-center justify-center rounded-lg bg-[var(--accent)] px-4 py-2 text-center text-sm font-bold text-[var(--primary)] shadow-sm transition-colors hover:bg-[#f0b94f] focus-visible:outline-[var(--primary)]">
                  Ver perfil e campanhas
                </Link>
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}
