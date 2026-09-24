"use client";

import { Suspense, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Alert, Field, PrimaryButton, Spinner, TextInput } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";

function RedefinirConteudo() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get("token") ?? "";
  const [senha, setSenha] = useState("");
  const [confirmacao, setConfirmacao] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState("");

  if (!token) {
    return <Alert kind="error">Link inválido. <Link href="/recuperar-senha" className="underline">Solicite um novo link</Link>.</Alert>;
  }

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    setErro("");
    try {
      await api("/auth/reset-password", { body: { token, senha, confirmacao }, token: null });
      router.push("/login?senha=ok");
    } catch (err) {
      setErro((err as ApiError).message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="mx-auto max-w-md">
      <h1 className="text-2xl font-bold">Redefinir senha</h1>
      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      <form onSubmit={enviar} className="mt-4 space-y-4 rounded-xl border p-5">
        <Field label="Nova senha (mín. 6 caracteres)" name="senha">
          <TextInput id="senha" type="password" required minLength={6} autoComplete="new-password" value={senha} onChange={(e) => setSenha(e.target.value)} />
        </Field>
        <Field label="Confirmar nova senha" name="confirmacao">
          <TextInput id="confirmacao" type="password" required autoComplete="new-password" value={confirmacao} onChange={(e) => setConfirmacao(e.target.value)} />
        </Field>
        <PrimaryButton type="submit" disabled={enviando} className="w-full">
          {enviando ? "Salvando..." : "Redefinir senha"}
        </PrimaryButton>
      </form>
    </div>
  );
}

export default function RedefinirSenhaPage() {
  return (
    <Suspense fallback={<Spinner />}>
      <RedefinirConteudo />
    </Suspense>
  );
}
