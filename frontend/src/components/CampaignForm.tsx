"use client";

import { useEffect, useRef, useState } from "react";
import { Alert, DangerButton, Field, PrimaryButton, Select, TextArea, TextInput } from "./ui";
import { useConfirm } from "./useConfirm";
import { api, fileUrl, type ApiError } from "@/lib/api";
import { CATEGORIAS } from "@/lib/categorias";
import type { Campaign } from "@/lib/types";
import { toast } from "sonner";

const urgencias = ["baixa", "media", "alta"];
const MAX_FOTOS = 5;

type FotoNova = { file: File; url: string };

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
    instrucoesEntrega: inicial?.instrucoesEntrega ?? "",
    enderecoEntrega: inicial?.enderecoEntrega ?? "",
  });
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState("");
  const [fotosNovas, setFotosNovas] = useState<FotoNova[]>([]);
  const [progresso, setProgresso] = useState<{ atual: number; total: number } | null>(null);
  const [salva, setSalva] = useState<Campaign | null>(inicial ?? null);
  const inputFotosRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    return () => {
      for (const f of fotosNovas) URL.revokeObjectURL(f.url);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const set = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.type === "checkbox" ? (e as React.ChangeEvent<HTMLInputElement>).target.checked : e.target.value }));

  const adicionarFotos = (files: FileList | null) => {
    if (!files?.length) return;
    const atuais = [...fotosNovas];
    for (const file of Array.from(files)) {
      if (atuais.length >= MAX_FOTOS) {
        toast.error(`Máximo de ${MAX_FOTOS} fotos por vez`);
        break;
      }
      atuais.push({ file, url: URL.createObjectURL(file) });
    }
    setFotosNovas(atuais);
    if (inputFotosRef.current) inputFotosRef.current.value = "";
    // Na edição a campanha já existe: sobe na hora, sem botão extra
    if (inicial && salva) void enviarFotos(atuais, salva.id);
  };

  const removerNova = (url: string) => {
    setFotosNovas((lista) => {
      const alvo = lista.find((f) => f.url === url);
      if (alvo) URL.revokeObjectURL(alvo.url);
      return lista.filter((f) => f.url !== url);
    });
  };

  const subirFotos = async (lista: FotoNova[], campaignId: number) => {
    let atualizada: Campaign | null = null;
    let falhas = 0;
    setProgresso({ atual: 0, total: lista.length });
    for (let i = 0; i < lista.length; i++) {
      setProgresso({ atual: i + 1, total: lista.length });
      const formData = new FormData();
      formData.set("imagem", lista[i].file);
      try {
        atualizada = await api<Campaign>(`/campaigns/${campaignId}/imagens`, { method: "POST", form: formData });
        setSalva(atualizada);
      } catch {
        falhas++;
      }
    }
    setProgresso(null);
    return { atualizada, falhas };
  };

  const enviarFotos = async (lista: FotoNova[], campaignId: number) => {
    const { atualizada, falhas } = await subirFotos(lista, campaignId);
    for (const f of lista) URL.revokeObjectURL(f.url);
    setFotosNovas([]);
    if (atualizada) onSalvo(atualizada);
    if (falhas > 0) {
      toast.error(`${falhas} foto(s) falharam — tente de novo na edição`);
    } else {
      toast.success("Fotos enviadas!");
    }
  };

  const salvar = async (e: React.FormEvent) => {
    e.preventDefault();
    setSalvando(true);
    setErro("");
    try {
      if (inicial) {
        const c = await api<Campaign>(`/campaigns/${inicial.id}`, { method: "PUT", body: form });
        setSalva(c);
        onSalvo(c);
        toast.success("Alterações salvas!");
      } else {
        const c = await api<Campaign>("/campaigns", { body: form });
        setSalva(c);
        if (fotosNovas.length > 0) {
          const { atualizada, falhas } = await subirFotos(fotosNovas, c.id);
          for (const f of fotosNovas) URL.revokeObjectURL(f.url);
          setFotosNovas([]);
          if (falhas > 0) toast.error(`Campanha publicada, mas ${falhas} foto(s) falharam`);
          onSalvo(atualizada ?? c);
        } else {
          onSalvo(c);
        }
        toast.success("Campanha publicada!");
      }
    } catch (err) {
      setErro((err as ApiError).message);
    } finally {
      setSalvando(false);
    }
  };

  const { confirmar, dialog: dialogoConfirm } = useConfirm();

  const removerFoto = async (imgId: number) => {
    if (!(await confirmar({ title: "Remover esta foto?", confirmLabel: "Remover" }))) return;
    try {
      await api(`/campaigns/imagens/${imgId}`, { method: "DELETE" });
      const atualizada = await api<Campaign>(`/campaigns/${salva?.id}`, {});
      setSalva(atualizada);
      onSalvo(atualizada);
    } catch (err) {
      setErro((err as ApiError).message);
    }
  };

  const enviando = progresso !== null;

  return (
    <div className="space-y-4">
      {erro && <Alert kind="error">{erro}</Alert>}
      <form onSubmit={salvar} className="grid gap-4 rounded-xl border p-5 sm:grid-cols-2">
        <div className="sm:col-span-2"><Field label="Título" name="titulo"><TextInput id="titulo" required value={form.titulo} onChange={set("titulo")} /></Field></div>
        <div className="sm:col-span-2"><Field label="Descrição" name="descricao"><TextArea id="descricao" required rows={5} value={form.descricao} onChange={set("descricao")} /></Field></div>
        <Field label="Categoria" name="categoria">
          <Select id="categoria" value={form.categoria} onChange={set("categoria")}>
            {CATEGORIAS.map((c) => <option key={c.value} value={c.value}>{c.label}</option>)}
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
        <div className="sm:col-span-2"><Field label="Instruções de entrega (opcional)" name="instrucoesEntrega"><TextArea id="instrucoesEntrega" rows={2} value={form.instrucoesEntrega} onChange={set("instrucoesEntrega")} placeholder="Ex.: entregas seg–sex 9h–17h; acima de 20 cestas buscamos no local" /></Field></div>
        <div className="sm:col-span-2"><Field label="Endereço de entrega (opcional — vazio usa o da instituição)" name="enderecoEntrega"><TextInput id="enderecoEntrega" value={form.enderecoEntrega} onChange={set("enderecoEntrega")} placeholder="Rua, número, bairro, cidade" /></Field></div>

        <div className="sm:col-span-2 rounded-xl bg-gray-50 p-4">
          <p className="text-sm font-semibold">📷 Fotos da campanha <span className="font-normal text-gray-500">(png, jpg, gif, webp — até 5MB cada, máx. {MAX_FOTOS})</span></p>
          {(salva?.imagens?.length ?? 0) > 0 && (
            <ul className="mt-2 flex flex-wrap gap-2">
              {salva!.imagens.map((img) => {
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
          {fotosNovas.length > 0 && (
            <ul className="mt-2 flex flex-wrap gap-2">
              {fotosNovas.map((f) => (
                <li key={f.url} className="relative">
                  <img src={f.url} alt="Prévia da foto" className="h-24 w-24 rounded-lg object-cover ring-2 ring-[var(--primary)]" />
                  <DangerButton onClick={() => removerNova(f.url)} aria-label="Descartar foto" className="absolute right-1 top-1 px-2 py-1 text-xs">✕</DangerButton>
                </li>
              ))}
            </ul>
          )}
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <label htmlFor="fotos" className="cursor-pointer rounded-lg border border-dashed border-[var(--primary)] px-4 py-2 text-sm font-semibold text-[var(--primary)] hover:bg-white">
              + Adicionar fotos
            </label>
            <input ref={inputFotosRef} id="fotos" type="file" accept="image/*" multiple onChange={(e) => adicionarFotos(e.target.files)} className="sr-only" />
            {enviando && progresso && (
              <p role="status" className="text-xs font-semibold text-[var(--primary)]">
                Enviando foto {progresso.atual} de {progresso.total}...
              </p>
            )}
          </div>
        </div>

        <div className="sm:col-span-2">
          <PrimaryButton type="submit" disabled={salvando || enviando}>
            {salvando || enviando
              ? (progresso ? `Enviando foto ${progresso.atual} de ${progresso.total}...` : "Salvando...")
              : inicial ? "Salvar alterações" : `Publicar campanha${fotosNovas.length > 0 ? ` com ${fotosNovas.length} foto(s)` : ""}`}
          </PrimaryButton>
        </div>
      </form>
      {dialogoConfirm}
    </div>
  );
}
