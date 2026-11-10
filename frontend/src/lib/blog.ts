import { fileUrl } from "@/lib/api";

const CATEGORIAS: Record<string, string> = {
  geral: "Geral",
  historias: "Histórias",
  transparencia: "Transparência",
  eventos: "Eventos",
  parcerias: "Parcerias",
};

export function categoriaBlogLabel(categoria?: string | null): string {
  if (!categoria) return "Geral";
  return CATEGORIAS[categoria] ?? categoria.charAt(0).toUpperCase() + categoria.slice(1);
}

export function tempoLeitura(conteudo?: string | null): number {
  if (!conteudo) return 1;
  const palavras = conteudo.trim().split(/\s+/).filter(Boolean).length;
  return Math.max(1, Math.ceil(palavras / 200));
}

/** Imagens públicas ficam em /img; uploads continuam vindo da API. */
export function imagemBlog(url?: string | null): string | null {
  if (!url) return null;
  if (url.startsWith("http") || url.startsWith("data:") || url.startsWith("/img/")) {
    return url;
  }
  return fileUrl(url);
}
