export function statusDoacaoLabel(status: string): string {
  switch (status) {
    case "pendente":
      return "Pendente (combinar entrega)";
    case "recebido":
      return "Recebido";
    case "rejeitado":
      return "Não aceita pela instituição";
    case "cancelado":
      return "Cancelado";
    default:
      return status;
  }
}
