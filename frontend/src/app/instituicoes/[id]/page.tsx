"use client";

import { use, useEffect, useState } from "react";
import CampaignCard from "@/components/CampaignCard";
import { Alert, Spinner } from "@/components/ui";
import { api, fileUrl } from "@/lib/api";
import type { Institution } from "@/lib/types";

export default function InstituicaoDetalhe({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const [inst, setInst] = useState<Institution | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    api<Institution>(`/institutions/${id}`, { token: null })
      .then(setInst)
      .catch(() => setErro("Instituição não encontrada."));
  }, [id]);

  if (erro) return <div className="mt-4"><Alert kind="error">{erro}</Alert></div>;
  if (!inst) return <Spinner />;

  const foto = fileUrl(inst.fotoUrl);

  return (
    <div>
      <div className="flex flex-col gap-4 rounded-2xl border p-6 sm:flex-row">
        {foto ? (
          <img src={foto} alt={`Foto de ${inst.nomeFantasia || inst.razaoSocial}`} className="h-40 w-40 rounded-xl object-cover" />
        ) : (
          <div aria-hidden="true" className="flex h-40 w-40 items-center justify-center rounded-xl bg-blue-50 text-6xl">🏠</div>
        )}
        <div>
          <h1 className="text-2xl font-bold">{inst.nomeFantasia || inst.razaoSocial}</h1>
          <p className="text-sm text-gray-500">{inst.razaoSocial} · CNPJ {inst.cnpj}</p>
          <dl className="mt-2 grid gap-1 text-sm text-gray-700 sm:grid-cols-2">
            {inst.endereco && <div><dt className="inline font-medium">Endereço: </dt><dd className="inline">{inst.endereco}</dd></div>}
            {(inst.cidade || inst.estado) && <div><dt className="inline font-medium">Cidade: </dt><dd className="inline">{inst.cidade}{inst.cidade && inst.estado ? "/" : ""}{inst.estado}</dd></div>}
            {inst.whatsapp && <div><dt className="inline font-medium">WhatsApp: </dt><dd className="inline">{inst.whatsapp}</dd></div>}
            {inst.website && <div><dt className="inline font-medium">Site: </dt><dd className="inline"><a href={inst.website} target="_blank" rel="noopener noreferrer" className="text-blue-700 hover:underline">{inst.website}</a></dd></div>}
            {inst.categoriaAtuacao && <div><dt className="inline font-medium">Atuação: </dt><dd className="inline">{inst.categoriaAtuacao}</dd></div>}
          </dl>
        </div>
      </div>

      {inst.descricao && (
        <section aria-label="Sobre a instituição" className="mt-6">
          <h2 className="text-lg font-bold">Sobre</h2>
          <p className="mt-2 whitespace-pre-line text-gray-700">{inst.descricao}</p>
        </section>
      )}

      <section aria-labelledby="campanhas-inst" className="mt-6">
        <h2 id="campanhas-inst" className="text-lg font-bold">Campanhas ativas</h2>
        {!inst.campanhas || inst.campanhas.length === 0 ? (
          <p className="mt-2 text-sm text-gray-600">Nenhuma campanha ativa no momento.</p>
        ) : (
          <div className="mt-3 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {inst.campanhas.map((c) => <CampaignCard key={c.id} campaign={c} />)}
          </div>
        )}
      </section>
    </div>
  );
}
