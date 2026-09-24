"use client";

import { useEffect, useState } from "react";

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: string }>;
}

export default function InstallPrompt() {
  const [deferred, setDeferred] = useState<BeforeInstallPromptEvent | null>(null);
  const [isIOS, setIsIOS] = useState(false);
  const [isStandalone, setIsStandalone] = useState(false);

  useEffect(() => {
    setIsIOS(/iPad|iPhone|iPod/.test(navigator.userAgent));
    setIsStandalone(window.matchMedia("(display-mode: standalone)").matches);
    const handler = (e: Event) => {
      e.preventDefault();
      setDeferred(e as BeforeInstallPromptEvent);
    };
    window.addEventListener("beforeinstallprompt", handler);
    return () => window.removeEventListener("beforeinstallprompt", handler);
  }, []);

  if (isStandalone) return null;

  const instalar = async () => {
    if (!deferred) return;
    await deferred.prompt();
    await deferred.userChoice;
    setDeferred(null);
  };

  return (
    <section aria-label="Instalar aplicativo" className="mt-8 rounded-xl border border-blue-200 bg-blue-50 p-4 text-sm">
      <h2 className="font-semibold text-blue-900">📲 Instale o aplicativo</h2>
      {deferred ? (
        <p className="mt-1 text-blue-900">
          <button onClick={instalar} className="mt-2 rounded-lg bg-blue-600 px-4 py-2 font-semibold text-white hover:bg-blue-700">
            Instalar agora
          </button>
        </p>
      ) : (
        <p className="mt-1 text-blue-900">
          {isIOS
            ? "No iPhone/iPad: toque em Compartilhar e depois em “Adicionar à Tela de Início”."
            : "Use o menu do navegador (“Instalar aplicativo” / “Adicionar à tela inicial”) para acesso rápido, inclusive offline."}
        </p>
      )}
    </section>
  );
}
