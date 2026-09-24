import type { MetadataRoute } from "next";

export default function manifest(): MetadataRoute.Manifest {
  return {
    name: "Conexões Solidárias",
    short_name: "Conexões",
    description:
      "Conecte-se a quem transforma doações em impacto real — campanhas de instituições validadas, com acompanhamento até a entrega.",
    start_url: "/",
    display: "standalone",
    display_override: ["standalone", "minimal-ui"],
    background_color: "#ffffff",
    theme_color: "#0d6efd",
    lang: "pt-BR",
    scope: "/",
    shortcuts: [
      {
        name: "Campanhas",
        url: "/campanhas",
        description: "Ver campanhas ativas",
        icons: [{ src: "/icons/icon-192.png", sizes: "192x192" }],
      },
      {
        name: "Meu painel",
        url: "/login",
        description: "Acessar meu painel",
        icons: [{ src: "/icons/icon-192.png", sizes: "192x192" }],
      },
    ],
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
