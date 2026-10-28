export function statusPagamentoLabel(status: string): string {
  switch (status) {
    case "pendente":
      return "Pendente";
    case "confirmado":
      return "Pix declarado (aguardando conferência)";
    case "recebido":
      return "Recebido";
    case "rejeitado":
      return "Pix não identificado";
    case "cancelado":
      return "Cancelado";
    default:
      return status;
  }
}

export function motivoPagamento(p: {
  motivoRejeicao?: string | null;
  motivoCancelamento?: string | null;
}): string | null {
  return p.motivoRejeicao ?? p.motivoCancelamento ?? null;
}
