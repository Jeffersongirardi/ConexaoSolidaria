"use client";

import { useState } from "react";
import { Field, PrimaryButton, SecondaryButton, TextInput } from "./ui";
import { api, type ApiError } from "@/lib/api";
import { toast } from "sonner";

/** Instituição informa por que não pode receber a doação de item. */
export default function RejeitarDoacao({ doacaoId, onRejeitada }: { doacaoId: number; onRejeitada: () => void }) {
  const [aberto, setAberto] = useState(false);
  const [motivo, setMotivo] = useState("");
  const [enviando, setEnviando] = useState(false);

  const confirmar = async () => {
    if (!motivo.trim()) {
      toast.error("Descreva o motivo para não aceitar a doação");
      return;
    }
    setEnviando(true);
    try {
      await api(`/donations/${doacaoId}/rejeitar`, { method: "PATCH", body: { motivo: motivo.trim() } });
      toast.success("Doação recusada e doador avisado");
      setAberto(false);
      setMotivo("");
      onRejeitada();
    } catch (err) {
      toast.error((err as ApiError).message);
    } finally {
      setEnviando(false);
    }
  };

  if (!aberto) {
    return <SecondaryButton size="sm" onClick={() => setAberto(true)}>Não posso receber</SecondaryButton>;
  }

  return (
    <div className="w-full space-y-2 rounded-lg bg-gray-50 p-3">
      <Field label="Motivo (o doador verá esta mensagem)" name={`motivo-rejeicao-doacao-${doacaoId}`}>
        <TextInput
          id={`motivo-rejeicao-doacao-${doacaoId}`}
          value={motivo}
          onChange={(e) => setMotivo(e.target.value)}
          placeholder="Ex.: não temos estrutura para armazenar este item"
        />
      </Field>
      <div className="flex gap-2">
        <PrimaryButton size="sm" onClick={() => void confirmar()} disabled={enviando}>
          {enviando ? "Recusando..." : "Confirmar recusa"}
        </PrimaryButton>
        <SecondaryButton size="sm" onClick={() => setAberto(false)}>Voltar</SecondaryButton>
      </div>
    </div>
  );
}
