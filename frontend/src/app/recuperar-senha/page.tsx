"use client";

import { useState } from "react";
import { Alert, Field, PrimaryButton, TextInput } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";

export default function RecuperarSenhaPage() {
  const [email, setEmail] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [retorno, setRetorno] = useState<{ kind: "error" | "success"; text: string } | null>(null);

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    setRetorno(null);
    try {
      const data = await api<{ message: string }>("/auth/forgot-password", { body: { email }, token: null });
      setRetorno({ kind: "success", text: data.message });
    } catch (err) {
      setRetorno({ kind: "error", text: (err as ApiError).message });
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="mx-auto max-w-md">
      <h1 className="text-2xl font-bold">Recuperar senha</h1>
      <p className="mt-1 text-sm text-gray-600">Informe seu e-mail para receber o link de redefinição (válido por 1 hora).</p>
      {retorno && <div className="mt-4"><Alert kind={retorno.kind}>{retorno.text}</Alert></div>}
      <form onSubmit={enviar} className="mt-4 space-y-4 rounded-xl border p-5">
        <Field label="E-mail" name="email">
          <TextInput id="email" type="email" required autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </Field>
        <PrimaryButton type="submit" disabled={enviando} className="w-full">
          {enviando ? "Enviando..." : "Enviar link"}
        </PrimaryButton>
      </form>
    </div>
  );
}
