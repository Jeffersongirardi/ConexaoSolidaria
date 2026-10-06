"use client";

import { use, useCallback, useEffect, useState } from "react";
import Link from "next/link";
import { Alert, PrimaryButton, SecondaryButton, Spinner } from "@/components/ui";
import CancelarOferta from "@/components/CancelarOferta";
import Voltar from "@/components/Voltar";
import SafeImage from "@/components/SafeImage";
import { useConfirm } from "@/components/useConfirm";
import { api, fileUrl, type ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { categoriaLabel } from "@/lib/categorias";
import { dataPrevistaColeta, diasAtrasoColeta } from "@/lib/ofertas";
import { waLink } from "@/lib/whatsapp";
import type { Oferta } from "@/lib/types";
import { estadoOfertaLabel } from "@/app/ofertas/page";
import { toast } from "sonner";

function OfertaDetalhe({ id }: { id: string }) {
  const { user } = useAuth();
  const [oferta, setOferta] = useState<Oferta | null>(null);
  const [erro, setErro] = useState("");
  const [acao, setAcao] = useState(false);
  const { confirmar, dialog: dialogoConfirm } = useConfirm();

  const carregar = useCallback(async () => {
    try {
      setOferta(await api<Oferta>(`/ofertas/${id}`));
    } catch {
      setErro("Oferta não encontrada ou sem permissão.");
    }
  }, [id]);

  useEffect(() => {
    void carregar();
  }, [carregar]);

  const reivindicar = async () => {
    if (!aceite) {
      toast.error("Marque o compromisso de coleta para reivindicar");
      return;
    }
    setAcao(true);
    try {
      const atualizada = await api<Oferta>(`/ofertas/${id}/reivindicar`, { method: "POST", body: {} });
      setOferta(atualizada);
      toast.success("Oferta reservada! Colete em até 7 dias.");
    } catch (err) {
      toast.error((err as ApiError).message);
    } finally {
      setAcao(false);
    }
  };

  const confirmarRecebimento = async () => {
    if (!oferta) return;
    const emAtraso = diasAtrasoColeta(oferta.prazoColeta);
    if (emAtraso > 0 && !(await confirmar({ title: "Confirmar com atraso?", description: `A coleta está ${emAtraso} ${emAtraso === 1 ? "dia" : "dias"} atrasada. O doador será avisado. Deseja confirmar mesmo assim?`, confirmLabel: "Confirmar mesmo assim" }))) return;
    setAcao(true);
    try {
      const atualizada = await api<Oferta>(`/ofertas/${id}/confirmar-recebimento`, { method: "POST", body: {} });
      setOferta(atualizada);
      toast.success(emAtraso > 0 ? "Recebimento confirmado com atraso registrado." : "Recebimento confirmado. Obrigado!");
    } catch (err) {
      toast.error((err as ApiError).message);
    } finally {
      setAcao(false);
    }
  };

  const liberar = async () => {
    if (!(await confirmar({ title: "Liberar oferta de novo?", description: "Avisaremos que não foi coletado e ela volta a ficar disponível.", confirmLabel: "Liberar" }))) return;
    setAcao(true);
    try {
      const atualizada = await api<Oferta>(`/ofertas/${id}/liberar`, { method: "POST", body: {} });
      setOferta(atualizada);
      toast.success("Oferta disponível novamente.");
    } catch (err) {
      toast.error((err as ApiError).message);
    } finally {
      setAcao(false);
    }
  };

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!oferta) return <Spinner />;

  const ehDono = user?.tipo === "doador" && user?.id === oferta.doadorId;
  const ehInstituicao = user?.tipo === "instituicao";
  const [aceite, setAceite] = useState(false);
  const atraso = oferta.status === "reservada" ? diasAtrasoColeta(oferta.prazoColeta) : 0;
  const zapDoador = waLink(oferta.doadorWhatsapp, `Olá ${oferta.doadorNome}! Vi sua oferta "${oferta.titulo}". Ainda está disponível?`);

  return (
    <div className="mx-auto max-w-3xl">
      <Voltar fallback={user?.tipo === "doador" ? "/painel/doador" : "/ofertas"} rotulo={user?.tipo === "doador" ? "Voltar às minhas ofertas" : "Voltar às ofertas"} />
      <div className="mt-2 flex flex-wrap items-center gap-2">
        <span className={`rounded-full px-3 py-1 text-xs font-bold ${oferta.status === "disponivel" ? "bg-green-100 text-green-800" : oferta.status === "reservada" ? "bg-amber-100 text-amber-800" : "bg-gray-100 text-gray-600"}`}>
          {oferta.status === "disponivel" ? "↗ Disponível" : oferta.status === "reservada" ? "⏳ Reservada" : oferta.status === "entregue" ? "✅ Entregue" : "❌ Cancelada"}
        </span>
        <span className="text-sm text-gray-500">{categoriaLabel(oferta.categoria)} · {estadoOfertaLabel(oferta.estadoItem)}</span>
      </div>
      <h1 className="mt-2 text-3xl font-bold">{oferta.titulo}</h1>
      <p className="mt-1 text-sm text-gray-500">
        Por {oferta.doadorNome} · {oferta.cidade ?? "Local a combinar"} · disponível até {oferta.disponivelAte}
      </p>

      {oferta.imagens?.length > 0 && (
        <div className="mt-4 flex flex-wrap gap-2">
          {oferta.imagens.map((img) => {
            const url = fileUrl(img.url);
            return url ? <SafeImage key={img.id} src={url} alt={`Foto da oferta ${oferta.titulo}`} className="h-40 w-40 rounded-xl object-cover" /> : null;
          })}
        </div>
      )}

      <p className="mt-4 whitespace-pre-line leading-relaxed">{oferta.descricao}</p>

      <dl className="mt-4 space-y-1 rounded-xl border p-4 text-sm">
        {oferta.precisaColeta && <div><dt className="inline font-medium">Coleta: </dt><dd className="inline">precisa buscar{oferta.enderecoColeta ? ` em ${oferta.enderecoColeta}` : ""}</dd></div>}
        {oferta.status === "reservada" && <div><dt className="inline font-medium">Reservada para: </dt><dd className="inline">{oferta.instituicaoNome} · prazo de coleta até {oferta.prazoColeta}</dd></div>}
      </dl>

      {atraso > 0 && (
        <p role="alert" className="mt-4 rounded-xl border border-red-300 bg-red-50 p-3 text-sm font-semibold text-red-800">
          ⚠️ Prazo de coleta estourado há {atraso} {atraso === 1 ? "dia" : "dias"}. Regularize o quanto antes.
        </p>
      )}

      <div className="mt-4 flex flex-wrap gap-2">
        {oferta.status === "disponivel" && ehInstituicao && (
          <div className="w-full space-y-2 rounded-xl border border-amber-200 bg-amber-50 p-4">
            <label className="flex items-start gap-2 text-sm text-amber-900">
              <input type="checkbox" checked={aceite} onChange={(e) => setAceite(e.target.checked)} className="mt-1" />
              <span>Comprometo-me a <strong>coletar até {dataPrevistaColeta()}</strong> (7 dias). Entendo que o atraso libera a oferta de volta.</span>
            </label>
            <PrimaryButton onClick={() => void reivindicar()} disabled={acao || !aceite}>
              {acao ? "Aguarde..." : "Reivindicar oferta"}
            </PrimaryButton>
          </div>
        )}
        {oferta.status === "reservada" && ehInstituicao && (
          <PrimaryButton onClick={() => void confirmarRecebimento()} disabled={acao}>
            {acao ? "Aguarde..." : "Confirmar recebimento"}
          </PrimaryButton>
        )}
        {oferta.status === "disponivel" && ehDono && (
          <Link href={`/ofertas/${oferta.id}/editar`} className="rounded-lg border px-4 py-2 text-sm hover:bg-gray-50">Editar oferta</Link>
        )}
        {oferta.status === "reservada" && ehDono && (
          <SecondaryButton onClick={() => void liberar()} disabled={acao}>
            Não foi coletado — deixar disponível de novo
          </SecondaryButton>
        )}
        {!user && (
          <Link href={`/login?origem=/ofertas/${oferta.id}`} className="rounded-lg bg-[var(--primary)] px-4 py-2 text-sm font-semibold text-white">Entrar para reivindicar</Link>
        )}
        {ehInstituicao && zapDoador && oferta.status !== "entregue" && (
          <a href={zapDoador} target="_blank" rel="noopener noreferrer" className="rounded-lg border border-green-300 bg-green-50 px-4 py-2 text-sm font-semibold text-green-800">💬 Falar com doador</a>
        )}
      </div>
      {ehDono && (oferta.status === "disponivel" || oferta.status === "reservada") && (
        <div className="mt-4">
          <CancelarOferta ofertaId={oferta.id} onCancelado={() => void carregar()} />
        </div>
      )}
      <p className="mt-2 text-xs text-gray-500">Ofertas são entre doador e instituição — a plataforma só aproxima, sem taxa e sem garantia (ver Termos).</p>
      {dialogoConfirm}
    </div>
  );
}

export default function OfertaPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { user } = useAuth();
  if (!user) {
    return (
      <div className="mx-auto max-w-md text-center">
        <h1 className="text-2xl font-bold">Ofertas exclusivas para instituições</h1>
        <p className="mt-2 text-sm text-gray-600">Entre como instituição aprovada para ver e reivindicar. Doadores acompanham as suas no painel.</p>
        <p className="mt-4 flex justify-center gap-2">
          <Link href={`/login?origem=/ofertas/${id}`} className="rounded-lg bg-[var(--primary)] px-4 py-2 text-sm font-semibold text-white">Entrar</Link>
          <Link href="/painel/doador" className="rounded-lg border px-4 py-2 text-sm">Minhas ofertas</Link>
        </p>
      </div>
    );
  }
  return <OfertaDetalhe id={id} />;
}

