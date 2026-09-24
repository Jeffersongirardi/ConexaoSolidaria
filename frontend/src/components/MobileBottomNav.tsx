"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth, painelPorTipo } from "@/lib/auth";

const tabs = [
  { href: "/", label: "Início", icon: "🏠", match: (p: string) => p === "/" },
  { href: "/campanhas", label: "Campanhas", icon: "🎯", match: (p: string) => p.startsWith("/campanhas") },
  { href: "/instituicoes", label: "Instituições", icon: "🏠", match: (p: string) => p.startsWith("/instituicoes") },
  { href: "/painel", label: "Painel", icon: "📊", match: (p: string) => p.startsWith("/painel") || p.startsWith("/perfil") || p.startsWith("/notificacoes") },
];

export default function MobileBottomNav() {
  const pathname = usePathname();
  const { user } = useAuth();
  const painelHref = user ? painelPorTipo(user.tipo) : "/login";

  return (
    <nav
      aria-label="Navegação do aplicativo"
      className="fixed inset-x-0 bottom-0 z-40 border-t bg-white/95 backdrop-blur supports-[backdrop-filter]:bg-white/80 sm:hidden"
      style={{ paddingBottom: "env(safe-area-inset-bottom)" }}
    >
      <ul className="mx-auto flex max-w-6xl items-stretch justify-around">
        {tabs.map((t) => {
          const href = t.href === "/painel" ? painelHref : t.href;
          const active = t.match(pathname);
          return (
            <li key={t.href} className="flex-1">
              <Link
                href={href}
                aria-current={active ? "page" : undefined}
                className={`flex flex-col items-center gap-0.5 px-2 py-2 text-xs ${active ? "font-semibold text-blue-700" : "text-gray-600"}`}
              >
                <span aria-hidden="true" className="text-lg leading-none">{t.icon}</span>
                {t.label}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
