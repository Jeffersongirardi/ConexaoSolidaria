"use client";

import { useState } from "react";
import { Alert, Field, PrimaryButton, Select, TextArea, TextInput } from "./ui";
import { api, type ApiError } from "@/lib/api";
import type { BlogPost } from "@/lib/types";

export default function BlogForm({
  inicial,
  onSalvo,
}: {
  inicial?: BlogPost | null;
  onSalvo: (p: BlogPost) => void;
}) {
  const [form, setForm] = useState({
    titulo: inicial?.titulo ?? "",
    conteudo: inicial?.conteudo ?? "",
    resumo: inicial?.resumo ?? "",
    categoria: inicial?.categoria ?? "geral",
    imagemUrl: inicial?.imagemUrl ?? "",
    publicado: inicial?.publicado ?? true,
  });
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState("");

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.type === "checkbox" ? (e as React.ChangeEvent<HTMLInputElement>).target.checked : e.target.value }));

  const salvar = async (e: React.FormEvent) => {
    e.preventDefault();
    setSalvando(true);
    setErro("");
    try {
      const p = inicial
        ? await api<BlogPost>(`/blog/${inicial.id}`, { method: "PUT", body: form })
        : await api<BlogPost>("/blog", { body: form });
      onSalvo(p);
    } catch (err) {
      setErro((err as ApiError).message);
    } finally {
      setSalvando(false);
    }
  };

  return (
    <form onSubmit={salvar} className="grid gap-4 rounded-xl border p-5">
      {erro && <Alert kind="error">{erro}</Alert>}
      <Field label="Título" name="titulo"><TextInput id="titulo" required value={form.titulo} onChange={set("titulo")} /></Field>
      <Field label="Resumo (opcional)" name="resumo"><TextInput id="resumo" value={form.resumo} onChange={set("resumo")} /></Field>
      <Field label="Conteúdo" name="conteudo"><TextArea id="conteudo" required rows={10} value={form.conteudo} onChange={set("conteudo")} /></Field>
      <Field label="Categoria" name="categoria">
        <Select id="categoria" value={form.categoria} onChange={set("categoria")}>
          {["geral", "historias", "transparencia", "eventos", "parcerias"].map((c) => <option key={c} value={c}>{c}</option>)}
        </Select>
      </Field>
      <Field label="URL da imagem (opcional)" name="imagemUrl"><TextInput id="imagemUrl" type="url" value={form.imagemUrl} onChange={set("imagemUrl")} /></Field>
      <label className="flex items-center gap-2 text-sm">
        <input type="checkbox" checked={form.publicado} onChange={set("publicado")} /> Publicado
      </label>
      <div><PrimaryButton type="submit" disabled={salvando}>{salvando ? "Salvando..." : inicial ? "Salvar" : "Publicar"}</PrimaryButton></div>
    </form>
  );
}
