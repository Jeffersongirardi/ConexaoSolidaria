"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import RequireAuth from "@/components/RequireAuth";
import SafeImage from "@/components/SafeImage";
import { Alert, Spinner } from "@/components/ui";
import { api } from "@/lib/api";
import type { Payment } from "@/lib/types";

function SucessoConteudo({ uuid }: { uuid: string }) {
  const [pagamento, setPagamento] = useState<Payment | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    api<Payment>(`/payments/${uuid}`)
      .then(setPagamento)
      .catch(() => setErro("Pagamento não encontrado."));
  }, [uuid]);

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!pagamento) return <Spinner />;

  return (
    <div className="mx-auto max-w-md text-center">
      <SafeImage src="/img/entrega-confirmada.jpg" alt="Entrega de doação confirmada" className="mx-auto h-44 w-full max-w-md rounded-2xl object-cover" loading="lazy" />
      <p aria-hidden="true" className="text-6xl">🎉</p>
      <h1 className="mt-4 text-2xl font-bold">Você fez o bem hoje!</h1>
      <p className="mt-2 text-gray-700">
        R$ {Number(pagamento.valor).toFixed(2)} via {pagamento.metodo} direto para {pagamento.instituicaoNome}.
        Compartilhe e dobre o impacto ❤️
      </p>
      {pagamento.transacaoId && <p className="mt-1 text-sm text-gray-500">Transação: {pagamento.transacaoId}</p>}
      <p className="mt-6 flex justify-center gap-2">
        <Link href={`/pagamento/${uuid}/comprovante`} className="rounded-lg border px-4 py-2 text-sm hover:bg-gray-50">🧾 Ver comprovante</Link>
        <Link href="/painel/doador" className="rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700">Meu painel</Link>
      </p>
    </div>
  );
}

export default function SucessoPage({ params }: { params: Promise<{ uuid: string }> }) {
  const { uuid } = use(params);
  return (
    <RequireAuth tipos={["doador"]}>
      <SucessoConteudo uuid={uuid} />
    </RequireAuth>
  );
}
