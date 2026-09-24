"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
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
    <header className="border-b bg-white">
      <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-3 px-4 py-3">
        <Link href="/" className="text-lg font-bold text-blue-700" aria-label="Conexões Solidárias — início">
          🤝 Conexões Solidárias
        </Link>
        <nav aria-label="Navegação principal" className="ml-auto">
          <ul className="flex flex-wrap items-center gap-1 text-sm">
            {links.map((l) => (
              <li key={l.href}>
                <Link
                  href={l.href}
                  aria-current={pathname === l.href ? "page" : undefined}
                  className={`rounded px-2 py-1 hover:bg-blue-50 ${pathname === l.href ? "font-semibold text-blue-700" : "text-gray-700"}`}
                >
                  {l.label}
                </Link>
              </li>
            ))}
            {user ? (
              <>
                <li>
                  <Link href="/notificacoes" className="rounded px-2 py-1 text-gray-700 hover:bg-gray-100" aria-label={`Notificações${naoLidas > 0 ? `, ${naoLidas} não lidas` : ""}`}>
                    🔔{naoLidas > 0 && <span className="ml-1 rounded-full bg-red-600 px-1.5 text-xs font-bold text-white">{naoLidas}</span>}
                  </Link>
                </li>
                <li>
                  <Link
                    href={painelPorTipo(user.tipo)}
                    className="rounded bg-blue-600 px-3 py-1.5 font-medium text-white hover:bg-blue-700"
                  >
                    Meu painel
                  </Link>
                </li>
                <li>
                  <button onClick={logout} className="rounded px-2 py-1 text-gray-700 hover:bg-gray-100">
                    Sair ({user.nome.split(" ")[0]})
                  </button>
                </li>
              </>
            ) : (
              <>
                <li>
                  <Link href="/login" className="rounded px-2 py-1 text-gray-700 hover:bg-gray-100">
                    Entrar
                  </Link>
                </li>
                <li>
                  <Link href="/cadastro" className="rounded bg-blue-600 px-3 py-1.5 font-medium text-white hover:bg-blue-700">
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
