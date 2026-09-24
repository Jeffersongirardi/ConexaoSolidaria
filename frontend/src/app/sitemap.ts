import type { MetadataRoute } from "next";

export default function sitemap(): MetadataRoute.Sitemap {
  const base = process.env.NEXT_PUBLIC_SITE_URL ?? "https://conexoessolidarias.org";
  const now = new Date();
  const routes = ["", "/campanhas", "/instituicoes", "/blog", "/sobre", "/faq", "/contato", "/privacidade", "/termos"];
  return routes.map((route) => ({
    url: `${base}${route}`,
    lastModified: now,
    changeFrequency: route === "" ? "daily" : "weekly",
    priority: route === "" ? 1 : 0.6,
  }));
}
