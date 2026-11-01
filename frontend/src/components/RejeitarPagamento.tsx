"use client";

import { useState } from "react";
import { Field, PrimaryButton, SecondaryButton, TextInput } from "./ui";
import { api, type ApiError } from "@/lib/api";
import { toast } from "sonner";

/** Instituição informa por que não identificou o Pix (rejeita o pagamento). */
export default function RejeitarPagamento({ uuid, onRejeitado }: { uuid: string; onRejeitado: () => void }) {
  const [aberto, setAberto] = useState(false);
  const [motivo, setMotivo] = useState("");
  const [enviando, setEnviando] = useState(false);

  const confirmar = async () => {
    if (!motivo.trim()) {
      toast.error("Descreva o motivo para rejeitar");
      return;
    }
    setEnviando(true);
    try {
      await api(`/payments/${uuid}/rejeitar`, { method: "PATCH", body: { motivo: motivo.trim() } });
      toast.success("Pagamento rejeitado e doador avisado");
      setAberto(false);
      setMotivo("");
      onRejeitado();
    } catch (err) {
      toast.error((err as ApiError).message);
    } finally {
      setEnviando(false);
    }
  };

  if (!aberto) {
    return <SecondaryButton size="sm" onClick={() => setAberto(true)}>Não identifiquei o Pix</SecondaryButton>;
  }

  return (
    <div className="w-full space-y-2 rounded-lg bg-gray-50 p-3">
      <Field label="Motivo (o doador verá esta mensagem)" name={`motivo-rejeicao-${uuid}`}>
        <TextInput
          id={`motivo-rejeicao-${uuid}`}
          value={motivo}
          onChange={(e) => setMotivo(e.target.value)}
          placeholder="Ex.: valor não localizado no extrato"
        />
      </Field>
      <div className="flex gap-2">
        <PrimaryButton size="sm" onClick={() => void confirmar()} disabled={enviando}>
          {enviando ? "Rejeitando..." : "Confirmar rejeição"}
        </PrimaryButton>
        <SecondaryButton size="sm" onClick={() => setAberto(false)}>Voltar</SecondaryButton>
      </div>
    </div>
  );
}
