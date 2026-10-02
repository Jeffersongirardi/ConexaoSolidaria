"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Bell, Building2, Gift, House, LayoutDashboard, Target } from "lucide-react";
import { useAuth, painelPorTipo } from "@/lib/auth";

const tabsBase = [
  { href: "/", label: "Início", Icon: House, match: (p: string) => p === "/" },
  { href: "/campanhas", label: "Campanhas", Icon: Target, match: (p: string) => p.startsWith("/campanhas") },
  { href: "/instituicoes", label: "Instituições", Icon: Building2, match: (p: string) => p.startsWith("/instituicoes") },
  { href: "/painel", label: "Painel", Icon: LayoutDashboard, match: (p: string) => p.startsWith("/painel") || p.startsWith("/perfil") },
  { href: "/notificacoes", label: "Notificações", Icon: Bell, match: (p: string) => p.startsWith("/notificacoes") },
];

const tabOfertas = { href: "/ofertas", label: "Ofertas", Icon: Gift, match: (p: string) => p.startsWith("/ofertas") };

export default function MobileBottomNav() {
  const pathname = usePathname();
  const { user } = useAuth();
  const painelHref = user ? painelPorTipo(user.tipo) : "/login";
  const tabs = user?.tipo === "instituicao"
    ? [tabsBase[0], tabsBase[1], tabOfertas, tabsBase[3], tabsBase[4]]
    : tabsBase;

  return (
    <nav
      aria-label="Navegação do aplicativo"
      className="fixed inset-x-0 bottom-0 z-40 border-t border-[var(--border)] bg-white/95 backdrop-blur supports-[backdrop-filter]:bg-white/85 md:hidden"
      style={{ paddingBottom: "env(safe-area-inset-bottom)" }}
    >
      <ul className="mx-auto flex max-w-6xl items-stretch justify-around">
        {tabs.map((t) => {
          const href = t.href === "/painel" ? painelHref : t.href;
          const active = t.match(pathname);
          const Icon = t.Icon;
          return (
            <li key={t.href} className="flex-1">
              <Link
                href={href}
                aria-current={active ? "page" : undefined}
                className={`flex min-h-11 flex-col items-center justify-center gap-0.5 px-1 py-2 text-[11px] leading-none ${active ? "font-semibold text-[var(--primary)]" : "text-[var(--text-soft)]"}`}
              >
                <Icon className={`h-[22px] w-[22px] ${active ? "stroke-[2.2]" : "stroke-[1.7]"}`} aria-hidden="true" />
                {t.label}
              </Link>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
