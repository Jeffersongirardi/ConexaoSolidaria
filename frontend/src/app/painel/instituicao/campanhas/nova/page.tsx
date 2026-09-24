"use client";

import { useRouter } from "next/navigation";
import RequireAuth from "@/components/RequireAuth";
import CampaignForm from "@/components/CampaignForm";

export default function NovaCampanhaPage() {
  const router = useRouter();
  return (
    <RequireAuth tipos={["instituicao"]}>
      <div className="mx-auto max-w-3xl">
        <h1 className="text-2xl font-bold">Nova campanha</h1>
        <p className="mt-1 text-sm text-gray-600">Descreva o que sua instituição precisa. Após publicar, você pode adicionar fotos.</p>
        <div className="mt-4">
          <CampaignForm onSalvo={(c) => router.push(`/campanhas/${c.id}`)} />
        </div>
      </div>
    </RequireAuth>
  );
}
