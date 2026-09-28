export const CATEGORIAS = [
  { value: "alimento", label: "Alimentos" },
  { value: "roupa", label: "Roupas" },
  { value: "calcado", label: "Calçados" },
  { value: "higiene", label: "Higiene" },
  { value: "fralda", label: "Fraldas e infantil" },
  { value: "material_escolar", label: "Material escolar" },
  { value: "brinquedo", label: "Brinquedos" },
  { value: "movel", label: "Móveis" },
  { value: "racao_animal", label: "Ração animal" },
  { value: "emergencia", label: "Emergência" },
  { value: "outro", label: "Outro" },
] as const;

export type CategoriaValue = (typeof CATEGORIAS)[number]["value"];

const labels = new Map<string, string>(CATEGORIAS.map((c) => [c.value, c.label]));

/** Rótulo plural para exibir; desconhecidas voltam formatadas sem quebrar. */
export function categoriaLabel(categoria?: string | null): string {
  if (!categoria) return "Outro";
  return labels.get(categoria) ?? categoria.replaceAll("_", " ");
}

/** Ícone/emoji por categoria (fallback do card sem foto). */
export function categoriaIcone(categoria?: string | null): string {
  switch (categoria) {
    case "alimento": return "🧺";
    case "roupa": return "👕";
    case "calcado": return "👟";
    case "higiene": return "🧴";
    case "fralda": return "👶";
    case "material_escolar": return "🎒";
    case "brinquedo": return "🧸";
    case "movel": return "🪑";
    case "racao_animal": return "🐾";
    case "emergencia": return "🚨";
    default: return "📦";
  }
}
