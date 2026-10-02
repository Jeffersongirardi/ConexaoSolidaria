"use client";

import { useRouter } from "next/navigation";
import RequireAuth from "@/components/RequireAuth";
import Voltar from "@/components/Voltar";
import OfertaForm from "@/components/OfertaForm";
import { toast } from "sonner";

export default function NovaOfertaPage() {
  const router = useRouter();
  return (
    <RequireAuth tipos={["doador"]}>
      <div className="mx-auto max-w-2xl">
        <Voltar fallback="/painel/doador" />
        <h1 className="mt-2 text-2xl font-bold">Ofertar um item</h1>
        <p className="mt-1 text-sm text-gray-600">
          Ex.: sofá, piano, violão, móveis. Seja completo: <strong>você se compromete a manter o item disponível até a data informada</strong>.
        </p>
        <div className="mt-4">
          <OfertaForm
            onSalvo={() => { toast.success("Oferta publicada! Aguarde a aprovação do admin."); router.push("/painel/doador"); }}
          />
        </div>
        <p className="mt-2 text-xs text-gray-500">
          Ao publicar, você se compromete a disponibilizar o item até a data informada. Se uma instituição reivindicar, ela tem 7 dias para coletar.
        </p>
      </div>
    </RequireAuth>
  );
}
