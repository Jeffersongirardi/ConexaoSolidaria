"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { Alert, EmptyState, Field, Pagination, Select, Spinner, formatarData } from "@/components/ui";
import { api } from "@/lib/api";
import type { BlogPost, Page } from "@/lib/types";

export default function BlogPage() {
  const [data, setData] = useState<Page<BlogPost> | null>(null);
  const [categorias, setCategorias] = useState<string[]>([]);
  const [categoria, setCategoria] = useState("");
  const [page, setPage] = useState(0);
  const [erro, setErro] = useState("");

  const carregar = useCallback(async () => {
    try {
      const params = new URLSearchParams({ page: String(page), size: "6" });
      if (categoria) params.set("categoria", categoria);
      setData(await api<Page<BlogPost>>(`/blog?${params}`, { token: null }));
    } catch {
      setErro("Não foi possível carregar o blog.");
    }
  }, [categoria, page]);

  useEffect(() => {
    api<string[]>("/blog/categorias", { token: null }).then(setCategorias).catch(() => {});
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  return (
    <div>
      <h1 className="text-2xl font-bold">Blog</h1>
      <p className="mt-1 text-sm text-gray-600">Histórias, transparência e novidades da rede solidária.</p>
      <div className="mt-4 max-w-xs">
        <Field label="Categoria" name="categoria">
          <Select id="categoria" value={categoria} onChange={(e) => { setCategoria(e.target.value); setPage(0); }}>
            <option value="">Todas</option>
            {categorias.map((c) => <option key={c} value={c}>{c}</option>)}
          </Select>
        </Field>
      </div>
      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      {!data && !erro && <Spinner />}
      {data && data.content.length === 0 && <div className="mt-4"><EmptyState>Nenhum post publicado ainda.</EmptyState></div>}
      {data && data.content.length > 0 && (
        <>
          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            {data.content.map((p) => (
              <article key={p.id} className="rounded-xl border p-4">
                <p className="text-xs text-gray-500">{p.categoria} · {formatarData(p.dataPublicacao)}</p>
                <h2 className="mt-1 font-semibold">
                  <Link href={`/blog/${p.slug}`} className="hover:text-blue-700">{p.titulo}</Link>
                </h2>
                {p.resumo && <p className="mt-1 text-sm text-gray-600">{p.resumo}</p>}
              </article>
            ))}
          </div>
          <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}
