import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = { title: "Sobre" };

export default function SobrePage() {
  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="text-3xl font-bold">Sobre o Conexões Solidárias</h1>
      <p className="mt-4 leading-relaxed">
        O <strong>Conexões Solidárias</strong> aproxima quem quer ajudar de quem
        faz a diferença todos os dias — com foco em doações de alimentos, roupas
        e itens essenciais para pessoas em situação de vulnerabilidade.
      </p>
      <p className="mt-3 leading-relaxed text-gray-700">
        Nascido em Curitiba e aberto a instituições de todo o Brasil, o app
        organiza campanhas, registra cada intenção de doação e mostra o caminho
        até a entrega, com transparência e acompanhamento.
      </p>

      <h2 className="mt-8 text-xl font-bold">Como funciona</h2>
      <ol className="mt-3 list-decimal space-y-2 pl-6 text-gray-700">
        <li>Escolha uma campanha de uma instituição validada.</li>
        <li>Registre o que vai doar — itens ou contribuição em valor.</li>
        <li>Acompanhe o status até a confirmação de recebimento.</li>
      </ol>

      <h2 className="mt-8 text-xl font-bold">Transparência</h2>
      <p className="mt-2 text-gray-700">
        Toda instituição passa por validação antes de publicar campanhas, e cada
        doação pode ser acompanhada com comprovante e atualizações da instituição.
      </p>
      <p className="mt-6">
        <Link href="/campanhas" className="rounded-lg bg-blue-600 px-5 py-2.5 font-semibold text-white hover:bg-blue-700">
          Ver campanhas
        </Link>
      </p>
    </div>
  );
}
