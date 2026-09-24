"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import RequireAuth from "@/components/RequireAuth";
import { Alert, DangerButton, EmptyState, Spinner, formatarData } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";
import type { BlogPost, Page } from "@/lib/types";

function AdminBlog() {
  const [data, setData] = useState<Page<BlogPost> | null>(null);
  const [erro, setErro] = useState("");

  const carregar = useCallback(async () => {
    try {
      setData(await api<Page<BlogPost>>("/blog?page=0&size=50", { token: null }));
    } catch (err) {
      setErro((err as ApiError).message);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const remover = async (id: number) => {
    if (!confirm("Remover este post?")) return;
    try {
      await api(`/blog/${id}`, { method: "DELETE" });
      await carregar();
    } catch (err) {
      setErro((err as ApiError).message);
    }
  };

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!data) return <Spinner />;

  return (
    <div>
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Blog</h1>
        <Link href="/painel/admin/blog/novo" className="rounded-lg bg-[var(--primary)] px-4 py-2 text-sm font-semibold text-white hover:brightness-95">
          ➕ Novo post
        </Link>
      </div>
      {data.content.length === 0 && <div className="mt-4"><EmptyState>Nenhum post publicado.</EmptyState></div>}
      <ul className="mt-4 space-y-2">
        {data.content.map((p) => (
          <li key={p.id} className="flex flex-wrap items-center gap-2 rounded-xl border p-3 text-sm">
            <div className="min-w-0 flex-1">
              <p><strong>{p.titulo}</strong></p>
              <p className="text-gray-600">/{p.slug} · {p.categoria} · {formatarData(p.dataPublicacao)}</p>
            </div>
            <Link href={`/blog/${p.slug}`} className="rounded-lg border px-3 py-1.5 hover:bg-gray-50">Ver</Link>
            <Link href={`/painel/admin/blog/${p.id}/editar`} className="rounded-lg border px-3 py-1.5 hover:bg-gray-50">Editar</Link>
            <DangerButton onClick={() => void remover(p.id)}>Remover</DangerButton>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default function Page() {
  return (
    <RequireAuth tipos={["admin"]}>
      <AdminBlog />
    </RequireAuth>
  );
}
