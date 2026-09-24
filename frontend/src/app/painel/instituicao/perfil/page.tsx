"use client";

import { useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import { Alert, Field, PrimaryButton, Spinner, TextArea, TextInput } from "@/components/ui";
import { api, fileUrl, type ApiError } from "@/lib/api";
import type { Institution } from "@/lib/types";

function PerfilInstituicao() {
  const [perfil, setPerfil] = useState<Institution | null>(null);
  const [form, setForm] = useState({ nomeFantasia: "", endereco: "", website: "", descricao: "", categoriaAtuacao: "", whatsapp: "", pixKey: "", pixTitular: "" });
  const [foto, setFoto] = useState<File | null>(null);
  const [salvando, setSalvando] = useState(false);
  const [retorno, setRetorno] = useState<{ kind: "error" | "success"; text: string } | null>(null);

  useEffect(() => {
    api<Institution>("/institutions/minha")
      .then((p) => {
        setPerfil(p);
        setForm({
          nomeFantasia: p.nomeFantasia ?? "", endereco: p.endereco ?? "", website: p.website ?? "",
          descricao: p.descricao ?? "", categoriaAtuacao: p.categoriaAtuacao ?? "",
          whatsapp: p.whatsapp ?? "", pixKey: p.pixKey ?? "", pixTitular: p.pixTitular ?? "",
        });
      })
      .catch((err) => setRetorno({ kind: "error", text: (err as ApiError).message }));
  }, []);

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.value }));

  const salvar = async (e: React.FormEvent) => {
    e.preventDefault();
    setSalvando(true);
    setRetorno(null);
    try {
      const atualizado = await api<Institution>("/institutions/minha", { method: "PUT", body: form });
      if (foto) {
        const formData = new FormData();
        formData.set("foto", foto);
        const comFoto = await api<Institution>("/institutions/minha/foto", { method: "POST", form: formData });
        setPerfil(comFoto);
      } else {
        setPerfil(atualizado);
      }
      setRetorno({ kind: "success", text: "Perfil atualizado!" });
    } catch (err) {
      setRetorno({ kind: "error", text: (err as ApiError).message });
    } finally {
      setSalvando(false);
    }
  };

  if (!perfil && !retorno) return <Spinner />;
  const fotoAtual = perfil ? fileUrl(perfil.fotoUrl) : null;

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-2xl font-bold">Perfil da instituição</h1>
      {perfil && <p className="mt-1 text-sm text-gray-600">{perfil.razaoSocial} · CNPJ {perfil.cnpj}</p>}
      {retorno && <div className="mt-4"><Alert kind={retorno.kind}>{retorno.text}</Alert></div>}
      <form onSubmit={salvar} className="mt-4 grid gap-4 rounded-xl border p-5 sm:grid-cols-2">
        {fotoAtual && (
          <div className="sm:col-span-2">
            <img src={fotoAtual} alt="Foto atual da instituição" className="h-32 w-32 rounded-xl object-cover" />
          </div>
        )}
        <Field label="Nome fantasia" name="nomeFantasia"><TextInput id="nomeFantasia" value={form.nomeFantasia} onChange={set("nomeFantasia")} /></Field>
        <Field label="WhatsApp" name="whatsapp"><TextInput id="whatsapp" type="tel" value={form.whatsapp} onChange={set("whatsapp")} /></Field>
        <div className="sm:col-span-2"><Field label="Endereço" name="endereco"><TextInput id="endereco" value={form.endereco} onChange={set("endereco")} /></Field></div>
        <Field label="Site" name="website"><TextInput id="website" type="url" value={form.website} onChange={set("website")} /></Field>
        <Field label="Área de atuação" name="categoriaAtuacao"><TextInput id="categoriaAtuacao" value={form.categoriaAtuacao} onChange={set("categoriaAtuacao")} /></Field>
        <div className="sm:col-span-2"><Field label="Descrição" name="descricao"><TextArea id="descricao" rows={4} value={form.descricao} onChange={set("descricao")} /></Field></div>
        <Field label="Chave Pix" name="pixKey"><TextInput id="pixKey" value={form.pixKey} onChange={set("pixKey")} /></Field>
        <Field label="Titular da chave Pix" name="pixTitular"><TextInput id="pixTitular" value={form.pixTitular} onChange={set("pixTitular")} /></Field>
        <div className="sm:col-span-2">
          <Field label="Foto da instituição (opcional)" name="foto">
            <input id="foto" type="file" accept="image/*" onChange={(e) => setFoto(e.target.files?.[0] ?? null)} className="text-sm" />
          </Field>
        </div>
        <div className="sm:col-span-2">
          <PrimaryButton type="submit" disabled={salvando} className="w-full">
            {salvando ? "Salvando..." : "Salvar perfil"}
          </PrimaryButton>
        </div>
      </form>
    </div>
  );
}

export default function Page() {
  return (
    <RequireAuth tipos={["instituicao"]}>
      <PerfilInstituicao />
    </RequireAuth>
  );
}
