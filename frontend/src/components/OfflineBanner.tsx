"use client";

import { useOffline } from "next/offline";

export default function OfflineBanner() {
  const isOffline = useOffline();
  if (!isOffline) return null;
  return (
    <div role="status" className="bg-yellow-400 px-4 py-2 text-center text-sm font-medium text-yellow-950">
      Você está offline. O conteúdo carregado continua disponível; novas ações serão retomadas com a conexão.
    </div>
  );
}
