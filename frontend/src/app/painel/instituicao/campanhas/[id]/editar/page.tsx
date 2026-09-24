"use client";

import { use, useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import CampaignForm from "@/components/CampaignForm";
import { Alert, Spinner } from "@/components/ui";
import { api } from "@/lib/api";
import type { Campaign } from "@/lib/types";

function EditarConteudo({ id }: { id: string }) {
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [erro, setErro] = useState("");
  const [salvo, setSalvo] = useState(false);

  useEffect(() => {
    api<Campaign>(`/campaigns/${id}`, { token: null })
      .then(setCampaign)
      .catch(() => setErro("Campanha não encontrada ou sem permissão."));
  }, [id]);

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!campaign) return <Spinner />;

  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="text-2xl font-bold">Editar campanha</h1>
      {salvo && <div className="mt-2"><Alert kind="success">Alterações salvas!</Alert></div>}
      <div className="mt-4">
        <CampaignForm inicial={campaign} onSalvo={(c) => { setCampaign(c); setSalvo(true); }} />
      </div>
    </div>
  );
}

export default function EditarCampanhaPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  return (
    <RequireAuth tipos={["instituicao"]}>
      <EditarConteudo id={id} />
    </RequireAuth>
  );
}
