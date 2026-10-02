"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import { PainelHeader, SecaoTitulo, StatCard, StatGrid } from "@/components/dashboard";
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

  const pendentes = stats.pendentes ?? 0;
  const naoLidas = stats.mensagensNaoLidas ?? 0;

  return (
    <div>
      <PainelHeader
        eyebrow="Painel do administrador"
        titulo="Administração ⚙️"
        subtitulo="Moderação, aprovações e conteúdo da plataforma."
      />
      <StatGrid>
        <StatCard icone="👥" rotulo="Usuários" valor={String(stats.usuarios ?? 0)} href="/painel/admin/usuarios" />
        <StatCard icone="🏠" rotulo="Instituições" valor={String(stats.instituicoes ?? 0)} href="/painel/admin/instituicoes" />
        <StatCard icone="⏳" rotulo="Cadastros pendentes" valor={String(pendentes)} tone={pendentes > 0 ? "alerta" : "default"} href="/painel/admin/instituicoes?filtro=pendentes" />
        <StatCard icone="🎁" rotulo="Doações" valor={String(stats.doacoes ?? 0)} href="/painel/admin/doacoes" />
        <StatCard icone="📣" rotulo="Campanhas" valor={String(stats.campanhas ?? 0)} href="/campanhas" />
        <StatCard icone="✉️" rotulo="Mensagens não lidas" valor={String(naoLidas)} tone={naoLidas > 0 ? "alerta" : "default"} href="/painel/admin/mensagens" />
      </StatGrid>
      <section aria-labelledby="gerenciar" className="mt-10">
        <SecaoTitulo id="gerenciar" titulo="Gerenciar" />
        <nav aria-label="Gerenciar" className="mt-4 flex flex-wrap gap-2 text-sm">
          <Link href="/painel/admin/blog" className="rounded-xl border bg-white px-4 py-2.5 shadow-sm hover:shadow">📝 Blog</Link>
          <Link href="/painel/admin/ofertas" className="rounded-xl border bg-white px-4 py-2.5 shadow-sm hover:shadow">🤝 Ofertas</Link>
          <Link href="/notificacoes" className="rounded-xl border bg-white px-4 py-2.5 shadow-sm hover:shadow">🔔 Notificações</Link>
          <Link href="/perfil" className="rounded-xl border bg-white px-4 py-2.5 shadow-sm hover:shadow">👤 Meu perfil</Link>
        </nav>
      </section>
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
