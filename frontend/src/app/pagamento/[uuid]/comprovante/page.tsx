"use client";

import { use, useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import { Alert, SecondaryButton, Spinner } from "@/components/ui";
import { api } from "@/lib/api";

function ComprovantePagamento({ uuid }: { uuid: string }) {
  const [dados, setDados] = useState<Record<string, string> | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    api<Record<string, string>>(`/payments/${uuid}/comprovante`)
      .then(setDados)
      .catch(() => setErro("Comprovante não encontrado."));
  }, [uuid]);

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!dados) return <Spinner />;

  return (
    <div className="mx-auto max-w-xl">
      <div className="rounded-xl border p-6">
        <h1 className="text-center text-xl font-bold">🧾 Comprovante de doação</h1>
        <p className="text-center text-sm text-gray-500">Conexões Solidárias · Curitiba/PR</p>
        <dl className="mt-4 space-y-2 text-sm">
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Tipo</dt><dd className="font-medium">Doação financeira</dd></div>
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Valor</dt><dd className="font-medium">R$ {Number(dados.valor).toFixed(2)}</dd></div>
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Método</dt><dd className="font-medium">{dados.metodo}</dd></div>
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Instituição</dt><dd className="font-medium">{dados.instituicao}</dd></div>
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Data</dt><dd className="font-medium">{dados.data}</dd></div>
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Status</dt><dd className="font-medium">{dados.status}</dd></div>
          {dados.transacaoId && <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Transação</dt><dd className="font-medium">{dados.transacaoId}</dd></div>}
        </dl>
      </div>
      <p className="mt-4 text-center print:hidden">
        <SecondaryButton onClick={() => window.print()}>🖨️ Imprimir / salvar PDF</SecondaryButton>
      </p>
    </div>
  );
}

export default function Page({ params }: { params: Promise<{ uuid: string }> }) {
  const { uuid } = use(params);
  return (
    <RequireAuth tipos={["doador"]}>
      <ComprovantePagamento uuid={uuid} />
    </RequireAuth>
  );
}
