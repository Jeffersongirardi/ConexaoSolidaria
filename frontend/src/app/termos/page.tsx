import type { Metadata } from "next";

export const metadata: Metadata = { title: "Termos de Uso" };

export default function TermosPage() {
  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="text-3xl font-bold">Termos de Uso</h1>
      <p className="mt-4 leading-relaxed text-gray-700">
        Ao usar o Conexões Solidárias você concorda em fornecer informações verdadeiras, respeitar as instituições
        parceiras e utilizar a plataforma apenas para doações legítimas.
      </p>
      <h2 className="mt-6 text-lg font-bold">Responsabilidades</h2>
      <ul className="mt-2 list-disc space-y-1 pl-6 text-gray-700">
        <li>O conteúdo das campanhas é de responsabilidade da instituição que publica, após validação.</li>
        <li>Doações em valor têm confirmação manual no momento; nenhuma cobrança real ocorre sem seu consentimento explícito.</li>
        <li>Não nos responsabilizamos por combinações de entrega feitas fora da plataforma.</li>
      </ul>
      <h2 className="mt-6 text-lg font-bold">Contato</h2>
      <p className="mt-2 text-gray-700">Dúvidas sobre os termos? Fale conosco pela página de contato.</p>
    </div>
  );
}
