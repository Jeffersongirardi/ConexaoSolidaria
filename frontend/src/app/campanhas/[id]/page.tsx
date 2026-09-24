"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Alert, Field, PrimaryButton, Spinner, TextArea, TextInput, UrgenciaBadge, categoriaIcone, formatarData } from "@/components/ui";
import { api, fileUrl, type ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import type { Campaign } from "@/lib/types";

export default function CampanhaDetalhe({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const { user } = useAuth();
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [erro, setErro] = useState("");
  const [fotoAtiva, setFotoAtiva] = useState(0);

  const [item, setItem] = useState("");
  const [quantidade, setQuantidade] = useState("");
  const [observacao, setObservacao] = useState("");
  const [doando, setDoando] = useState(false);
  const [msgDoacao, setMsgDoacao] = useState<{ kind: "error" | "success"; text: string } | null>(null);

  const [valor, setValor] = useState("");
  const [metodo, setMetodo] = useState("pix");

  useEffect(() => {
    api<Campaign>(`/campaigns/${id}`, { token: null })
      .then(setCampaign)
      .catch(() => setErro("Campanha não encontrada."));
  }, [id]);

  if (erro) return <div className="mt-4"><Alert kind="error">{erro}</Alert></div>;
  if (!campaign) return <Spinner />;

  const doarItem = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) {
      router.push("/login?origem=/campanhas/" + id);
      return;
    }
    setDoando(true);
    setMsgDoacao(null);
    try {
      await api("/donations", { body: { campaignId: campaign.id, item, quantidade, observacao } });
      setMsgDoacao({ kind: "success", text: "Intenção registrada! Acompanhe no seu painel." });
      setItem("");
      setQuantidade("");
      setObservacao("");
    } catch (err) {
      setMsgDoacao({ kind: "error", text: (err as ApiError).message });
    } finally {
      setDoando(false);
    }
  };

  const doarValor = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) {
      router.push("/login?origem=/campanhas/" + id);
      return;
    }
    setDoando(true);
    setMsgDoacao(null);
    try {
      const pagamento = await api<{ uuid: string }>("/payments", {
        body: { campaignId: campaign.id, valor: Number(valor), metodo },
      });
      router.push(`/pagamento/${pagamento.uuid}`);
    } catch (err) {
      setMsgDoacao({ kind: "error", text: (err as ApiError).message });
      setDoando(false);
    }
  };

  const fotos = campaign.imagens ?? [];
  const fotoPrincipal = fotos[fotoAtiva]?.url ? fileUrl(fotos[fotoAtiva].url) : null;

  return (
    <div className="grid gap-8 lg:grid-cols-3">
      <article className="lg:col-span-2">
        <div className="flex flex-wrap items-center gap-2">
          <UrgenciaBadge urgencia={campaign.urgencia} />
          <span className="text-sm text-gray-500">{categoriaIcone(campaign.categoria)} {campaign.categoria.replace("_", " ")}</span>
        </div>
        <h1 className="mt-2 text-3xl font-bold">{campaign.titulo}</h1>
        <p className="mt-1 text-sm text-gray-600">
          Por{" "}
          <Link href={`/instituicoes/${campaign.instituicao?.id}`} className="text-blue-700 hover:underline">
            {campaign.instituicao?.nomeFantasia || campaign.instituicao?.razaoSocial}
          </Link>{" "}
          · desde {formatarData(campaign.dataCriacao)}
        </p>

        {fotoPrincipal ? (
          <figure className="mt-4">
            <img src={fotoPrincipal} alt={`Foto da campanha ${campaign.titulo}`} className="max-h-96 w-full rounded-xl object-cover" />
            {fotos.length > 1 && (
              <div className="mt-2 flex gap-2" role="group" aria-label="Outras fotos">
                {fotos.map((f, i) => {
                  const url = fileUrl(f.url);
                  return url ? (
                    <button key={f.id} onClick={() => setFotoAtiva(i)} aria-label={`Ver foto ${i + 1}`} aria-pressed={i === fotoAtiva}
                      className={`overflow-hidden rounded-lg border-2 ${i === fotoAtiva ? "border-blue-600" : "border-transparent"}`}>
                      <img src={url} alt="" className="h-16 w-16 object-cover" />
                    </button>
                  ) : null;
                })}
              </div>
            )}
          </figure>
        ) : null}

        <p className="mt-4 whitespace-pre-line leading-relaxed">{campaign.descricao}</p>

        <div className="mt-4 rounded-xl border p-4">
          <p className="text-sm font-medium">🎯 Meta: {campaign.quantidadeAlvo}</p>
          <div className="mt-2 h-3 overflow-hidden rounded-full bg-gray-200" role="progressbar" aria-valuenow={campaign.progresso ?? 0} aria-valuemin={0} aria-valuemax={100} aria-label={`Progresso: ${campaign.progresso ?? 0}%`}>
            <div className="h-full bg-green-500" style={{ width: `${Math.min(100, campaign.progresso ?? 0)}%` }} />
          </div>
          <p className="mt-1 text-sm text-gray-600">{campaign.progresso ?? 0}% arrecadado</p>
        </div>

        <div className="mt-4 flex flex-wrap gap-2" aria-label="Compartilhar">
          <span className="text-sm font-medium">Compartilhar:</span>
          <a className="text-sm text-blue-700 hover:underline" target="_blank" rel="noopener noreferrer"
            href={`https://wa.me/?text=${encodeURIComponent(`Apoie: ${campaign.titulo}`)}`}>WhatsApp</a>
          <a className="text-sm text-blue-700 hover:underline" target="_blank" rel="noopener noreferrer"
            href={`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(typeof window !== "undefined" ? window.location.href : "")}`}>Facebook</a>
          <button className="text-sm text-blue-700 hover:underline" onClick={() => navigator.clipboard?.writeText(window.location.href)}>
            Copiar link
          </button>
        </div>
      </article>

      <aside aria-label="Doar" className="space-y-4">
        {msgDoacao && <Alert kind={msgDoacao.kind}>{msgDoacao.text}</Alert>}
        <form onSubmit={doarItem} className="rounded-xl border p-4">
          <h2 className="font-bold">🎁 Doar itens</h2>
          <div className="mt-3 space-y-3">
            <Field label="Item" name="item"><TextInput id="item" required value={item} onChange={(e) => setItem(e.target.value)} placeholder="Ex.: arroz, cobertor" /></Field>
            <Field label="Quantidade" name="quantidade"><TextInput id="quantidade" required value={quantidade} onChange={(e) => setQuantidade(e.target.value)} placeholder="Ex.: 10 kg" /></Field>
            <Field label="Observação (opcional)" name="observacao"><TextArea id="observacao" rows={2} value={observacao} onChange={(e) => setObservacao(e.target.value)} /></Field>
            <PrimaryButton type="submit" disabled={doando} className="w-full">
              {doando ? "Registrando..." : user ? "Registrar intenção de doação" : "Entrar para doar"}
            </PrimaryButton>
          </div>
        </form>

        {campaign.aceitaFinanceiro && (
          <form onSubmit={doarValor} className="rounded-xl border p-4">
            <h2 className="font-bold">💰 Contribuir com valor</h2>
            <div className="mt-3 space-y-3">
              <Field label="Valor (R$)" name="valor">
                <TextInput id="valor" required inputMode="decimal" value={valor} onChange={(e) => setValor(e.target.value.replace(",", "."))} placeholder="Ex.: 50.00" />
              </Field>
              <Field label="Método" name="metodo">
                <select id="metodo" value={metodo} onChange={(e) => setMetodo(e.target.value)} className="w-full rounded-lg border border-gray-300 px-3 py-2 text-sm">
                  <option value="pix">Pix</option>
                  <option value="cartao">Cartão</option>
                  <option value="transferencia">Transferência</option>
                </select>
              </Field>
              <PrimaryButton type="submit" disabled={doando} className="w-full">
                {doando ? "Aguarde..." : "Continuar para pagamento"}
              </PrimaryButton>
            </div>
          </form>
        )}
      </aside>
    </div>
  );
}
