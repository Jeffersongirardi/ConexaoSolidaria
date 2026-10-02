"use client";

import { useRouter } from "next/navigation";

/** Botão "← Voltar": volta no histórico ou vai ao destino de segurança (acesso direto). */
export default function Voltar({ fallback = "/", rotulo = "Voltar" }: { fallback?: string; rotulo?: string }) {
  const router = useRouter();
  return (
    <button
      type="button"
      onClick={() => {
        if (typeof window !== "undefined" && window.history.length > 1) {
          router.back();
        } else {
          router.push(fallback);
        }
      }}
      className="text-sm text-[var(--primary)] hover:underline"
    >
      ← {rotulo}
    </button>
  );
}
