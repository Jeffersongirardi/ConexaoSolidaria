"use client";

import { use, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import RequireAuth from "@/components/RequireAuth";
import Voltar from "@/components/Voltar";
import OfertaForm from "@/components/OfertaForm";
import { Alert, Spinner } from "@/components/ui";
import { api } from "@/lib/api";
import type { Oferta } from "@/lib/types";
import { toast } from "sonner";

function EditarConteudo({ id }: { id: string }) {
  const router = useRouter();
  const [oferta, setOferta] = useState<Oferta | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    api<Oferta>(`/ofertas/${id}`)
      .then(setOferta)
      .catch(() => setErro("Oferta não encontrada ou sem permissão."));
  }, [id]);

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!oferta) return <Spinner />;

  if (oferta.status !== "disponivel") {
    return (
      <div className="mx-auto max-w-3xl">
        <h1 className="text-2xl font-bold">Editar oferta</h1>
        <div className="mt-4"><Alert kind="error">Só ofertas disponíveis podem ser editadas. Libere a reserva ou aguarde a coleta antes de editar.</Alert></div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl">
      <Voltar fallback="/painel/doador" rotulo="Voltar às minhas ofertas" />
      <h1 className="mt-2 text-2xl font-bold">Editar oferta</h1>
      <div className="mt-4">
        <OfertaForm
          inicial={oferta}
          textoBotao="Salvar alterações"
          onSalvo={() => { toast.success("Alterações salvas!"); router.push("/painel/doador#minhas-ofertas"); }}
        />
      </div>
    </div>
  );
}

export default function EditarOfertaPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  return (
    <RequireAuth tipos={["doador"]}>
      <EditarConteudo id={id} />
    </RequireAuth>
  );
}
