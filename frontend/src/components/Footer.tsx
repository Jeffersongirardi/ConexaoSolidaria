import Link from "next/link";

export default function Footer() {
  return (
    <footer className="mt-12 border-t bg-gray-50">
      <div className="mx-auto grid max-w-6xl gap-6 px-4 py-8 text-sm text-gray-600 sm:grid-cols-3">
        <div>
          <p className="font-bold text-gray-800">🤝 Conexões Solidárias</p>
          <p className="mt-2">
            Conectando doadores a instituições de caridade em Curitiba/PR.
          </p>
        </div>
        <nav aria-label="Links institucionais">
          <ul className="space-y-1">
            <li><Link href="/sobre" className="hover:text-blue-700">Sobre o projeto</Link></li>
            <li><Link href="/faq" className="hover:text-blue-700">Perguntas frequentes</Link></li>
            <li><Link href="/privacidade" className="hover:text-blue-700">Privacidade (LGPD)</Link></li>
            <li><Link href="/contato" className="hover:text-blue-700">Fale conosco</Link></li>
          </ul>
        </nav>
        <div>
          <p className="font-semibold text-gray-800">ODS contemplados</p>
          <p className="mt-1">1 · 2 · 10 · 17 — Erradicação da pobreza, fome zero, redução das desigualdades e parcerias.</p>
        </div>
      </div>
      <p className="border-t py-3 text-center text-xs text-gray-500">
        Atividade Extensionista III — Engenharia de Software · Curitiba/PR
      </p>
    </footer>
  );
}
