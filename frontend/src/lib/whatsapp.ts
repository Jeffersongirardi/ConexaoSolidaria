/**
 * Normaliza telefone brasileiro para DDD + número (10 ou 11 dígitos).
 * Aceita números já com o código do país (55) e remove o prefixo.
 * Retorna null quando o número não tem um formato utilizável.
 */
export function normalizarWhatsapp(phone?: string | null): string | null {
  if (!phone) return null;

  let digits = phone.replace(/\D/g, "").replace(/^0+/, "");
  if (!digits) return null;

  if ((digits.length === 12 || digits.length === 13) && digits.startsWith("55")) {
    digits = digits.slice(2);
  }

  // Celular brasileiro tem 11 dígitos e o nono dígito é sempre 9.
  if (digits.length === 11 && digits[2] !== "9") return null;
  if (digits.length !== 10 && digits.length !== 11) return null;

  return digits;
}

/** Máscara usada nos formulários; remove o 55 de país antes de limitar o tamanho. */
export function maskPhone(value: string): string {
  let digits = value.replace(/\D/g, "");
  if (digits.length > 11 && digits.startsWith("55")) digits = digits.slice(2);
  digits = digits.slice(0, 11);

  if (digits.length <= 10) {
    return digits.replace(/(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3").trim();
  }
  return digits.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3").trim();
}

/** Link tel: para discagem, com um único código do país. */
export function telLink(phone?: string | null): string | null {
  const local = normalizarWhatsapp(phone);
  return local ? `tel:+55${local}` : null;
}

/** Link wa.me com mensagem pronta, sempre com um único código do país. */
export function waLink(phone?: string | null, message?: string): string | null {
  const local = normalizarWhatsapp(phone);
  if (!local) return null;

  const text = message ? `?text=${encodeURIComponent(message)}` : "";
  return `https://wa.me/55${local}${text}`;
}
