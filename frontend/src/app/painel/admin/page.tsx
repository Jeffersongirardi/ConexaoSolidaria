"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import { Alert, Spinner } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";

function AdminDashboard() {
  const [stats, setStats] = useState<Record<string, number> | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    api<Record<string, number>>("/admin/dashboard")
      .then(setStats)
      .catch((err) => setErro((err as ApiError).message));
  }, []);

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!stats) return <Spinner />;

  const cards: [string, string, string][] = [
    ["Usuários", String(stats.usuarios ?? 0), "/painel/admin/usuarios"],
    ["Instituições", String(stats.instituicoes ?? 0), "/painel/admin/instituicoes"],
    ["Cadastros pendentes", String(stats.pendentes ?? 0), "/painel/admin/instituicoes?filtro=pendentes"],
    ["Doações", String(stats.doacoes ?? 0), "/painel/admin/instituicoes"],
    ["Campanhas", String(stats.campanhas ?? 0), "/campanhas"],
    ["Mensagens não lidas", String(stats.mensagensNaoLidas ?? 0), "/painel/admin/mensagens"],
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold">Administração</h1>
      <div className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
        {cards.map(([rotulo, valor, href]) => (
          <Link key={rotulo} href={href} className="rounded-xl border p-4 text-center hover:border-blue-600 hover:shadow">
            <p className="text-xs text-gray-500">{rotulo}</p>
            <p className="text-2xl font-bold">{valor}</p>
          </Link>
        ))}
      </div>
      <nav aria-label="Gerenciar" className="mt-6 flex flex-wrap gap-2 text-sm">
        <Link href="/painel/admin/blog" className="rounded-lg border px-4 py-2 hover:bg-gray-50">📝 Blog</Link>
        <Link href="/notificacoes" className="rounded-lg border px-4 py-2 hover:bg-gray-50">🔔 Notificações</Link>
        <Link href="/perfil" className="rounded-lg border px-4 py-2 hover:bg-gray-50">👤 Meu perfil</Link>
      </nav>
    </div>
  );
}

export default function Page() {
  return (
    <RequireAuth tipos={["admin"]}>
      <AdminDashboard />
    </RequireAuth>
  );
}
