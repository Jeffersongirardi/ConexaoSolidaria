"use client";

import { useRef, useState } from "react";
import { Alert, Field, PrimaryButton, Select, TextArea, TextInput } from "./ui";
import { api, type ApiError } from "@/lib/api";
import { CATEGORIAS } from "@/lib/categorias";
import { ESTADOS_OFERTA } from "@/lib/ofertas";
import type { Oferta } from "@/lib/types";

const TIPOS_OK = ["image/png", "image/jpeg", "image/gif", "image/webp"];
const MAX_BYTES = 5 * 1024 * 1024;
const MAX_FOTOS = 5;

export default function OfertaForm({
  inicial,
  onSalvo,
  textoBotao,
}: {
  inicial?: Oferta | null;
  onSalvo: (o: Oferta) => void;
  textoBotao?: string;
}) {
  const [form, setForm] = useState({
    titulo: inicial?.titulo ?? "",
    descricao: inicial?.descricao ?? "",
    categoria: inicial?.categoria ?? "movel",
    estadoItem: inicial?.estadoItem ?? "usado",
    cidade: inicial?.cidade ?? "",
    precisaColeta: inicial?.precisaColeta ?? false,
    enderecoColeta: inicial?.enderecoColeta ?? "",
    disponivelAte: inicial?.disponivelAte ?? "",
  });
  const [fotos, setFotos] = useState<{ file: File; url: string }[]>([]);
  const [enviando, setEnviando] = useState(false);
  const [progressoFoto, setProgressoFoto] = useState("");
  const [erro, setErro] = useState("");
  const inputRef = useRef<HTMLInputElement>(null);

  const set = (k: string) => (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) =>
    setForm((f) => ({ ...f, [k]: e.target.type === "checkbox" ? (e as React.ChangeEvent<HTMLInputElement>).target.checked : e.target.value }));

  const escolherFotos = (files: FileList | null) => {
    if (!files?.length) return;
    const validas: { file: File; url: string }[] = [];
    for (const f of Array.from(files)) {
      if (!TIPOS_OK.includes(f.type.toLowerCase())) {
        setErro(`"${f.name}" não é suportado (use png, jpg, gif ou webp).`);
        continue;
      }
      if (f.size > MAX_BYTES) {
        setErro(`"${f.name}" passa de 5MB.`);
        continue;
      }
      if (fotos.length + validas.length >= MAX_FOTOS) {
        setErro(`Máximo de ${MAX_FOTOS} fotos.`);
        break;
      }
      validas.push({ file: f, url: URL.createObjectURL(f) });
    }
    if (validas.length > 0) {
      setErro("");
      setFotos((atual) => [...atual, ...validas]);
    }
    if (inputRef.current) inputRef.current.value = "";
  };

  const removerFoto = (url: string) => {
    setFotos((atual) => {
      const alvo = atual.find((f) => f.url === url);
      if (alvo) URL.revokeObjectURL(alvo.url);
      return atual.filter((f) => f.url !== url);
    });
  };

  const enviar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.disponivelAte) {
      setErro("Informe até quando o item fica disponível — é seu compromisso.");
      return;
    }
    setEnviando(true);
    setErro("");
    try {
      const oferta = inicial
        ? await api<Oferta>(`/ofertas/${inicial.id}`, { method: "PUT", body: form })
        : await api<Oferta>("/ofertas", { body: form });
      let falhas = 0;
      for (let i = 0; i < fotos.length; i++) {
        setProgressoFoto(`Enviando foto ${i + 1} de ${fotos.length}...`);
        const fd = new FormData();
        fd.set("imagem", fotos[i].file);
        try {
          await api(`/ofertas/${oferta.id}/imagens`, { method: "POST", form: fd });
        } catch {
          falhas++;
        }
      }
      setProgressoFoto("");
      for (const f of fotos) URL.revokeObjectURL(f.url);
      setFotos([]);
      if (falhas > 0) throw new Error(`${falhas} foto(s) falharam — tente de novo depois`);
      onSalvo(oferta);
    } catch (err) {
      setErro((err as ApiError).message ?? String(err));
    } finally {
      setEnviando(false);
      setProgressoFoto("");
    }
  };

  return (
    <form onSubmit={enviar} className="grid gap-4 rounded-xl border p-5 sm:grid-cols-2">
      {erro && <div className="sm:col-span-2"><Alert kind="error">{erro}</Alert></div>}
      <div className="sm:col-span-2"><Field label="O que você tem?" name="titulo"><TextInput id="titulo" required value={form.titulo} onChange={set("titulo")} placeholder="Ex.: sofá 3 lugares" /></Field></div>
      <div className="sm:col-span-2"><Field label="Descrição completa (medidas, defeitos, como retirar)" name="descricao"><TextArea id="descricao" required rows={4} value={form.descricao} onChange={set("descricao")} /></Field></div>
      <Field label="Categoria" name="categoria">
        <Select id="categoria" value={form.categoria} onChange={set("categoria")}>
          {CATEGORIAS.map((c) => <option key={c.value} value={c.value}>{c.label}</option>)}
        </Select>
      </Field>
      <Field label="Estado" name="estadoItem">
        <Select id="estadoItem" value={form.estadoItem} onChange={set("estadoItem")}>
          {ESTADOS_OFERTA.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
        </Select>
      </Field>
      <Field label="Cidade" name="cidade"><TextInput id="cidade" value={form.cidade} onChange={set("cidade")} placeholder="Ex.: Curitiba/PR" /></Field>
      <Field label="Disponível até (compromisso)" name="disponivelAte"><TextInput id="disponivelAte" type="date" required value={form.disponivelAte} onChange={set("disponivelAte")} /></Field>
      <div className="sm:col-span-2">
        <label className="flex items-start gap-2 text-sm">
          <input type="checkbox" checked={form.precisaColeta} onChange={set("precisaColeta")} className="mt-1" />
          <span>Precisa de coleta no meu endereço (a instituição combina a retirada)</span>
        </label>
      </div>
      {form.precisaColeta && (
        <div className="sm:col-span-2"><Field label="Endereço para coleta" name="enderecoColeta"><TextInput id="enderecoColeta" required value={form.enderecoColeta} onChange={set("enderecoColeta")} placeholder="Rua, número, bairro" /></Field></div>
      )}
      <div className="sm:col-span-2">
        <label htmlFor="fotos-oferta" className="cursor-pointer rounded-lg border border-dashed border-[var(--primary)] px-4 py-2 text-sm font-semibold text-[var(--primary)] hover:bg-white">
          + Adicionar fotos {fotos.length > 0 ? `(${fotos.length}/${MAX_FOTOS})` : ""}
        </label>
        <input ref={inputRef} id="fotos-oferta" type="file" accept="image/png,image/jpeg,image/gif,image/webp" multiple className="sr-only"
          onChange={(e) => escolherFotos(e.target.files)} />
        <p className="mt-1 text-xs text-gray-500">png, jpg, gif ou webp — até 5MB cada (fotos HEIC de iPhone não são aceitas).</p>
        {fotos.length > 0 && (
          <ul className="mt-2 flex flex-wrap gap-2">
            {fotos.map((f) => (
              <li key={f.url} className="relative">
                <img src={f.url} alt="Prévia" className="h-24 w-24 rounded-lg object-cover ring-2 ring-[var(--primary)]" />
                <button type="button" onClick={() => removerFoto(f.url)} aria-label="Descartar foto" className="absolute right-1 top-1 rounded bg-red-600 px-2 py-1 text-xs text-white">✕</button>
              </li>
            ))}
          </ul>
        )}
        {progressoFoto && <p role="status" className="mt-1 text-xs font-semibold text-[var(--primary)]">{progressoFoto}</p>}
      </div>
      <div className="sm:col-span-2">
        <PrimaryButton type="submit" disabled={enviando} className="w-full">
          {enviando ? (progressoFoto || "Salvando...") : (textoBotao ?? "Publicar oferta")}
        </PrimaryButton>
      </div>
    </form>
  );
}
