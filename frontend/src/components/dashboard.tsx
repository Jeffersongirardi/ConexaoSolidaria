"use client";

import Link from "next/link";
import type { ReactNode } from "react";

/** Cabeçalho padrão dos painéis: eyebrow, título, subtítulo e ações à direita. */
export function PainelHeader({
  eyebrow,
  titulo,
  subtitulo,
  actions,
}: {
  eyebrow: string;
  titulo: string;
  subtitulo?: string;
  actions?: ReactNode;
}) {
  return (
    <div className="flex flex-wrap items-start justify-between gap-3">
      <div className="min-w-0">
        <p className="text-xs font-bold uppercase tracking-widest text-[var(--primary)]">{eyebrow}</p>
        <h1 className="mt-1 text-2xl font-bold tracking-tight sm:text-3xl">{titulo}</h1>
        {subtitulo && <p className="mt-1 text-sm text-gray-600">{subtitulo}</p>}
      </div>
      {actions && <div className="flex shrink-0 flex-wrap gap-2">{actions}</div>}
    </div>
  );
}

export type StatTone = "default" | "destaque" | "alerta" | "sucesso";

const toneClasses: Record<StatTone, string> = {
  default: "border-[var(--border)]",
  destaque: "border-[var(--primary)] ring-1 ring-[var(--primary)]",
  alerta: "border-amber-300 bg-amber-50/60",
  sucesso: "border-green-200 bg-green-50/60",
};

/** Card de estatística com ícone, rótulo e valor. */
export function StatCard({
  icone,
  rotulo,
  valor,
  tone = "default",
  href,
}: {
  icone: string;
  rotulo: string;
  valor: string;
  tone?: StatTone;
  href?: string;
}) {
  const corpo = (
    <>
      <span aria-hidden="true" className="text-2xl">{icone}</span>
      <span className="mt-1 block text-2xl font-bold tabular-nums text-[var(--text)]">{valor}</span>
      <span className="mt-0.5 block text-xs font-medium text-gray-500">{rotulo}</span>
    </>
  );
  const cls = `rounded-2xl border bg-white p-4 text-center shadow-sm transition hover:shadow-md ${toneClasses[tone]}`;
  return href ? (
    <Link href={href} className={cls} aria-label={`${rotulo}: ${valor}`}>
      {corpo}
    </Link>
  ) : (
    <div className={cls}>{corpo}</div>
  );
}

/** Grade responsiva de estatísticas. */
export function StatGrid({ children }: { children: ReactNode }) {
  return <dl className="mt-5 grid grid-cols-2 gap-3 lg:grid-cols-4">{children}</dl>;
}

/** Título de seção com ação opcional à direita (“Ver todas →”). */
export function SecaoTitulo({
  id,
  titulo,
  acao,
}: {
  id: string;
  titulo: string;
  acao?: { href: string; rotulo: string };
}) {
  return (
    <div className="mb-4 flex items-center justify-between gap-2">
      <h2 id={id} className="text-lg font-bold tracking-tight">{titulo}</h2>
      {acao && (
        <Link href={acao.href} className="shrink-0 text-sm font-medium text-[var(--primary)] hover:underline">
          {acao.rotulo}
        </Link>
      )}
    </div>
  );
}
