"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import RequireAuth from "@/components/RequireAuth";
import { Alert, EmptyState, SecondaryButton, Spinner, formatarData } from "@/components/ui";
import CampaignCard from "@/components/CampaignCard";
import { CampaignCardSkeleton } from "@/components/Skeletons";
import { toast } from "sonner";
import { api, type ApiError } from "@/lib/api";
import type { Donation, Payment, Campaign } from "@/lib/types";

function DashboardSkeleton() {
  return (
    <div className="space-y-6">
      <div className="h-8 w-48 animate-pulse rounded bg-gray-200" />
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-20 animate-pulse rounded-xl border bg-gray-100" />
        ))}
      </div>
      <div className="h-8 w-64 animate-pulse rounded bg-gray-200" />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="animate-pulse h-72 rounded-xl border bg-gray-100" />
        ))}
      </div>
    </div>
  );
}

function PainelDoador() {
  const [doacoes, setDoacoes] = useState<Donation[]>([]);
  const [pagamentos, setPagamentos] = useState<Payment[]>([]);
  const [campanhasRecentes, setCampanhasRecentes] = useState<Campaign[]>([]);
  const [erro, setErro] = useState("");
  const [acao, setAcao] = useState("");

  const carregar = useCallback(async () => {
    try {
      const [d, p, c] = await Promise.all([
        api<Donation[]>("/donations/minhas"),
        api<Payment[]>("/payments/meus"),
        api<Campaign[]>("/campaigns/destaques", { token: null }),
      ]);
      setDoacoes(d);
      setPagamentos(p);
      setCampanhasRecentes(c);
    } catch (err) {
      setErro((err as ApiError).message);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const cancelar = async (id: number) => {
    if (!confirm("Cancelar esta intenção de doação?")) return;
    setAcao("Cancelando...");
    try {
      await api(`/donations/${id}/cancelar`, { method: "PATCH" });
      toast.success("Doação cancelada");
      await carregar();
    } catch (err) {
      toast.error((err as ApiError).message);
    } finally {
      setAcao("");
    }
  };

  // Computed values
  const recebidas = doacoes!.filter((d) => d.status === "recebido").length
    + pagamentos!.filter((p) => p.status === "confirmado").length;
  const doacoesPendentes = doacoes!.filter((d) => d.status === "pendente") ?? [];
  const pagamentosPendentes = pagamentos!.filter((p) => p.status === "pendente") ?? [];
  const temPendencias = doacoesPendentes.length > 0 || pagamentosPendentes.length > 0;
  const pendentesTotal = doacoesPendentes.length + pagamentosPendentes.length;
  const pendentes = doacoes!.filter((d) => d.status === "pendente").length
    + pagamentos!.filter((p) => p.status === "pendente").length;
  const valorDoado = pagamentos!
    .filter((p) => p.status === "confirmado")
    .reduce((s, p) => s + Number(p.valor), 0);
  const porCategoria: Record<string, number> = {};
  for (const d of doacoes!) porCategoria[d.categoria || "outro"] = (porCategoria[d.categoria || "outro"] ?? 0) + 1;

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (campanhasRecentes === null) return <DashboardSkeleton />;

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold">Meu painel</h1>
        {temPendencias && (
          <div className="rounded-lg bg-amber-50 border border-amber-200 p-3 text-sm text-amber-800 flex items-center gap-2">
            <span className="text-lg">⏳</span>
            <span>Você tem <strong>{pendentesTotal}</strong> ação{pendentesTotal > 1 ? "ões" : ""} pendente{pendentesTotal > 1 ? "s" : ""}.</span>
          </div>
        )}
        {acao && <p role="status" className="mt-2 text-sm text-amber-700">{acao}</p>}
      </div>

      <dl className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
        {[
          ["Total de doações", String(doacoes.length + pagamentos.length)],
          ["Recebidas/confirmadas", String(recebidas)],
          ["Pendentes", String(pendentes)],
          ["Valor doado (R$)", valorDoado.toFixed(2)],
        ].map(([rotulo, valor]) => (
          <div key={rotulo} className="rounded-xl border p-3 text-center bg-white shadow-sm">
            <dt className="text-xs text-gray-500">{rotulo}</dt>
            <dd className="text-xl font-bold text-[var(--primary)]">{valor}</dd>
          </div>
        ))}
      </dl>

      {Object.keys(porCategoria).length > 0 && (
        <p className="mt-3 text-sm text-gray-600">
          Por categoria: {Object.entries(porCategoria).map(([c, n]) => `${c.replace("_", " ")} (${n})`).join(" · ")}
        </p>
      )}

      {/* Campanhas Recentes */}
      <section aria-labelledby="campanhas-recentes" className="mt-8">
        <div className="flex items-center justify-between mb-4">
          <h2 id="campanhas-recentes" className="text-lg font-bold">Campanhas Recentes</h2>
          <Link href="/campanhas" className="text-sm text-[var(--primary)] hover:underline">Ver todas →</Link>
        </div>
        {campanhasRecentes?.length === 0 ? (
          <EmptyState>
            Nenhuma campanha ativa no momento. <Link href="/campanhas" className="text-[var(--primary)] underline">Explorar campanhas</Link>.
          </EmptyState>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {campanhasRecentes.map((c) => <CampaignCard key={c.id} campaign={c} />)}
          </div>
        )}
      </section>

      {/* Continue onde parou */}
      {temPendencias && (
        <section aria-labelledby="continue-onde-parou" className="mt-8">
          <h2 id="continue-onde-parou" className="text-lg font-bold">Continue onde parou</h2>
          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            {doacoesPendentes.length > 0 && (
              <Link href="/painel/doador#minhas-doacoes" className="rounded-xl border border-amber-200 bg-amber-50 p-4 hover:border-amber-300 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-lg bg-amber-100 flex items-center justify-center">⏳</div>
                  <div>
                    <p className="font-semibold text-amber-800">{doacoesPendentes.length} doação{doacoesPendentes.length > 1 ? "s" : ""} pendente{doacoesPendentes.length > 1 ? "s" : ""}</p>
                    <p className="text-sm text-amber-700">Clique para cancelar ou acompanhar</p>
                  </div>
                </div>
              </Link>
            )}
            {pagamentosPendentes.length > 0 && (
              <Link href="/painel/doador#meus-pagamentos" className="rounded-xl border border-blue-200 bg-blue-50 p-4 hover:border-blue-300 transition-colors">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-lg bg-blue-100 flex items-center justify-center">💰</div>
                  <div>
                    <p className="font-semibold text-blue-800">{pagamentosPendentes.length} pagamento{pagamentosPendentes.length > 1 ? "s" : ""} pendente{pagamentosPendentes.length > 1 ? "s" : ""}</p>
                    <p className="text-sm text-blue-700">Clique para concluir pagamento</p>
                  </div>
                </div>
              </Link>
            )}
          </div>
        </section>
      )}

      {Object.keys(porCategoria).length > 0 && (
        <p className="mt-3 text-sm text-gray-600">
          Por categoria: {Object.entries(porCategoria).map(([c, n]) => `${c.replace("_", " ")} (${n})`).join(" · ")}
        </p>
      )}

      <section aria-labelledby="minhas-doacoes" className="mt-8">
        <div className="flex items-center justify-between mb-4">
          <h2 id="minhas-doacoes" className="text-lg font-bold">Doações de itens</h2>
          <Link href="/campanhas" className="text-sm text-[var(--primary)] hover:underline">Ver campanhas →</Link>
        </div>
        {doacoes.length === 0 ? (
          <EmptyState>
            Nenhuma doação ainda. <Link href="/campanhas" className="text-[var(--primary)] underline">Descubra campanhas</Link> e faça sua primeira doação!
          </EmptyState>
        ) : (
          <ul className="space-y-2">
            {doacoes.map((d) => (
              <li key={d.id} className="rounded-xl border p-3 text-sm hover:shadow-sm transition-shadow">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <p><strong>{d.quantidade} de {d.item}</strong> → {d.campaignTitulo} ({d.instituicaoNome})</p>
                    <p className="mt-1 text-gray-600">
                      Status: <strong>{d.status === "recebido" ? "✅ Recebido" : d.status === "cancelado" ? "❌ Cancelado" : "⏳ Pendente"}</strong> · {formatarData(d.dataIntencao)}
                    </p>
                    {d.updates?.length > 0 && (
                      <ul className="mt-2 space-y-1 border-l-2 border-[var(--primary-light)] pl-3">
                        {d.updates.map((u) => <li key={u.id} className="text-sm">💬 {u.mensagem} <span className="text-gray-500">({formatarData(u.dataCriacao)})</span></li>)}
                      </ul>
                    )}
                  </div>
                  <div className="flex flex-wrap gap-2 shrink-0">
                    {d.status === "pendente" && (
                      <SecondaryButton onClick={() => void cancelar(d.id)} size="sm">Cancelar</SecondaryButton>
                    )}
                    <Link href={`/comprovante/doacao/${d.id}`} className="rounded-lg border border-[var(--border)] px-4 py-2 text-sm hover:bg-gray-50">🧾 Comprovante</Link>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section aria-labelledby="meus-pagamentos" className="mt-8">
        <div className="flex items-center justify-between mb-4">
          <h2 id="meus-pagamentos" className="text-lg font-bold">Contribuições financeiras</h2>
          <Link href="/campanhas" className="text-sm text-[var(--primary)] hover:underline">+ Nova contribuição</Link>
        </div>
        {pagamentos.length === 0 ? (
          <EmptyState>
            Nenhuma contribuição financeira ainda. <Link href="/campanhas" className="text-[var(--primary)] underline">Explore campanhas</Link> que aceitam contribuições.
          </EmptyState>
        ) : (
          <ul className="space-y-2">
            {pagamentos.map((p) => (
              <li key={p.uuid} className="rounded-xl border p-3 text-sm hover:shadow-sm transition-shadow">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0 flex-1">
                    <p><strong>R$ {Number(p.valor).toFixed(2)}</strong> via {p.metodo} → {p.instituicaoNome}{p.campaignTitulo ? ` (${p.campaignTitulo})` : ""}</p>
                    <p className="mt-1 text-gray-600">Status: <strong>{p.status === "confirmado" ? "✅ Confirmado" : "⏳ Pendente"}</strong> · {formatarData(p.dataCriacao)}</p>
                  </div>
                  <div className="flex flex-wrap gap-2 shrink-0">
                    {p.status === "pendente" && <Link href={`/pagamento/${p.uuid}`} className="rounded-lg bg-[var(--primary)] px-4 py-2 text-sm font-semibold text-white hover:brightness-95">Concluir pagamento</Link>}
                    <Link href={`/pagamento/${p.uuid}/comprovante`} className="rounded-lg border border-[var(--border)] px-4 py-2 text-sm hover:bg-gray-50">🧾 Comprovante</Link>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}
      </section>

      <nav aria-label="Atalhos" className="mt-8 flex flex-wrap gap-2 text-sm">
        <Link href="/perfil" className="rounded-lg border px-4 py-2 hover:bg-gray-50">👤 Meu perfil</Link>
        <Link href="/notificacoes" className="rounded-lg border px-4 py-2 hover:bg-gray-50">🔔 Notificações</Link>
      </nav>
    </div>
  );
}

export default function Page() {
  return (
    <RequireAuth tipos={["doador"]}>
      <PainelDoador />
    </RequireAuth>
  );
}