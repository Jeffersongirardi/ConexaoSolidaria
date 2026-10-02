"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { Alert, Field, PrimaryButton, Spinner, TextArea, TextInput, UrgenciaBadge, categoriaIcone, formatarData } from "@/components/ui";
import Voltar from "@/components/Voltar";
import { api, fileUrl, type ApiError } from "@/lib/api";
import { categoriaLabel } from "@/lib/categorias";
import { waLink } from "@/lib/whatsapp";
import type { Donation } from "@/lib/types";
import { useAuth } from "@/lib/auth";
import type { Campaign } from "@/lib/types";
import { toast } from "sonner";

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
  const [precisaColeta, setPrecisaColeta] = useState(false);
  const [enderecoColeta, setEnderecoColeta] = useState("");
  const [doando, setDoando] = useState(false);
  const [msgDoacao, setMsgDoacao] = useState<{ kind: "error" | "success"; text: string } | null>(null);
  const [ultimaDoacao, setUltimaDoacao] = useState<Donation | null>(null);

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
      const nova = await api<Donation>("/donations", {
        body: {
          campaignId: campaign.id, item, quantidade, observacao,
          precisaColeta, enderecoColeta: precisaColeta ? enderecoColeta : "",
        },
      });
      setUltimaDoacao(nova);
      setMsgDoacao({ kind: "success", text: `Intenção #${nova.id} registrada! Siga os passos abaixo para entregar.` });
      setItem("");
      setQuantidade("");
      setObservacao("");
      setPrecisaColeta(false);
      setEnderecoColeta("");
    } catch (err) {
      setMsgDoacao({ kind: "error", text: (err as ApiError).message });
    } finally {
      setDoando(false);
    }
  };

  const doarValor = async (e: React.FormEvent) => {
    e.preventDefault();
    const num = Number(valor.replace(",", "."));
    if (!num || Number.isNaN(num) || num < 0.01) {
      setMsgDoacao({ kind: "error", text: "Informe um valor válido (mín. R$ 0,01)." });
      return;
    }
    if (!user) {
      router.push("/login?origem=/campanhas/" + id);
      return;
    }
    setDoando(true);
    setMsgDoacao(null);
    try {
      const pagamento = await api<{ uuid: string }>("/payments", {
        body: { campaignId: campaign.id, valor: num, metodo },
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
    <div>
      <Voltar fallback="/campanhas" />
      <div className="mt-2 grid gap-8 lg:grid-cols-3">
      <article className="lg:col-span-2">
        <div className="flex flex-wrap items-center gap-2">
          <UrgenciaBadge urgencia={campaign.urgencia} />
          <span className="text-sm text-[var(--text-soft)]">{categoriaIcone(campaign.categoria)}</span>
        </div>
        <h1 className="mt-2 text-3xl font-bold tracking-tight">{campaign.titulo}</h1>
        <p className="mt-1 text-sm text-[var(--text-soft)]">
          Por{" "}
          <Link href={`/instituicoes/${campaign.instituicao?.id}`} className="text-[var(--primary)] hover:underline">
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
                      className={`overflow-hidden rounded-lg border-2 ${i === fotoAtiva ? "border-[var(--primary)]" : "border-transparent"}`}>
                      <img src={url} alt="" className="h-16 w-16 object-cover" />
                    </button>
                  ) : null;
                })}
              </div>
            )}
          </figure>
        ) : null}

        <p className="mt-4 whitespace-pre-line leading-relaxed">{campaign.descricao}</p>

          <dl className="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-3">
          <div className="rounded-xl border border-[var(--border)] bg-[var(--surface)] p-3 text-center">
            <dt className="text-xs text-[var(--text-soft)]">Doadores</dt>
            <dd className="text-xl font-bold text-[var(--primary)]">👥 {campaign.numDoadores ?? 0}</dd>
          </div>
          <div className="rounded-xl border border-[var(--border)] bg-[var(--surface)] p-3 text-center">
            <dt className="text-xs text-[var(--text-soft)]">Em valores</dt>
            <dd className="text-xl font-bold text-[var(--primary)]">R$ {Number(campaign.valorRecebido ?? 0).toFixed(2)}</dd>
          </div>
          <div className="rounded-xl border border-[var(--border)] bg-[var(--surface)] p-3 text-center">
            <dt className="text-xs text-[var(--text-soft)]">Itens recebidos</dt>
            <dd className="text-xl font-bold text-[var(--primary)]">🎁 {campaign.numDoacoesItens ?? 0}</dd>
          </div>
        </dl>
        <p className="mt-2 text-sm text-[var(--text-soft)]">
          Meta: {campaign.quantidadeAlvo}
          {campaign.itensPorCategoria && Object.keys(campaign.itensPorCategoria).length > 0 && (
            <> · {Object.entries(campaign.itensPorCategoria).map(([c, n]) => `${categoriaLabel(c)} (${n})`).join(" · ")}</>
          )}
        </p>
        <p className="mt-1 text-xs text-[var(--text-soft)]">Doadores: todos que apoiaram (intenções ativas). Valores e itens: só o confirmado pela instituição.</p>

        <nav className="mt-4 flex flex-wrap gap-2" aria-label="Compartilhar">
          <span className="text-sm font-medium">Compartilhar:</span>
          <a className="text-sm text-[var(--primary)] hover:underline" target="_blank" rel="noopener noreferrer"
            href={`https://wa.me/?text=${encodeURIComponent(`Apoie: ${campaign.titulo} — ${typeof window !== "undefined" ? window.location.href : ""}`)}`}>WhatsApp</a>
          <a className="text-sm text-[var(--primary)] hover:underline" target="_blank" rel="noopener noreferrer"
            href={`https://www.facebook.com/sharer/sharer.php?u=${encodeURIComponent(typeof window !== "undefined" ? window.location.href : "")}`}>Facebook</a>
          <button className="text-sm text-[var(--primary)] hover:underline" onClick={async () => { try { await navigator.clipboard.writeText(window.location.href); toast.success("Link copiado!"); } catch { toast.error("Não foi possível copiar."); } }}>
            Copiar link
          </button>
        </nav>
      </article>

      <aside aria-label="Doar" className="space-y-4 lg:sticky lg:top-[4.5rem] lg:self-start">
        {msgDoacao && <Alert kind={msgDoacao.kind}>{msgDoacao.text}</Alert>}
        <form onSubmit={doarItem} className="rounded-xl border p-4">
          <h2 className="font-bold">🎁 Doar itens</h2>
          <div className="mt-3 space-y-3">
            <Field label="Item" name="item"><TextInput id="item" required value={item} onChange={(e) => setItem(e.target.value)} placeholder="Ex.: arroz, cobertor" /></Field>
            <Field label="Quantidade" name="quantidade"><TextInput id="quantidade" required value={quantidade} onChange={(e) => setQuantidade(e.target.value)} placeholder="Ex.: 10 kg" /></Field>
            <Field label="Observação (opcional)" name="observacao"><TextArea id="observacao" rows={2} value={observacao} onChange={(e) => setObservacao(e.target.value)} /></Field>
            <label className="flex items-start gap-2 text-sm">
              <input type="checkbox" checked={precisaColeta} onChange={(e) => setPrecisaColeta(e.target.checked)} className="mt-1" />
              <span>Preciso que busquem (grandes volumes)</span>
            </label>
            {precisaColeta && (
              <Field label="Endereço para coleta" name="enderecoColeta"><TextInput id="enderecoColeta" required value={enderecoColeta} onChange={(e) => setEnderecoColeta(e.target.value)} placeholder="Rua, número, bairro, cidade" /></Field>
            )}
            <PrimaryButton type="submit" disabled={doando} className="w-full">
              {doando ? "Registrando..." : user ? "Registrar intenção de doação" : "Entrar para doar"}
            </PrimaryButton>
          </div>
        </form>
        {ultimaDoacao && (
          <div className="rounded-xl border border-green-200 bg-green-50 p-4 text-sm" aria-live="polite">
            <h2 className="font-bold text-green-800">✅ Intenção #{ultimaDoacao.id} registrada!</h2>
            <ol className="mt-2 list-decimal space-y-1 pl-5 text-green-900">
              <li>
                Chame a instituição no WhatsApp
                {waLink(ultimaDoacao.instituicaoWhatsapp, `Olá! Registrei a doação #${ultimaDoacao.id} (${ultimaDoacao.quantidade} de ${ultimaDoacao.item}) na campanha "${ultimaDoacao.campaignTitulo}". Como combinamos a entrega?`) ? (
                  <> — <a href={waLink(ultimaDoacao.instituicaoWhatsapp, `Olá! Registrei a doação #${ultimaDoacao.id} (${ultimaDoacao.quantidade} de ${ultimaDoacao.item}) na campanha "${ultimaDoacao.campaignTitulo}". Como combinamos a entrega?`)!} target="_blank" rel="noopener noreferrer" className="font-semibold underline">abrir conversa</a></>
                ) : (" (número em breve no seu painel)")}
              </li>
              <li>
                {ultimaDoacao.precisaColeta
                  ? <>Aguarde o contato para a <strong>coleta</strong>{ultimaDoacao.enderecoColeta ? <> em {ultimaDoacao.enderecoColeta}</> : null}.</>
                  : <>Entregue em <strong>{ultimaDoacao.instituicaoEndereco ?? "endereço a combinar"}</strong>{ultimaDoacao.instrucoesEntrega ? <> — {ultimaDoacao.instrucoesEntrega}</> : null}.</>}
              </li>
              <li>Aguarde a confirmação de recebimento aqui no <Link href="/painel/doador" className="font-semibold underline">seu painel</Link>.</li>
            </ol>
          </div>
        )}

        <div className="rounded-xl border border-amber-200 bg-amber-50 p-3 text-xs text-amber-800">
          <p><strong>R$ 20 = 4 marmitas 🍲 · R$ 50 = 1 cesta 🧺.</strong> Pix direto à instituição, taxa R$ 0. Sem reembolso pela plataforma.</p>
        </div>
        {campaign.aceitaFinanceiro && (
          <form onSubmit={doarValor} className="rounded-xl border p-4">
            <h2 className="font-bold">💰 Doar agora com valor</h2>
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
                {doando ? "Aguarde..." : "Doar agora →"}
              </PrimaryButton>
            </div>
          </form>
        )}
      </aside>
      </div>
    </div>
  );
}
