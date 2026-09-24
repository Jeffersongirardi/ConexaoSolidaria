"use client";

import { useCallback, useEffect, useState } from "react";
import Link from "next/link";
import RequireAuth from "@/components/RequireAuth";
import { Alert, EmptyState, SecondaryButton, Spinner, formatarData } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";
import type { Donation, Payment } from "@/lib/types";

function PainelDoador() {
  const [doacoes, setDoacoes] = useState<Donation[] | null>(null);
  const [pagamentos, setPagamentos] = useState<Payment[] | null>(null);
  const [erro, setErro] = useState("");
  const [acao, setAcao] = useState("");

  const carregar = useCallback(async () => {
    try {
      const [d, p] = await Promise.all([
        api<Donation[]>("/donations/minhas"),
        api<Payment[]>("/payments/meus"),
      ]);
      setDoacoes(d);
      setPagamentos(p);
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
      await carregar();
    } catch (err) {
      setAcao((err as ApiError).message);
    } finally {
      setAcao("");
    }
  };

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!doacoes || !pagamentos) return <Spinner />;

  const recebidas = doacoes.filter((d) => d.status === "recebido").length
    + pagamentos.filter((p) => p.status === "confirmado").length;
  const pendentes = doacoes.filter((d) => d.status === "pendente").length
    + pagamentos.filter((p) => p.status === "pendente").length;
  const valorDoado = pagamentos
    .filter((p) => p.status === "confirmado")
    .reduce((s, p) => s + Number(p.valor), 0);
  const porCategoria: Record<string, number> = {};
  for (const d of doacoes) porCategoria[d.categoria || "outro"] = (porCategoria[d.categoria || "outro"] ?? 0) + 1;

  return (
    <div>
      <h1 className="text-2xl font-bold">Meu painel</h1>
      {acao && <p role="status" className="mt-2 text-sm text-gray-600">{acao}</p>}

      <dl className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-4">
        {[
          ["Total de doações", String(doacoes.length + pagamentos.length)],
          ["Recebidas/confirmadas", String(recebidas)],
          ["Pendentes", String(pendentes)],
          ["Valor doado (R$)", valorDoado.toFixed(2)],
        ].map(([rotulo, valor]) => (
          <div key={rotulo} className="rounded-xl border p-3 text-center">
            <dt className="text-xs text-gray-500">{rotulo}</dt>
            <dd className="text-xl font-bold">{valor}</dd>
          </div>
        ))}
      </dl>

      {Object.keys(porCategoria).length > 0 && (
        <p className="mt-3 text-sm text-gray-600">
          Por categoria: {Object.entries(porCategoria).map(([c, n]) => `${c.replace("_", " ")} (${n})`).join(" · ")}
        </p>
      )}

      <section aria-labelledby="minhas-doacoes" className="mt-8">
        <h2 id="minhas-doacoes" className="text-lg font-bold">Doações de itens</h2>
        {doacoes.length === 0 ? (
          <div className="mt-2"><EmptyState>Nenhuma doação ainda. <Link href="/campanhas" className="text-blue-700 underline">Ver campanhas</Link>.</EmptyState></div>
        ) : (
          <ul className="mt-2 space-y-2">
            {doacoes.map((d) => (
              <li key={d.id} className="rounded-xl border p-3 text-sm">
                <p><strong>{d.quantidade} de {d.item}</strong> → {d.campaignTitulo} ({d.instituicaoNome})</p>
                <p className="mt-1 text-gray-600">
                  Status: <strong>{d.status === "recebido" ? "✅ Recebido" : d.status === "cancelado" ? "❌ Cancelado" : "⏳ Pendente"}</strong> · {formatarData(d.dataIntencao)}
                </p>
                {d.updates?.length > 0 && (
                  <ul className="mt-2 space-y-1 border-l-2 border-blue-200 pl-3">
                    {d.updates.map((u) => <li key={u.id}>💬 {u.mensagem} <span className="text-gray-500">({formatarData(u.dataCriacao)})</span></li>)}
                  </ul>
                )}
                <p className="mt-2 flex flex-wrap gap-2">
                  {d.status === "pendente" && <SecondaryButton onClick={() => void cancelar(d.id)}>Cancelar</SecondaryButton>}
                  <Link href={`/comprovante/doacao/${d.id}`} className="rounded-lg border border-gray-300 px-4 py-2 text-sm hover:bg-gray-50">🧾 Comprovante</Link>
                </p>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section aria-labelledby="meus-pagamentos" className="mt-8">
        <h2 id="meus-pagamentos" className="text-lg font-bold">Contribuições financeiras</h2>
        {pagamentos.length === 0 ? (
          <div className="mt-2"><EmptyState>Nenhuma contribuição financeira.</EmptyState></div>
        ) : (
          <ul className="mt-2 space-y-2">
            {pagamentos.map((p) => (
              <li key={p.uuid} className="rounded-xl border p-3 text-sm">
                <p><strong>R$ {Number(p.valor).toFixed(2)}</strong> via {p.metodo} → {p.instituicaoNome}{p.campaignTitulo ? ` (${p.campaignTitulo})` : ""}</p>
                <p className="mt-1 text-gray-600">Status: <strong>{p.status === "confirmado" ? "✅ Confirmado" : "⏳ Pendente"}</strong> · {formatarData(p.dataCriacao)}</p>
                <p className="mt-2 flex flex-wrap gap-2">
                  {p.status === "pendente" && <Link href={`/pagamento/${p.uuid}`} className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700">Concluir pagamento</Link>}
                  <Link href={`/pagamento/${p.uuid}/comprovante`} className="rounded-lg border border-gray-300 px-4 py-2 text-sm hover:bg-gray-50">🧾 Comprovante</Link>
                </p>
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
