"use client";

import { Suspense, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Alert, Field, PrimaryButton, Spinner, TextInput } from "@/components/ui";
import { painelPorTipo, useAuth } from "@/lib/auth";
import { type ApiError } from "@/lib/api";

function LoginConteudo() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { user, login } = useAuth();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [entrando, setEntrando] = useState(false);
  const [erro, setErro] = useState("");

  useEffect(() => {
    if (user) router.replace(painelPorTipo(user.tipo));
  }, [user, router]);

  if (user) return <Spinner />;

  const entrar = async (e: React.FormEvent) => {
    e.preventDefault();
    setEntrando(true);
    setErro("");
    try {
      const u = await login(email, senha);
      router.push(searchParams.get("origem") ?? painelPorTipo(u.tipo));
    } catch (err) {
      const apiErr = err as ApiError;
      if (apiErr.status === 403 && apiErr.message.toLowerCase().includes("desativada")) {
        setErro("Conta desativada. Entre em contato com o suporte.");
      } else {
        setErro(apiErr.status === 401 || apiErr.status === 400 ? "E-mail ou senha inválidos." : apiErr.message);
      }
    } finally {
      setEntrando(false);
    }
  };

  const cadastrado = searchParams.get("cadastrado");
  const senhaOk = searchParams.get("senha");

  return (
    <div className="mx-auto max-w-md">
      <h1 className="text-2xl font-bold">Entrar</h1>
      <p className="mt-1 text-sm text-gray-600">Acesse com sua conta de doador, instituição ou administrador.</p>
      {cadastrado && <div className="mt-4"><Alert kind="success">{cadastrado === "instituicao" ? "Cadastro enviado! Aguarde aprovação para publicar campanhas." : "Conta criada! Faça login."}</Alert></div>}
      {senhaOk && <div className="mt-4"><Alert kind="success">Senha redefinida! Faça login.</Alert></div>}
      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      <form onSubmit={entrar} className="mt-4 space-y-4 rounded-xl border p-5">
        <Field label="E-mail" name="email">
          <TextInput id="email" type="email" required autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} />
        </Field>
        <Field label="Senha" name="senha">
          <TextInput id="senha" type="password" required autoComplete="current-password" value={senha} onChange={(e) => setSenha(e.target.value)} />
        </Field>
        <PrimaryButton type="submit" disabled={entrando} className="w-full">
          {entrando ? "Entrando..." : "Entrar"}
        </PrimaryButton>
        <p className="flex justify-between text-sm">
          <Link href="/recuperar-senha" className="text-[var(--primary)] hover:underline">Esqueci minha senha</Link>
          <Link href="/cadastro" className="text-[var(--primary)] hover:underline">Criar conta</Link>
        </p>
      </form>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={<Spinner />}>
      <LoginConteudo />
    </Suspense>
  );
}
