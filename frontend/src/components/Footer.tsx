import Link from "next/link";

export default function Footer() {
  return (
    <footer className="mt-12 hidden border-t bg-[var(--primary)] text-white/85 sm:block">
      <div className="mx-auto grid max-w-6xl gap-6 px-4 py-8 text-sm sm:grid-cols-3">
        <div>
          <p className="font-bold text-white">🤝 Conexões Solidárias</p>
          <p className="mt-2 text-white/75">
            Conecte-se a quem transforma doações em impacto real.
          </p>
        </div>
        <nav aria-label="Links úteis">
          <ul className="space-y-1">
            <li><Link href="/sobre" className="text-white/70 hover:text-[var(--accent)]">Sobre</Link></li>
            <li><Link href="/faq" className="text-white/70 hover:text-[var(--accent)]">Perguntas frequentes</Link></li>
            <li><Link href="/privacidade" className="text-white/70 hover:text-[var(--accent)]">Privacidade</Link></li>
            <li><Link href="/termos" className="text-white/70 hover:text-[var(--accent)]">Termos</Link></li>
            <li><Link href="/contato" className="text-white/70 hover:text-[var(--accent)]">Fale conosco</Link></li>
          </ul>
        </nav>
        <div>
          <p className="font-semibold text-white">Transparência</p>
          <p className="mt-1 text-white/75">Instituições validadas e acompanhamento de cada doação até a entrega.</p>
        </div>
      </div>
      <p className="border-t border-white/10 py-3 text-center text-xs text-white/60">
        © {new Date().getFullYear()} Conexões Solidárias · Feito com cuidado em Curitiba
      </p>
    </footer>
  );
}
