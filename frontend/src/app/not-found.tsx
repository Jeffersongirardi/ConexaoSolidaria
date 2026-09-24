import Link from "next/link";

export default function NotFound() {
  return (
    <div className="mx-auto max-w-xl py-16 text-center">
      <p aria-hidden="true" className="text-6xl">🔍</p>
      <h1 className="mt-4 text-3xl font-bold">Página não encontrada</h1>
      <p className="mt-2 text-gray-600">A página que você procura não existe ou foi movida.</p>
      <p className="mt-6 flex justify-center gap-2">
        <Link href="/" className="rounded-lg bg-[var(--primary)] px-5 py-2.5 font-semibold text-white hover:brightness-95">
          Voltar ao início
        </Link>
        <Link href="/campanhas" className="rounded-lg border border-[var(--border)] px-5 py-2.5 font-semibold hover:bg-gray-50">
          Ver campanhas
        </Link>
      </p>
    </div>
  );
}
