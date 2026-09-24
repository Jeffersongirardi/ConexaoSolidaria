import Link from "next/link";

export default function Footer() {
  return (
    <footer className="mt-12 hidden border-t bg-gray-50 sm:block">
      <div className="mx-auto grid max-w-6xl gap-6 px-4 py-8 text-sm text-gray-600 sm:grid-cols-3">
        <div>
          <p className="font-bold text-gray-800">🤝 Conexões Solidárias</p>
          <p className="mt-2">
            Conecte-se a quem transforma doações em impacto real.
          </p>
        </div>
        <nav aria-label="Links úteis">
          <ul className="space-y-1">
            <li><Link href="/sobre" className="hover:text-blue-700">Sobre</Link></li>
            <li><Link href="/faq" className="hover:text-blue-700">Perguntas frequentes</Link></li>
            <li><Link href="/privacidade" className="hover:text-blue-700">Privacidade</Link></li>
            <li><Link href="/contato" className="hover:text-blue-700">Fale conosco</Link></li>
          </ul>
        </nav>
        <div>
          <p className="font-semibold text-gray-800">Transparência</p>
          <p className="mt-1">Instituições validadas e acompanhamento de cada doação até a entrega.</p>
        </div>
      </div>
      <p className="border-t py-3 text-center text-xs text-gray-500">
        © {new Date().getFullYear()} Conexões Solidárias · Feito com cuidado em Curitiba
      </p>
    </footer>
  );
}
