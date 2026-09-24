"use client";

import { useState } from "react";
import { Alert, Field, PrimaryButton, TextArea, TextInput } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";

export default function ContatoPage() {
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [assunto, setAssunto] = useState("");
  const [mensagem, setMensagem] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [retorno, setRetorno] = useState<{ kind: "error" | "success"; text: string } | null>(null);

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    setRetorno(null);
    try {
      await api("/contact", { body: { nome, email, assunto, mensagem }, token: null });
      setRetorno({ kind: "success", text: "Mensagem enviada com sucesso! Retornaremos em breve." });
      setNome("");
      setEmail("");
      setAssunto("");
      setMensagem("");
    } catch (err) {
      setRetorno({ kind: "error", text: (err as ApiError).message });
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="mx-auto max-w-xl">
      <h1 className="text-2xl font-bold">Fale conosco</h1>
      <p className="mt-1 text-sm text-gray-600">Dúvidas, sugestões ou parcerias — estamos aqui para ajudar.</p>
      {retorno && <div className="mt-4"><Alert kind={retorno.kind}>{retorno.text}</Alert></div>}
      <form onSubmit={enviar} className="mt-4 space-y-4 rounded-xl border p-5">
        <Field label="Nome" name="nome"><TextInput id="nome" required value={nome} onChange={(e) => setNome(e.target.value)} autoComplete="name" /></Field>
        <Field label="E-mail" name="email"><TextInput id="email" required type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" /></Field>
        <Field label="Assunto (opcional)" name="assunto"><TextInput id="assunto" value={assunto} onChange={(e) => setAssunto(e.target.value)} /></Field>
        <Field label="Mensagem" name="mensagem"><TextArea id="mensagem" required rows={5} value={mensagem} onChange={(e) => setMensagem(e.target.value)} /></Field>
        <PrimaryButton type="submit" disabled={enviando} className="w-full">
          {enviando ? "Enviando..." : "Enviar mensagem"}
        </PrimaryButton>
      </form>
    </div>
  );
}
