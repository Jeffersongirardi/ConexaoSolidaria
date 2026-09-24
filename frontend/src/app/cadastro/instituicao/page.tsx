"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Alert, Field, PrimaryButton, TextArea, TextInput } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";

export default function CadastroInstituicaoPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    nome: "", email: "", senha: "", cnpj: "", razaoSocial: "", nomeFantasia: "",
    telefone: "", whatsapp: "", endereco: "", descricao: "", categoriaAtuacao: "",
    pixKey: "", pixTitular: "",
  });
  const [lgpd, setLgpd] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState("");

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.value }));

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    setErro("");
    try {
      await api("/auth/register/instituicao", { body: form, token: null });
      router.push("/login?cadastrado=instituicao");
    } catch (err) {
      setErro((err as ApiError).message);
    } finally {
      setEnviando(false);
    }
  };

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-2xl font-bold">Cadastro de instituição</h1>
      <p className="mt-1 text-sm text-gray-600">Após o cadastro, seus dados passam por validação antes da publicação de campanhas.</p>
      {erro && <div className="mt-4"><Alert kind="error">{erro}</Alert></div>}
      <form onSubmit={enviar} className="mt-4 grid gap-4 rounded-xl border p-5 sm:grid-cols-2">
        <Field label="Nome do responsável" name="nome"><TextInput id="nome" required value={form.nome} onChange={set("nome")} /></Field>
        <Field label="E-mail institucional" name="email"><TextInput id="email" type="email" required value={form.email} onChange={set("email")} /></Field>
        <div className="sm:col-span-2"><Field label="Senha (mín. 6 caracteres)" name="senha"><TextInput id="senha" type="password" required minLength={6} autoComplete="new-password" value={form.senha} onChange={set("senha")} /></Field></div>
        <Field label="CNPJ" name="cnpj"><TextInput id="cnpj" required inputMode="numeric" value={form.cnpj} onChange={set("cnpj")} placeholder="00.000.000/0001-00" /></Field>
        <Field label="Razão social" name="razaoSocial"><TextInput id="razaoSocial" required value={form.razaoSocial} onChange={set("razaoSocial")} /></Field>
        <Field label="Nome fantasia (opcional)" name="nomeFantasia"><TextInput id="nomeFantasia" value={form.nomeFantasia} onChange={set("nomeFantasia")} /></Field>
        <Field label="WhatsApp (opcional)" name="whatsapp"><TextInput id="whatsapp" type="tel" value={form.whatsapp} onChange={set("whatsapp")} /></Field>
        <div className="sm:col-span-2"><Field label="Endereço (opcional)" name="endereco"><TextInput id="endereco" value={form.endereco} onChange={set("endereco")} /></Field></div>
        <div className="sm:col-span-2"><Field label="Descrição das atividades (opcional)" name="descricao"><TextArea id="descricao" rows={3} value={form.descricao} onChange={set("descricao")} /></Field></div>
        <Field label="Área de atuação (opcional)" name="categoriaAtuacao"><TextInput id="categoriaAtuacao" value={form.categoriaAtuacao} onChange={set("categoriaAtuacao")} placeholder="Ex.: assistência social" /></Field>
        <Field label="Telefone (opcional)" name="telefone"><TextInput id="telefone" type="tel" value={form.telefone} onChange={set("telefone")} /></Field>
        <Field label="Chave Pix (opcional)" name="pixKey"><TextInput id="pixKey" value={form.pixKey} onChange={set("pixKey")} /></Field>
        <Field label="Titular da chave Pix (opcional)" name="pixTitular"><TextInput id="pixTitular" value={form.pixTitular} onChange={set("pixTitular")} /></Field>
        <div className="sm:col-span-2">
          <label className="flex items-start gap-2 text-sm">
            <input type="checkbox" required checked={lgpd} onChange={(e) => setLgpd(e.target.checked)} className="mt-1" />
            <span>Concordo com o tratamento dos dados conforme a <Link href="/privacidade" className="text-blue-700 hover:underline">Política de Privacidade (LGPD)</Link>.</span>
          </label>
        </div>
        <div className="sm:col-span-2">
          <PrimaryButton type="submit" disabled={enviando || !lgpd} className="w-full">
            {enviando ? "Cadastrando..." : "Solicitar cadastro"}
          </PrimaryButton>
        </div>
      </form>
    </div>
  );
}
