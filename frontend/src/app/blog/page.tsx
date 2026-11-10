"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { Alert, EmptyState, Pagination, Spinner, formatarDataSimples } from "@/components/ui";
import SafeImage from "@/components/SafeImage";
import { Newspaper } from "lucide-react";
import { api } from "@/lib/api";
import { categoriaBlogLabel, imagemBlog, tempoLeitura } from "@/lib/blog";
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
      <p className="mt-1 text-sm text-[var(--text-soft)]">
        Histórias, transparência e dicas para doar melhor.
      </p>

      <div className="mt-4 flex flex-wrap gap-2" aria-label="Filtrar por categoria">
        <button
          type="button"
          onClick={() => { setCategoria(""); setPage(0); }}
          aria-pressed={!categoria}
          className={`min-h-11 rounded-full border px-4 text-sm font-semibold transition ${
            !categoria
              ? "border-[var(--primary)] bg-[var(--primary)] text-white"
              : "border-[var(--border)] bg-[var(--surface)] text-[var(--text)] hover:bg-[var(--bg)]"
          }`}
        >
          Todas
        </button>
        {categorias.map((c) => (
          <button
            key={c}
            type="button"
            onClick={() => { setCategoria(c); setPage(0); }}
            aria-pressed={categoria === c}
            className={`min-h-11 rounded-full border px-4 text-sm font-semibold transition ${
              categoria === c
                ? "border-[var(--primary)] bg-[var(--primary)] text-white"
                : "border-[var(--border)] bg-[var(--surface)] text-[var(--text)] hover:bg-[var(--bg)]"
            }`}
          >
            {categoriaBlogLabel(c)}
          </button>
        ))}
      </div>

      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      {!data && !erro && <Spinner />}
      {data && data.content.length === 0 && (
        <div className="mt-4"><EmptyState>Nenhum post publicado ainda.</EmptyState></div>
      )}

      {data && data.content.length > 0 && (
        <>
          <div className="mt-6 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {data.content.map((p) => {
              const imagem = imagemBlog(p.imagemUrl);
              return (
                <article
                  key={p.id}
                  className="group flex flex-col overflow-hidden rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--surface)] shadow-sm transition hover:shadow-md"
                >
                  <Link
                    href={`/blog/${p.slug}`}
                    className="relative block aspect-[16/9] overflow-hidden bg-[var(--accent-soft)]"
                    aria-label={`Ler artigo: ${p.titulo}`}
                  >
                    {imagem ? (
                      <SafeImage
                        src={imagem}
                        alt={`Imagem do artigo ${p.titulo}`}
                        loading="lazy"
                        className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.02]"
                      />
                    ) : (
                      <div className="flex h-full w-full items-center justify-center text-[var(--text-soft)]">
                        <Newspaper className="h-10 w-10" aria-hidden="true" />
                      </div>
                    )}
                  </Link>

                  <div className="flex flex-1 flex-col gap-2 p-4">
                    <div className="flex items-center justify-between gap-2 text-xs">
                      <span className="rounded-full bg-[var(--accent-soft)] px-2 py-0.5 font-semibold text-[var(--primary)]">
                        {categoriaBlogLabel(p.categoria)}
                      </span>
                      <span className="text-[var(--text-soft)]">{tempoLeitura(p.conteudo)} min de leitura</span>
                    </div>

                    <h2 className="line-clamp-2 font-semibold leading-snug">
                      <Link href={`/blog/${p.slug}`} className="hover:text-[var(--primary)]">
                        {p.titulo}
                      </Link>
                    </h2>

                    {p.resumo && <p className="line-clamp-3 text-sm text-[var(--text-soft)]">{p.resumo}</p>}

                    <div className="mt-auto flex items-center justify-between gap-2 pt-2 text-xs text-[var(--text-soft)]">
                      <span>{formatarDataSimples(p.dataPublicacao)}</span>
                      <Link href={`/blog/${p.slug}`} className="font-semibold text-[var(--primary)] hover:underline">
                        Ler artigo →
                      </Link>
                    </div>
                  </div>
                </article>
              );
            })}
          </div>
          <Pagination page={data.number} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}
