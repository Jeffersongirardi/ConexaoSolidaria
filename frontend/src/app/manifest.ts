import type { MetadataRoute } from "next";

export default function manifest(): MetadataRoute.Manifest {
  return {
    name: "Conexões Solidárias",
    short_name: "Conexões",
    description:
      "Plataforma que conecta doadores a instituições de caridade em Curitiba/PR.",
    start_url: "/",
    display: "standalone",
    background_color: "#ffffff",
    theme_color: "#0d6efd",
    lang: "pt-BR",
    icons: [
      {
        src: "/icons/icon-192.png",
        sizes: "192x192",
        type: "image/png",
      },
      {
        src: "/icons/icon-512.png",
        sizes: "512x512",
        type: "image/png",
      },
      {
        src: "/icons/maskable-512.png",
        sizes: "512x512",
        type: "image/png",
        purpose: "maskable",
      },
    ],
  };
}
