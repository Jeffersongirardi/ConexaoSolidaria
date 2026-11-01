"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import RequireAuth from "@/components/RequireAuth";
import { Alert, PrimaryButton, SecondaryButton, Spinner, formatarData } from "@/components/ui";
import { PainelHeader } from "@/components/dashboard";
import { api, type ApiError } from "@/lib/api";
import { motivoPagamento, statusPagamentoLabel } from "@/lib/pagamentos";
import type { Payment } from "@/lib/types";
import { toast } from "sonner";

const FILTROS = [
  { value: "todas", label: "Todas" },
  { value: "pendente", label: "Pendentes" },
  { value: "confirmado", label: "Confirmados (declarados)" },
  { value: "recebido", label: "Recebidos" },
  { value: "rejeitado", label: "Rejeitados" },
  { value: "cancelado", label: "Cancelados" },
];

function AdminPagamentos() {
  const [pagamentos, setPagamentos] = useState<Payment[] | null>(null);
  const [filtro, setFiltro] = useState("todas");
  const [cancelando, setCancelando] = useState<string | null>(null);
  const [motivo, setMotivo] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState("");

  const carregar = useCallback(async () => {
    try {
      setPagamentos(await api<Payment[]>("/admin/payments"));
    } catch (err) {
      setErro((err as ApiError).message);
    }
  }, []);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const cancelar = async (uuid: string) => {
    if (!motivo.trim()) {
      toast.error("Descreva o motivo do cancelamento");
      return;
    }
    setEnviando(true);
    try {
      await api(`/admin/payments/${uuid}/cancelar`, { method: "PATCH", body: { motivo: motivo.trim() } });
      toast.success("Pagamento cancelado e doador avisado");
      setCancelando(null);
      setMotivo("");
      await carregar();
    } catch (err) {
      toast.error((err as ApiError).message);
    } finally {
      setEnviando(false);
    }
  };

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!pagamentos) return <Spinner />;

  const lista = filtro === "todas" ? pagamentos : pagamentos.filter((p) => p.status === filtro);

  return (
    <div>
      <PainelHeader
        eyebrow="Painel do administrador"
        titulo="Pagamentos Pix"
        subtitulo="Acompanhe declarações, recebimentos e cancele com motivo quando necessário."
      />
      <div className="mt-4 flex flex-wrap items-center gap-2">
        <label htmlFor="filtro-pagamentos" className="text-sm font-medium">Filtrar:</label>
        <select
          id="filtro-pagamentos"
          value={filtro}
          onChange={(e) => setFiltro(e.target.value)}
          className="rounded-lg border border-gray-300 px-3 py-2 text-sm"
        >
          {FILTROS.map((f) => <option key={f.value} value={f.value}>{f.label}</option>)}
        </select>
        <Link href="/painel/admin" className="ml-auto text-sm text-[var(--primary)] underline">Voltar ao painel</Link>
      </div>

      {lista.length === 0 ? (
        <p className="mt-6 text-sm text-gray-600">Nenhum pagamento neste filtro.</p>
      ) : (
        <ul className="mt-4 space-y-2">
          {lista.map((p) => (
            <li key={p.uuid} className="rounded-xl border p-3 text-sm">
              <div className="flex flex-wrap items-start justify-between gap-2">
                <div className="min-w-0 flex-1">
                  <p>
                    <strong>R$ {Number(p.valor).toFixed(2)}</strong> · {statusPagamentoLabel(p.status)} · {formatarData(p.dataCriacao)}
                  </p>
                  <p className="text-gray-600">
                    Doador: {p.doadorNome ?? "—"} · Instituição: {p.instituicaoNome ?? "—"}
                    {p.campaignTitulo && <> · {p.campaignTitulo}</>}
                  </p>
                  <p className="text-xs text-gray-500">
                    {p.dataConfirmacao && <>Declarado em {formatarData(p.dataConfirmacao)} · </>}
                    {p.dataRecebimento && <>Recebido em {formatarData(p.dataRecebimento)} · </>}
                    {p.transacaoId ?? "sem transação"}
                  </p>
                  {motivoPagamento(p) && <p className="mt-1 text-xs text-red-700">Motivo: {motivoPagamento(p)}</p>}
                </div>
                {(p.status === "pendente" || p.status === "confirmado") && cancelando !== p.uuid && (
                  <SecondaryButton size="sm" onClick={() => { setCancelando(p.uuid); setMotivo(""); }}>
                    Cancelar pagamento
                  </SecondaryButton>
                )}
              </div>
              {cancelando === p.uuid && (
                <div className="mt-3 space-y-2 rounded-lg bg-gray-50 p-3">
                  <label htmlFor={`motivo-${p.uuid}`} className="block text-sm font-medium">Motivo do cancelamento (o doador verá)</label>
                  <input
                    id={`motivo-${p.uuid}`}
                    value={motivo}
                    onChange={(e) => setMotivo(e.target.value)}
                    placeholder="Ex.: pagamento duplicado / fraude confirmada"
                    className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm"
                  />
                  <div className="flex gap-2">
                    <PrimaryButton size="sm" onClick={() => void cancelar(p.uuid)} disabled={enviando}>
                      {enviando ? "Cancelando..." : "Confirmar cancelamento"}
                    </PrimaryButton>
                    <SecondaryButton size="sm" onClick={() => { setCancelando(null); setMotivo(""); }}>Voltar</SecondaryButton>
                  </div>
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default function Page() {
  return (
    <RequireAuth tipos={["admin"]}>
      <AdminPagamentos />
    </RequireAuth>
  );
}
