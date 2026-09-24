"use client";

import { use, useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import { Alert, SecondaryButton, Spinner, formatarData } from "@/components/ui";
import { api } from "@/lib/api";
import type { Donation } from "@/lib/types";

function ComprovanteDoacao({ id }: { id: string }) {
  const [doacao, setDoacao] = useState<Donation | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    api<Donation>(`/donations/${id}`)
      .then(setDoacao)
      .catch(() => setErro("Comprovante não encontrado."));
  }, [id]);

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!doacao) return <Spinner />;

  return (
    <div className="mx-auto max-w-xl">
      <div className="rounded-xl border p-6">
        <h1 className="text-center text-xl font-bold">🧾 Comprovante de doação</h1>
        <p className="text-center text-sm text-gray-500">Conexões Solidárias · Curitiba/PR</p>
        <dl className="mt-4 space-y-2 text-sm">
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Tipo</dt><dd className="font-medium">Doação de itens</dd></div>
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Item</dt><dd className="font-medium">{doacao.quantidade} de {doacao.item}</dd></div>
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Campanha</dt><dd className="font-medium">{doacao.campaignTitulo}</dd></div>
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Instituição</dt><dd className="font-medium">{doacao.instituicaoNome}</dd></div>
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Doador(a)</dt><dd className="font-medium">{doacao.doadorNome}</dd></div>
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Data da intenção</dt><dd className="font-medium">{formatarData(doacao.dataIntencao)}</dd></div>
          {doacao.dataRecebimento && <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Recebido em</dt><dd className="font-medium">{formatarData(doacao.dataRecebimento)}</dd></div>}
          <div className="flex justify-between border-b py-1"><dt className="text-gray-600">Status</dt><dd className="font-medium">{doacao.status}</dd></div>
        </dl>
      </div>
      <p className="mt-4 text-center print:hidden">
        <SecondaryButton onClick={() => window.print()}>🖨️ Imprimir / salvar PDF</SecondaryButton>
      </p>
    </div>
  );
}

export default function Page({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  return (
    <RequireAuth tipos={["doador"]}>
      <ComprovanteDoacao id={id} />
    </RequireAuth>
  );
}
