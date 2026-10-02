"use client";

import Link from "next/link";

export default function Error({ error, reset }: { error: Error & { digest?: string }; reset: () => void }) {
  return (
    <div className="mx-auto max-w-xl py-16 text-center">
      <p aria-hidden="true" className="text-6xl">⚠️</p>
      <h1 className="mt-4 text-2xl font-bold">Algo deu errado</h1>
      <p className="mt-2 text-sm text-gray-600">{error.message || "Tente novamente em instantes."}</p>
      <p className="mt-6 flex justify-center gap-2">
        <button onClick={reset} className="rounded-lg bg-[var(--primary)] px-5 py-2.5 font-semibold text-white hover:brightness-95">
          Tentar novamente
        </button>
        <Link href="/" className="rounded-lg border px-5 py-2.5 hover:bg-gray-50">Início</Link>
        <Link href="/campanhas" className="rounded-lg border px-5 py-2.5 hover:bg-gray-50">Campanhas</Link>
      </p>
    </div>
  );
}
