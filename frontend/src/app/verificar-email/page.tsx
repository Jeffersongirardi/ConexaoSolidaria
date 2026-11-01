"use client";

import { Suspense, useEffect, useState } from "react";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Alert, Spinner } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";

function VerificarConteudo() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token") ?? "";
  const [estado, setEstado] = useState<"enviando" | "ok" | "erro">("enviando");
  const [mensagem, setMensagem] = useState("");

  useEffect(() => {
    if (!token) {
      setEstado("erro");
      setMensagem("Link inválido. Solicite um novo e-mail de confirmação.");
      return;
    }
    api<{ message: string }>("/auth/verify-email", { method: "POST", body: { token }, token: null })
      .then((r) => {
        setEstado("ok");
        setMensagem(r.message);
      })
      .catch((err) => {
        setEstado("erro");
        setMensagem((err as ApiError).message);
      });
  }, [token]);

  if (estado === "enviando") return <Spinner />;

  return (
    <div className="mx-auto max-w-md text-center">
      <h1 className="text-2xl font-bold">{estado === "ok" ? "E-mail confirmado" : "Não foi possível confirmar"}</h1>
      <div className="mt-4">
        <Alert kind={estado === "ok" ? "success" : "error"}>
          {estado === "ok"
            ? "Tudo certo! Você já pode doar, ofertar e reivindicar."
            : mensagem}
        </Alert>
      </div>
      <p className="mt-6 flex justify-center gap-2">
        {estado === "ok" ? (
          <Link href="/login" className="rounded-lg bg-[var(--primary)] px-4 py-2 text-sm font-semibold text-white hover:brightness-95">
            Entrar
          </Link>
        ) : (
          <Link href="/login" className="rounded-lg border px-4 py-2 text-sm hover:bg-gray-50">
            Voltar ao login
          </Link>
        )}
      </p>
    </div>
  );
}

export default function VerificarEmailPage() {
  return (
    <Suspense fallback={<Spinner />}>
      <VerificarConteudo />
    </Suspense>
  );
}
