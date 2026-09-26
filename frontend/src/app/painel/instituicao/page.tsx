"use client";

import { useCallback, useEffect, useState, useMemo } from "react";
import Link from "next/link";
import RequireAuth from "@/components/RequireAuth";
import { Alert, DangerButton, EmptyState, PrimaryButton, SecondaryButton, Spinner, formatarData } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { toast } from "sonner";
import type { Campaign, Donation, Institution, Payment } from "@/lib/types";

const DashboardSkeleton = () => {
  return (
    <div className="space-y-6 animate-pulse">
      <div className="h-8 w-48 rounded bg-gray-200" />
      <dl className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div key={i} className="h-20 rounded-xl border bg-gray-100" />
        ))}
      </dl>
      <div className="h-6 w-64 rounded bg-gray-200" />
      <ul className="space-y-2">
        {Array.from({ length: 4 }).map((_, i) => (
          <li key={i} className="h-14 rounded-xl border bg-gray-100" />
        ))}
      </ul>
      <div className="h-6 w-64 rounded bg-gray-200" />
      <ul className="space-y-2">
        {Array.from({ length: 3 }).map((_, i) => (
          <li key={i} className="h-14 rounded-xl border bg-gray-100" />
        ))}
      </ul>
    </div>
  );
};

function PainelInstituicao() {
  const { user } = useAuth();
  const [perfil, setPerfil] = useState<Institution | null>(null);
  const [campanhas, setCampanhas] = useState<Campaign[] | null>(null);
  const [doacoes, setDoacoes] = useState<Donation[] | null>(null);
  const [pagamentos, setPagamentos] = useState<Payment[] | null>(null);
  const [erro, setErro] = useState("");
  const [acao, setAcao] = useState("");
  const [updateId, setUpdateId] = useState<number | null>(null);
  const [updateMsg, setUpdateMsg] = useState("");
  const [updateFoto, setUpdateFoto] = useState<File | null>(null);

  const carregar = useCallback(async () => {
    try {
      const [p, c, d, pg] = await Promise.all([
        api<Institution>("/institutions/minha"),
        api<Campaign[]>("/campaigns/minhas"),
        api<Donation[]>("/donations/recebidas"),
        api<Payment[]>("/payments/recebidos"),
      ]);
      setPerfil(p);
      setCampanhas(c);
      setDoacoes(d);
      setPagamentos(pg);
    } catch (err) {
      setErro((err as ApiError).message);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const confirmar = async (id: number) => {
    setAcao("Confirmando...");
    try {
      await api(`/donations/${id}/confirmar`, { method: "PATCH" });
      await carregar();
    } catch (err) {
      setAcao((err as ApiError).message);
    } finally {
      setAcao("");
    }
  };

  const toggle = async (id: number) => {
    try {
      await api(`/campaigns/${id}/toggle`, { method: "PATCH" });
      await carregar();
    } catch (err) {
      setAcao((err as ApiError).message);
    }
  };

  const remover = async (id: number) => {
    if (!confirm("Remover esta campanha?")) return;
    try {
      await api(`/campaigns/${id}`, { method: "DELETE" });
      await carregar();
    } catch (err) {
      setAcao((err as ApiError).message);
    }
  };

  const enviarUpdate = async (donationId: number) => {
    if (!updateMsg.trim()) return;
    const form = new FormData();
    form.set("mensagem", updateMsg);
    if (updateFoto) form.set("foto", updateFoto);
    try {
      await api(`/donations/${donationId}/atualizacoes`, { method: "POST", form });
      setUpdateId(null);
      setUpdateMsg("");
      setUpdateFoto(null);
      await carregar();
    } catch (err) {
      setAcao((err as ApiError).message);
    }
  };

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!perfil || !campanhas || !doacoes || !pagamentos) return <Spinner />;

  if (!perfil.aprovado) {
    return (
      <div className="mx-auto max-w-xl text-center">
        <p aria-hidden="true" className="text-5xl">⏳</p>
        <h1 className="mt-4 text-2xl font-bold">Cadastro em análise</h1>
        <p className="mt-2 text-gray-600">
          Olá, {user?.nome}! Sua instituição <strong>{perfil.razaoSocial}</strong> está
          aguardando aprovação de um administrador.
          {perfil.motivoRecusa && <> Motivo informado: {perfil.motivoRecusa}</>}
        </p>
        <div className="mt-6 flex justify-center gap-2">
          <Link href="/painel/instituicao/perfil" className="rounded-lg bg-[var(--primary)] px-4 py-2 text-sm font-semibold text-white hover:brightness-95">
            Editar perfil
          </Link>
          <Link href="/contato" className="rounded-lg border border-[var(--border)] px-4 py-2 text-sm hover:bg-gray-50">
            Falar com suporte
          </Link>
        </div>
      </div>
    );
  }

  const pendentes = doacoes.filter((d) => d.status === "pendente").length;
  const valorRecebido = pagamentos.filter((p) => p.status === "confirmado")
    .reduce((s, p) => s + Number(p.valor), 0);

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold">Painel da instituição</h1>
        <Link href="/painel/instituicao/campanhas/nova" className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700">
          ➕ Nova campanha
        </Link>
      </div>
      {acao && <p role="status" className="mt-2 text-sm text-gray-600">{acao}</p>}

      <dl className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
        {[
          ["Campanhas ativas", String(campanhas.filter((c) => c.ativo).length)],
          ["Doações pendentes", String(pendentes)],
          ["Doações recebidas", String(doacoes.filter((d) => d.status === "recebido").length)],
          ["Valor recebido (R$)", valorRecebido.toFixed(2)],
        ].map(([rotulo, valor]) => (
          <div key={rotulo} className="rounded-xl border p-3 text-center">
            <dt className="text-xs text-gray-500">{rotulo}</dt>
            <dd className="text-xl font-bold">{valor}</dd>
          </div>
        ))}
      </dl>

      <section aria-labelledby="minhas-campanhas" className="mt-8">
        <h2 id="minhas-campanhas" className="text-lg font-bold">Minhas campanhas</h2>
        {campanhas.length === 0 ? (
          <div className="mt-2"><EmptyState>Nenhuma campanha ainda. Publique a primeira!</EmptyState></div>
        ) : (
          <ul className="mt-2 space-y-2">
            {campanhas.map((c) => (
              <li key={c.id} className="flex flex-wrap items-center gap-2 rounded-xl border p-3 text-sm">
                <div className="min-w-0 flex-1">
                  <p><strong>{c.titulo}</strong> {c.ativo ? "(ativa)" : "(pausada)"}</p>
                  <p className="text-gray-600">{c.quantidadeAlvo} · progresso {c.progresso ?? 0}% · {c.imagens?.length ?? 0} foto(s)</p>
                </div>
                <Link href={`/campanhas/${c.id}`} className="rounded-lg border px-3 py-1.5 hover:bg-gray-50">Ver</Link>
                <Link href={`/painel/instituicao/campanhas/${c.id}/editar`} className="rounded-lg border px-3 py-1.5 hover:bg-gray-50">Editar</Link>
                <SecondaryButton onClick={() => void toggle(c.id)}>{c.ativo ? "Pausar" : "Ativar"}</SecondaryButton>
                <DangerButton onClick={() => void remover(c.id)}>Remover</DangerButton>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section aria-labelledby="doacoes-recebidas" className="mt-8">
        <h2 id="doacoes-recebidas" className="text-lg font-bold">Intenções de doação recebidas</h2>
        {doacoes.length === 0 ? (
          <div className="mt-2"><EmptyState>Nenhuma intenção de doação ainda.</EmptyState></div>
        ) : (
          <ul className="mt-2 space-y-2">
            {doacoes.map((d) => (
              <li key={d.id} className="rounded-xl border p-3 text-sm">
                <p><strong>{d.doadorNome}</strong> quer doar <strong>{d.quantidade} de {d.item}</strong> para “{d.campaignTitulo}”</p>
                <p className="text-gray-600">Status: <strong>{d.status}</strong> · {formatarData(d.dataIntencao)}{d.observacao ? ` · " ${d.observacao}"` : ""}</p>
                <p className="mt-2 flex flex-wrap gap-2">
                  {d.status === "pendente" && <PrimaryButton onClick={() => void confirmar(d.id)}>Confirmar recebimento</PrimaryButton>}
                  <SecondaryButton onClick={() => { setUpdateId(updateId === d.id ? null : d.id); setUpdateMsg(""); }}>
                    {updateId === d.id ? "Fechar" : "Enviar atualização"}
                  </SecondaryButton>
                </p>
                {updateId === d.id && (
                  <div className="mt-2 space-y-2 rounded-lg bg-gray-50 p-3">
                    <label htmlFor={`msg-${d.id}`} className="text-xs font-medium">Mensagem ao doador</label>
                    <textarea id={`msg-${d.id}`} rows={2} value={updateMsg} onChange={(e) => setUpdateMsg(e.target.value)} className="w-full rounded-lg border px-3 py-2 text-sm" />
                    <label htmlFor={`foto-${d.id}`} className="text-xs font-medium">Foto (opcional)</label>
                    <input id={`foto-${d.id}`} type="file" accept="image/*" onChange={(e) => setUpdateFoto(e.target.files?.[0] ?? null)} className="text-sm" />
                    <PrimaryButton onClick={() => void enviarUpdate(d.id)}>Enviar</PrimaryButton>
                  </div>
                )}
              </li>
            ))}
          </ul>
        )}
      </section>

      {pagamentos.length > 0 && (
        <section aria-labelledby="valores-recebidos" className="mt-8">
          <h2 id="valores-recebidos" className="text-lg font-bold">Contribuições financeiras recebidas</h2>
          <ul className="mt-2 space-y-2">
            {pagamentos.map((p) => (
              <li key={p.uuid} className="flex flex-wrap items-center gap-2 rounded-xl border p-3 text-sm">
                <span className="flex-1"><strong>R$ {Number(p.valor).toFixed(2)}</strong> via {p.metodo} · {p.status} · {formatarData(p.dataCriacao)}{p.campaignTitulo && <> · {p.campaignTitulo}</>}</span>
                {p.status !== "recebido" && p.status !== "cancelado" && (
                  <PrimaryButton onClick={async () => { try { await api(`/payments/${p.uuid}/confirmar-recebimento`, { method: "PATCH" }); await carregar(); } catch (e) { setAcao((e as ApiError).message); }}}>
                    Confirmar recebimento
                  </PrimaryButton>
                )}
                <Link href={`/pagamento/${p.uuid}/comprovante`} className="rounded-lg border px-3 py-1.5 hover:bg-gray-50">Comprovante</Link>
              </li>
            ))}
          </ul>
        </section>
      )}

      <nav aria-label="Atalhos" className="mt-8 flex flex-wrap gap-2 text-sm">
        <Link href="/painel/instituicao/perfil" className="rounded-lg border px-4 py-2 hover:bg-gray-50">🏠 Perfil da instituição</Link>
        <Link href="/perfil" className="rounded-lg border px-4 py-2 hover:bg-gray-50">👤 Meu perfil</Link>
        <Link href="/notificacoes" className="rounded-lg border px-4 py-2 hover:bg-gray-50">🔔 Notificações</Link>
      </nav>
    </div>
  );
}

export default function Page() {
  return (
    <RequireAuth tipos={["instituicao"]}>
      <PainelInstituicao />
    </RequireAuth>
  );
}