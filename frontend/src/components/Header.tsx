"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { Bell, User } from "lucide-react";
import { painelPorTipo, useAuth } from "@/lib/auth";
import { api } from "@/lib/api";

const links = [
  { href: "/", label: "Início" },
  { href: "/campanhas", label: "Campanhas" },
  { href: "/instituicoes", label: "Instituições" },
  { href: "/blog", label: "Blog" },
  { href: "/sobre", label: "Sobre" },
  { href: "/faq", label: "FAQ" },
  { href: "/contato", label: "Contato" },
];

export default function Header() {
  const pathname = usePathname();
  const { user, logout } = useAuth();
  const [naoLidas, setNaoLidas] = useState(0);

  useEffect(() => {
    if (!user) {
      setNaoLidas(0);
      return;
    }
    api<{ count: number }>("/notifications/nao-lidas")
      .then((d) => setNaoLidas(d.count))
      .catch(() => {});
  }, [user, pathname]);

  return (
    <header className="sticky top-0 z-30 border-b border-white/10 bg-[var(--primary)] text-white shadow-sm">
      <div className="mx-auto flex h-14 max-w-6xl items-center gap-3 px-4">
        <Link href="/" className="flex items-center gap-2 text-[15px] font-bold tracking-tight" aria-label="Conexões Solidárias — início">
          <span aria-hidden="true" className="flex h-7 w-7 items-center justify-center rounded-full bg-[var(--accent)] text-[var(--primary)]">♥</span>
          <span className="hidden sm:inline">Conexões Solidárias</span>
          <span className="sm:hidden">Conexões</span>
        </Link>

        {/* Desktop: navegação completa (paleta legada) */}
        <nav aria-label="Navegação principal" className="ml-6 hidden sm:block">
          <ul className="flex items-center gap-1 text-sm">
            {links.map((l) => (
              <li key={l.href}>
                <Link
                  href={l.href}
                  aria-current={pathname === l.href ? "page" : undefined}
                  className={`rounded-lg px-2.5 py-1.5 ${pathname === l.href ? "bg-white/15 font-semibold text-white" : "text-white/85 hover:bg-white/10 hover:text-white"}`}
                >
                  {l.label}
                </Link>
              </li>
            ))}
          </ul>
        </nav>

        {/* Ações à direita: sempre visíveis, enxutas no app */}
        <nav aria-label="Ações" className="ml-auto">
          <ul className="flex items-center gap-1 text-sm">
            {user ? (
              <>
                <li>
                  <Link
                    href="/notificacoes"
                    aria-label={`Notificações${naoLidas > 0 ? `, ${naoLidas} não lidas` : ""}`}
                    className="relative inline-flex h-9 w-9 items-center justify-center rounded-full text-white/90 hover:bg-white/10"
                  >
                    <Bell className="h-5 w-5" />
                    {naoLidas > 0 && (
                      <span className="absolute -right-1 -top-1 min-w-5 rounded-full bg-[var(--accent)] px-1 py-0.5 text-center text-xs font-bold leading-none text-[var(--primary)]">
                        {naoLidas > 99 ? "99+" : naoLidas}
                      </span>
                    )}
                  </Link>
                </li>
                <li className="hidden sm:block">
                  <Link
                    href={painelPorTipo(user.tipo)}
                    className="rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-[var(--primary)] hover:brightness-95"
                  >
                    Meu painel
                  </Link>
                </li>
                <li className="hidden sm:block">
                  <button onClick={logout} className="rounded-lg px-2.5 py-1.5 text-white/85 hover:bg-white/10 hover:text-white">
                    Sair
                  </button>
                </li>
                <li className="sm:hidden">
                  <Link
                    href={painelPorTipo(user.tipo)}
                    aria-label="Meu painel"
                    className="inline-flex h-9 w-9 items-center justify-center rounded-full bg-white/15"
                  >
                    <User className="h-5 w-5" />
                  </Link>
                </li>
              </>
            ) : (
              <>
                <li>
                  <Link href="/login" className="hidden rounded-lg px-3 py-1.5 text-white/90 hover:bg-white/10 hover:text-white sm:inline-flex">
                    Entrar
                  </Link>
                </li>
                <li>
                  <Link href="/cadastro" className="rounded-full bg-[var(--accent)] px-4 py-2 text-sm font-semibold text-[var(--primary)] hover:brightness-95">
                    Cadastrar
                  </Link>
                </li>
              </>
            )}
          </ul>
        </nav>
      </div>
    </header>
  );
}
