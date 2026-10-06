export const MOTIVOS_CANCELAMENTO = [
  { value: "doado_outro_meio", label: "Consegui doar por outro meio" },
  { value: "indisponivel", label: "Item não está mais disponível" },
  { value: "erro_anuncio", label: "Erro no anúncio" },
  { value: "desistencia", label: "Desisti da oferta" },
  { value: "outro", label: "Outro (descrever)" },
] as const;

export function motivoCancelamentoLabel(v?: string | null): string {
  return MOTIVOS_CANCELAMENTO.find((m) => m.value === v)?.label ?? v ?? "";
}

export function estadoOfertaLabel(e: string) {
  return e === "novo" ? "Novo" : e === "bom_estado" ? "Bom estado" : "Usado";
}

export const ESTADOS_OFERTA = [
  { value: "novo", label: "Novo" },
  { value: "bom_estado", label: "Bom estado" },
  { value: "usado", label: "Usado" },
] as const;

/** Dias de atraso da coleta (0 se no prazo). Prazo "yyyy-MM-dd" ou ISO. */
export function diasAtrasoColeta(prazo?: string | null): number {
  if (!prazo) return 0;
  const limite = new Date(prazo.length <= 10 ? `${prazo}T23:59:59` : prazo);
  if (Number.isNaN(limite.getTime())) return 0;
  return Math.max(0, Math.floor((Date.now() - limite.getTime()) / 86_400_000));
}

/** Data prevista da coleta (hoje + 7 dias) para o aceite. */
export function dataPrevistaColeta(): string {
  return new Date(Date.now() + 7 * 86_400_000).toLocaleDateString("pt-BR");
}
