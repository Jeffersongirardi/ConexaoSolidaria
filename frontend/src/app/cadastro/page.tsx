import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = { title: "Criar conta" };

export default function CadastroPage() {
  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-2xl font-bold">Criar conta</h1>
      <p className="mt-1 text-sm text-gray-600">Escolha o tipo de cadastro:</p>
      <div className="mt-4 grid gap-4 sm:grid-cols-2">
        <Link href="/cadastro/doador" className="rounded-xl border p-6 hover:border-blue-600 hover:shadow-md">
          <p aria-hidden="true" className="text-4xl">🙋</p>
          <h2 className="mt-2 font-bold">Sou doador</h2>
          <p className="mt-1 text-sm text-gray-600">Doe itens ou valores e acompanhe cada doação até a entrega.</p>
        </Link>
        <Link href="/cadastro/instituicao" className="rounded-xl border p-6 hover:border-blue-600 hover:shadow-md">
          <p aria-hidden="true" className="text-4xl">🏠</p>
          <h2 className="mt-2 font-bold">Sou instituição</h2>
          <p className="mt-1 text-sm text-gray-600">Publique campanhas e gerencie doações recebidas (sujeito a aprovação).</p>
        </Link>
      </div>
    </div>
  );
}
