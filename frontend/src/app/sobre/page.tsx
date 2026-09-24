import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = { title: "Sobre" };

const piloto = [
  { nome: "Amigos do Caximba", bairro: "Caximba" },
  { nome: "Fundação Iniciativa", bairro: "Uberaba" },
  { nome: "Complexo de Saúde Pequeno Cotolengo", bairro: "Campo Comprido" },
];

export default function SobrePage() {
  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="text-3xl font-bold">Sobre o projeto</h1>
      <p className="mt-4 leading-relaxed">
        O <strong>Conexões Solidárias</strong> é uma plataforma digital que aproxima
        doadores e instituições de caridade em Curitiba/PR, com foco na doação de
        alimentos e roupas para pessoas em situação de vulnerabilidade social.
      </p>
      <p className="mt-3 leading-relaxed">
        O projeto é desenvolvido como Atividade Extensionista do curso de
        Engenharia de Software e contempla os Objetivos de Desenvolvimento
        Sustentável (ODS) 1, 2, 10 e 17.
      </p>

      <h2 className="mt-8 text-xl font-bold">Piloto em Curitiba</h2>
      <p className="mt-2 text-gray-700">
        A avaliação piloto envolve instituições de assistência social da cidade:
      </p>
      <ul className="mt-3 space-y-2">
        {piloto.map((p) => (
          <li key={p.nome} className="rounded-xl border p-3">
            <strong>{p.nome}</strong>
            <span className="text-gray-600"> — bairro {p.bairro}, Curitiba/PR</span>
          </li>
        ))}
      </ul>

      <h2 className="mt-8 text-xl font-bold">Transparência</h2>
      <p className="mt-2 text-gray-700">
        Instituições passam por validação antes de publicar campanhas, e cada
        doação pode ser acompanhada até a confirmação de recebimento.
      </p>
      <p className="mt-6">
        <Link href="/campanhas" className="rounded-lg bg-blue-600 px-5 py-2.5 font-semibold text-white hover:bg-blue-700">
          Ver campanhas
        </Link>
      </p>
    </div>
  );
}
