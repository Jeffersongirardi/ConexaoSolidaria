"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Alert, Field, PrimaryButton, TextInput } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";

export default function CadastroDoadorPage() {
  const router = useRouter();
  const [form, setForm] = useState({ nome: "", email: "", senha: "", telefone: "", cpf: "", whatsapp: "", cep: "", cidade: "", estado: "" });
  const [lgpd, setLgpd] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState("");
  const [erros, setErros] = useState<Record<string, string>>({});

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.value }));

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    setErro("");
    setErros({});
    try {
      await api("/auth/register/doador", { body: form, token: null });
      router.push("/login?cadastrado=1");
    } catch (err) {
      const apiErr = err as ApiError;
      setErro(apiErr.message);
      if (apiErr.errors) setErros(apiErr.errors);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="mx-auto max-w-xl">
      <h1 className="text-2xl font-bold">Cadastro de doador</h1>
      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      <form onSubmit={enviar} className="mt-4 grid gap-4 rounded-xl border p-5 sm:grid-cols-2" noValidate={false}>
        <div className="sm:col-span-2"><Field label="Nome completo" name="nome" error={erros.nome}><TextInput id="nome" required autoComplete="name" value={form.nome} onChange={set("nome")} aria-describedby={erros.nome ? "nome-erro" : undefined} /></Field></div>
        <div className="sm:col-span-2"><Field label="E-mail" name="email" error={erros.email}><TextInput id="email" type="email" required autoComplete="email" value={form.email} onChange={set("email")} /></Field></div>
        <div className="sm:col-span-2"><Field label="Senha (mín. 6 caracteres)" name="senha" error={erros.senha}><TextInput id="senha" type="password" required minLength={6} autoComplete="new-password" value={form.senha} onChange={set("senha")} /></Field></div>
        <Field label="CPF (opcional)" name="cpf"><TextInput id="cpf" inputMode="numeric" value={form.cpf} onChange={set("cpf")} placeholder="000.000.000-00" /></Field>
        <Field label="Telefone (opcional)" name="telefone"><TextInput id="telefone" type="tel" autoComplete="tel" value={form.telefone} onChange={set("telefone")} /></Field>
        <Field label="WhatsApp (opcional)" name="whatsapp"><TextInput id="whatsapp" type="tel" value={form.whatsapp} onChange={set("whatsapp")} /></Field>
        <Field label="CEP (opcional)" name="cep"><TextInput id="cep" inputMode="numeric" autoComplete="postal-code" value={form.cep} onChange={set("cep")} /></Field>
        <Field label="Cidade (opcional)" name="cidade"><TextInput id="cidade" value={form.cidade} onChange={set("cidade")} /></Field>
        <Field label="Estado (UF)" name="estado"><TextInput id="estado" maxLength={2} value={form.estado} onChange={set("estado")} placeholder="PR" /></Field>
        <div className="sm:col-span-2">
          <label className="flex items-start gap-2 text-sm">
            <input type="checkbox" required checked={lgpd} onChange={(e) => setLgpd(e.target.checked)} className="mt-1" />
            <span>Li e concordo com o tratamento dos meus dados conforme a <Link href="/privacidade" className="text-blue-700 hover:underline">Política de Privacidade (LGPD)</Link>.</span>
          </label>
        </div>
        <div className="sm:col-span-2">
          <PrimaryButton type="submit" disabled={enviando || !lgpd} className="w-full">
            {enviando ? "Cadastrando..." : "Criar conta"}
          </PrimaryButton>
        </div>
      </form>
    </div>
  );
}
