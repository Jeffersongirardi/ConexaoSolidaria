"use client";

import { useState } from "react";
import { Alert, DangerButton, Field, PrimaryButton, SecondaryButton, Select, TextArea, TextInput } from "./ui";
import { api, fileUrl, type ApiError } from "@/lib/api";
import type { Campaign } from "@/lib/types";

const categorias = ["alimento", "roupa", "higiene", "material_escolar", "outro"];
const urgencias = ["baixa", "media", "alta"];

export default function CampaignForm({
  inicial,
  onSalvo,
}: {
  inicial?: Campaign | null;
  onSalvo: (c: Campaign) => void;
}) {
  const [form, setForm] = useState({
    titulo: inicial?.titulo ?? "",
    descricao: inicial?.descricao ?? "",
    categoria: inicial?.categoria ?? "alimento",
    quantidadeAlvo: inicial?.quantidadeAlvo ?? "",
    urgencia: inicial?.urgencia ?? "media",
    aceitaFinanceiro: inicial?.aceitaFinanceiro ?? false,
  });
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState("");
  const [fotos, setFotos] = useState<FileList | null>(null);
  const [enviandoFoto, setEnviandoFoto] = useState(false);
  const [salva, setSalva] = useState<Campaign | null>(inicial ?? null);

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.type === "checkbox" ? (e as React.ChangeEvent<HTMLInputElement>).target.checked : e.target.value }));

  const salvar = async (e: React.FormEvent) => {
    e.preventDefault();
    setSalvando(true);
    setErro("");
    try {
      const c = inicial
        ? await api<Campaign>(`/campaigns/${inicial.id}`, { method: "PUT", body: form })
        : await api<Campaign>("/campaigns", { body: form });
      setSalva(c);
      onSalvo(c);
    } catch (err) {
      setErro((err as ApiError).message);
    } finally {
      setSalvando(false);
    }
  };

  const enviarFotos = async () => {
    if (!salva || !fotos?.length) return;
    setEnviandoFoto(true);
    setErro("");
    try {
      for (const foto of Array.from(fotos)) {
        const formData = new FormData();
        formData.set("imagem", foto);
        const atualizada = await api<Campaign>(`/campaigns/${salva.id}/imagens`, { method: "POST", form: formData });
        setSalva(atualizada);
        onSalvo(atualizada);
      }
      setFotos(null);
    } catch (err) {
      setErro((err as ApiError).message);
    } finally {
      setEnviandoFoto(false);
    }
  };

  const removerFoto = async (imgId: number) => {
    if (!confirm("Remover esta foto?")) return;
    try {
      await api(`/campaigns/imagens/${imgId}`, { method: "DELETE" });
      const atualizada = await api<Campaign>(`/campaigns/${salva?.id}`, {});
      setSalva(atualizada);
      onSalvo(atualizada);
    } catch (err) {
      setErro((err as ApiError).message);
    }
  };

  return (
    <div className="space-y-4">
      {erro && <Alert kind="error">{erro}</Alert>}
      <form onSubmit={salvar} className="grid gap-4 rounded-xl border p-5 sm:grid-cols-2">
        <div className="sm:col-span-2"><Field label="Título" name="titulo"><TextInput id="titulo" required value={form.titulo} onChange={set("titulo")} /></Field></div>
        <div className="sm:col-span-2"><Field label="Descrição" name="descricao"><TextArea id="descricao" required rows={5} value={form.descricao} onChange={set("descricao")} /></Field></div>
        <Field label="Categoria" name="categoria">
          <Select id="categoria" value={form.categoria} onChange={set("categoria")}>
            {categorias.map((c) => <option key={c} value={c}>{c.replace("_", " ")}</option>)}
          </Select>
        </Field>
        <Field label="Urgência" name="urgencia">
          <Select id="urgencia" value={form.urgencia} onChange={set("urgencia")}>
            {urgencias.map((u) => <option key={u} value={u}>{u}</option>)}
          </Select>
        </Field>
        <div className="sm:col-span-2"><Field label="Meta / quantidade alvo" name="quantidadeAlvo"><TextInput id="quantidadeAlvo" required value={form.quantidadeAlvo} onChange={set("quantidadeAlvo")} placeholder="Ex.: 100 cestas básicas" /></Field></div>
        <div className="sm:col-span-2">
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" checked={form.aceitaFinanceiro} onChange={set("aceitaFinanceiro")} />
            Aceita contribuição financeira (Pix/cartão/transferência)
          </label>
        </div>
        <div className="sm:col-span-2">
          <PrimaryButton type="submit" disabled={salvando}>
            {salvando ? "Salvando..." : inicial ? "Salvar alterações" : "Publicar campanha"}
          </PrimaryButton>
        </div>
      </form>

      {salva && (
        <section aria-label="Fotos da campanha" className="rounded-xl border p-5">
          <h2 className="font-bold">Fotos</h2>
          {(salva.imagens?.length ?? 0) > 0 && (
            <ul className="mt-2 flex flex-wrap gap-2">
              {salva.imagens.map((img) => {
                const url = fileUrl(img.url);
                return url ? (
                  <li key={img.id} className="relative">
                    <img src={url} alt="" className="h-24 w-24 rounded-lg object-cover" />
                    <DangerButton onClick={() => void removerFoto(img.id)} aria-label="Remover foto" className="absolute right-1 top-1 px-2 py-1 text-xs">✕</DangerButton>
                  </li>
                ) : null;
              })}
            </ul>
          )}
          <div className="mt-3 flex flex-wrap items-center gap-2">
            <label htmlFor="fotos" className="text-sm font-medium">Adicionar fotos (png/jpg/gif/webp até 5MB)</label>
            <input id="fotos" type="file" accept="image/*" multiple onChange={(e) => setFotos(e.target.files)} className="text-sm" />
            <SecondaryButton onClick={() => void enviarFotos()} disabled={!fotos?.length || enviandoFoto}>
              {enviandoFoto ? "Enviando..." : "Enviar fotos"}
            </SecondaryButton>
          </div>
        </section>
      )}
    </div>
  );
}
