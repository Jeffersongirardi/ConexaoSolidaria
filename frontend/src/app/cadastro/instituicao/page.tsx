"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Alert, Field, PrimaryButton, TextArea, TextInput } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";

function maskCnpj(v: string) { const d = v.replace(/\D/g, "").slice(0, 14); return d.replace(/^(\d{2})(\d)/, "$1.$2").replace(/^(\d{2})\.(\d{3})(\d)/, "$1.$2.$3").replace(/\.(\d{3})(\d)/, ".$1/$2").replace(/(\d{4})(\d)/, "$1-$2"); }
function maskCep(v: string) { const d = v.replace(/\D/g, "").slice(0, 8); return d.replace(/(\d{5})(\d)/, "$1-$2"); }
function maskPhone(v: string) { const d = v.replace(/\D/g, "").slice(0, 11); if (d.length <= 10) return d.replace(/(\d{2})(\d{4})(\d{0,4})/, "($1) $2-$3").trim(); return d.replace(/(\d{2})(\d{5})(\d{0,4})/, "($1) $2-$3").trim(); }

export default function CadastroInstituicaoPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    nome: "", email: "", senha: "", cnpj: "", razaoSocial: "", nomeFantasia: "",
    telefone: "", whatsapp: "", endereco: "", descricao: "", categoriaAtuacao: "",
    pixKey: "", pixTitular: "", cep: "", cidade: "", estado: "",
  });
  const [lgpd, setLgpd] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erro, setErro] = useState("");
  const [erros, setErros] = useState<Record<string, string>>({});

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    let v = e.target.value;
    if (k === "cnpj") v = maskCnpj(v);
    if (k === "cep") v = maskCep(v);
    if (k === "telefone" || k === "whatsapp") v = maskPhone(v);
    if (k === "estado") v = v.toUpperCase().slice(0, 2);
    setForm((f) => ({ ...f, [k]: v }));
  };

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    setEnviando(true);
    setErro("");
    setErros({});
    try {
      await api("/auth/register/instituicao", { body: form, token: null });
      router.push("/login?cadastrado=instituicao");
    } catch (err) {
      const apiErr = err as ApiError;
      setErro(apiErr.message);
      if (apiErr.errors) setErros(apiErr.errors);
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
        <Field label="CNPJ" name="cnpj" error={erros.cnpj}><TextInput id="cnpj" required inputMode="numeric" value={form.cnpj} onChange={set("cnpj")} placeholder="00.000.000/0001-00" /></Field>
        <Field label="Razão social" name="razaoSocial" error={erros.razaoSocial}><TextInput id="razaoSocial" required value={form.razaoSocial} onChange={set("razaoSocial")} /></Field>
        <Field label="Nome fantasia (opcional)" name="nomeFantasia"><TextInput id="nomeFantasia" value={form.nomeFantasia} onChange={set("nomeFantasia")} /></Field>
        <Field label="WhatsApp (opcional)" name="whatsapp" error={erros.whatsapp}><TextInput id="whatsapp" type="tel" value={form.whatsapp} onChange={set("whatsapp")} /></Field>
        <div className="sm:col-span-2"><Field label="Endereço (opcional)" name="endereco"><TextInput id="endereco" value={form.endereco} onChange={set("endereco")} /></Field></div>
        <Field label="CEP (opcional)" name="cep" error={erros.cep}><TextInput id="cep" inputMode="numeric" value={form.cep} onChange={set("cep")} placeholder="00000-000" /></Field>
        <Field label="Cidade (opcional)" name="cidade"><TextInput id="cidade" value={form.cidade} onChange={set("cidade")} /></Field>
        <Field label="Estado (UF) (opcional)" name="estado" error={erros.estado}><TextInput id="estado" maxLength={2} value={form.estado} onChange={set("estado")} placeholder="PR" /></Field>
        <div className="sm:col-span-2"><Field label="Descrição das atividades (opcional)" name="descricao"><TextArea id="descricao" rows={3} value={form.descricao} onChange={set("descricao")} /></Field></div>
        <Field label="Área de atuação (opcional)" name="categoriaAtuacao"><TextInput id="categoriaAtuacao" value={form.categoriaAtuacao} onChange={set("categoriaAtuacao")} placeholder="Ex.: assistência social" /></Field>
        <Field label="Telefone (opcional)" name="telefone" error={erros.telefone}><TextInput id="telefone" type="tel" value={form.telefone} onChange={set("telefone")} /></Field>
        <Field label="Chave Pix (opcional)" name="pixKey"><TextInput id="pixKey" value={form.pixKey} onChange={set("pixKey")} /></Field>
        <Field label="Titular da chave Pix (opcional)" name="pixTitular"><TextInput id="pixTitular" value={form.pixTitular} onChange={set("pixTitular")} /></Field>
        <div className="sm:col-span-2">
          <label className="flex items-start gap-2 text-sm">
            <input type="checkbox" required checked={lgpd} onChange={(e) => setLgpd(e.target.checked)} className="mt-1" />
            <span>Declaro CNPJ ativo, li os <Link href="/termos" className="text-[var(--primary)] hover:underline">Termos</Link> (só intermediação, taxa R$ 0, sou responsável por campanhas/entregas/uso e por confirmar recebimentos, sem reembolso pela plataforma) e autorizo o tratamento LGPD conforme a <Link href="/privacidade" className="text-[var(--primary)] hover:underline">Privacidade</Link>.</span>
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
