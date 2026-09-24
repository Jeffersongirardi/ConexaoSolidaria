"use client";

import { useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import { Alert, Field, PrimaryButton, Spinner, TextInput } from "@/components/ui";
import { api, fileUrl, type ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import type { User } from "@/lib/types";

function PerfilConteudo() {
  const { refreshUser } = useAuth();
  const [user, setUser] = useState<User | null>(null);
  const [form, setForm] = useState({ nome: "", telefone: "", whatsapp: "", cep: "", cidade: "", estado: "" });
  const [avatar, setAvatar] = useState<File | null>(null);
  const [salvando, setSalvando] = useState(false);
  const [retorno, setRetorno] = useState<{ kind: "error" | "success"; text: string } | null>(null);

  const [senhas, setSenhas] = useState({ senhaAtual: "", novaSenha: "", confirmacao: "" });
  const [trocando, setTrocando] = useState(false);
  const [retornoSenha, setRetornoSenha] = useState<{ kind: "error" | "success"; text: string } | null>(null);

  useEffect(() => {
    api<User>("/users/me")
      .then((u) => {
        setUser(u);
        setForm({ nome: u.nome ?? "", telefone: u.telefone ?? "", whatsapp: u.whatsapp ?? "", cep: u.cep ?? "", cidade: u.cidade ?? "", estado: u.estado ?? "" });
      })
      .catch((err) => setRetorno({ kind: "error", text: (err as ApiError).message }));
  }, []);

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.value }));

  const salvar = async (e: React.FormEvent) => {
    e.preventDefault();
    setSalvando(true);
    setRetorno(null);
    try {
      let atualizado = await api<User>("/users/me", { method: "PUT", body: form });
      if (avatar) {
        const formData = new FormData();
        formData.set("avatar", avatar);
        atualizado = await api<User>("/users/me/avatar", { method: "POST", form: formData });
      }
      setUser(atualizado);
      await refreshUser();
      setRetorno({ kind: "success", text: "Perfil atualizado!" });
    } catch (err) {
      setRetorno({ kind: "error", text: (err as ApiError).message });
    } finally {
      setSalvando(false);
    }
  };

  const trocarSenha = async (e: React.FormEvent) => {
    e.preventDefault();
    setTrocando(true);
    setRetornoSenha(null);
    try {
      const data = await api<{ message: string }>("/users/me/password", { body: senhas });
      setRetornoSenha({ kind: "success", text: data.message });
      setSenhas({ senhaAtual: "", novaSenha: "", confirmacao: "" });
    } catch (err) {
      setRetornoSenha({ kind: "error", text: (err as ApiError).message });
    } finally {
      setTrocando(false);
    }
  };

  if (!user && !retorno) return <Spinner />;
  const avatarUrl = user ? fileUrl(user.avatarUrl) : null;

  return (
    <div className="mx-auto max-w-xl">
      <h1 className="text-2xl font-bold">Meu perfil</h1>
      {user && <p className="mt-1 text-sm text-gray-600">{user.email} · conta de {user.tipo}</p>}
      {retorno && <div className="mt-4"><Alert kind={retorno.kind}>{retorno.text}</Alert></div>}
      <form onSubmit={salvar} className="mt-4 grid gap-4 rounded-xl border p-5 sm:grid-cols-2">
        {avatarUrl && (
          <div className="sm:col-span-2">
            <img src={avatarUrl} alt="Seu avatar atual" className="h-24 w-24 rounded-full object-cover" />
          </div>
        )}
        <div className="sm:col-span-2"><Field label="Nome" name="nome"><TextInput id="nome" required value={form.nome} onChange={set("nome")} autoComplete="name" /></Field></div>
        <Field label="Telefone" name="telefone"><TextInput id="telefone" type="tel" value={form.telefone} onChange={set("telefone")} /></Field>
        <Field label="WhatsApp" name="whatsapp"><TextInput id="whatsapp" type="tel" value={form.whatsapp} onChange={set("whatsapp")} /></Field>
        <Field label="CEP" name="cep"><TextInput id="cep" inputMode="numeric" value={form.cep} onChange={set("cep")} /></Field>
        <Field label="Cidade" name="cidade"><TextInput id="cidade" value={form.cidade} onChange={set("cidade")} /></Field>
        <div className="sm:col-span-2"><Field label="Estado (UF)" name="estado"><TextInput id="estado" maxLength={2} value={form.estado} onChange={set("estado")} /></Field></div>
        <div className="sm:col-span-2">
          <Field label="Foto de perfil (opcional)" name="avatar">
            <input id="avatar" type="file" accept="image/*" onChange={(e) => setAvatar(e.target.files?.[0] ?? null)} className="text-sm" />
          </Field>
        </div>
        <div className="sm:col-span-2">
          <PrimaryButton type="submit" disabled={salvando} className="w-full">
            {salvando ? "Salvando..." : "Salvar perfil"}
          </PrimaryButton>
        </div>
      </form>

      <h2 className="mt-8 text-lg font-bold">Alterar senha</h2>
      {retornoSenha && <div className="mt-2"><Alert kind={retornoSenha.kind}>{retornoSenha.text}</Alert></div>}
      <form onSubmit={trocarSenha} className="mt-2 space-y-4 rounded-xl border p-5">
        <Field label="Senha atual" name="senhaAtual"><TextInput id="senhaAtual" type="password" required autoComplete="current-password" value={senhas.senhaAtual} onChange={(e) => setSenhas({ ...senhas, senhaAtual: e.target.value })} /></Field>
        <Field label="Nova senha (mín. 6 caracteres)" name="novaSenha"><TextInput id="novaSenha" type="password" required minLength={6} autoComplete="new-password" value={senhas.novaSenha} onChange={(e) => setSenhas({ ...senhas, novaSenha: e.target.value })} /></Field>
        <Field label="Confirmar nova senha" name="confirmacao"><TextInput id="confirmacao" type="password" required autoComplete="new-password" value={senhas.confirmacao} onChange={(e) => setSenhas({ ...senhas, confirmacao: e.target.value })} /></Field>
        <PrimaryButton type="submit" disabled={trocando} className="w-full">
          {trocando ? "Alterando..." : "Alterar senha"}
        </PrimaryButton>
      </form>
    </div>
  );
}

export default function Page() {
  return (
    <RequireAuth>
      <PerfilConteudo />
    </RequireAuth>
  );
}
