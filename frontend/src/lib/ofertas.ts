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
