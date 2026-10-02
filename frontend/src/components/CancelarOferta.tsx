"use client";

import { useState } from "react";
import { Field, PrimaryButton, SecondaryButton, Select, TextInput } from "./ui";
import { api, type ApiError } from "@/lib/api";
import { MOTIVOS_CANCELAMENTO } from "@/lib/ofertas";
import { toast } from "sonner";

export default function CancelarOferta({ ofertaId, onCancelado, compact = false }: { ofertaId: number; onCancelado: () => void; compact?: boolean }) {
  const [aberto, setAberto] = useState(false);
  const [motivo, setMotivo] = useState("");
  const [detalhe, setDetalhe] = useState("");
  const [enviando, setEnviando] = useState(false);

  const confirmar = async () => {
    if (!motivo) {
      toast.error("Escolha um motivo para cancelar");
      return;
    }
    if (motivo === "outro" && !detalhe.trim()) {
      toast.error("Descreva o motivo do cancelamento");
      return;
    }
    setEnviando(true);
    try {
      await api(`/ofertas/${ofertaId}/cancelar`, { method: "PATCH", body: { motivo, detalhe } });
      toast.success("Oferta cancelada");
      setAberto(false);
      onCancelado();
    } catch (err) {
      toast.error((err as ApiError).message);
    } finally {
      setEnviando(false);
    }
  };

  if (!aberto) {
    return compact ? (
      <button onClick={() => setAberto(true)} className="px-1 py-1.5 text-xs text-gray-500 underline hover:text-red-700">
        Cancelar oferta
      </button>
    ) : (
      <SecondaryButton size="sm" onClick={() => setAberto(true)}>Cancelar</SecondaryButton>
    );
  }

  return (
    <div className="w-full space-y-2 rounded-lg bg-gray-50 p-3">
      <Field label="Motivo do cancelamento" name={`motivo-${ofertaId}`}>
        <Select id={`motivo-${ofertaId}`} value={motivo} onChange={(e) => setMotivo(e.target.value)}>
          <option value="">Selecione...</option>
          {MOTIVOS_CANCELAMENTO.map((m) => <option key={m.value} value={m.value}>{m.label}</option>)}
        </Select>
      </Field>
      {motivo === "outro" && (
        <Field label="Descreva o motivo" name={`detalhe-${ofertaId}`}>
          <TextInput id={`detalhe-${ofertaId}`} value={detalhe} onChange={(e) => setDetalhe(e.target.value)} placeholder="Ex.: mudei de cidade" />
        </Field>
      )}
      <div className="flex gap-2">
        <PrimaryButton size="sm" onClick={() => void confirmar()} disabled={enviando}>
          {enviando ? "Cancelando..." : "Confirmar cancelamento"}
        </PrimaryButton>
        <SecondaryButton size="sm" onClick={() => setAberto(false)}>Voltar</SecondaryButton>
      </div>
    </div>
  );
}
