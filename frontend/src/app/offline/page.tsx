import Link from "next/link";
import type { Metadata } from "next";

export const metadata: Metadata = { title: "Offline" };

export default function OfflinePage() {
  return (
    <div className="mx-auto max-w-xl py-16 text-center">
      <p aria-hidden="true" className="text-5xl">📡</p>
      <h1 className="mt-4 text-2xl font-bold">Você está offline</h1>
      <p className="mt-2 text-gray-600">
        Não foi possível carregar esta página sem conexão. Páginas visitadas
        recentemente continuam disponíveis.
      </p>
      <p className="mt-6">
        <Link href="/" className="rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white hover:bg-blue-700">
          Voltar ao início
        </Link>
      </p>
    </div>
  );
}
