import Link from "next/link";
import { fileUrl } from "@/lib/api";
import type { Campaign } from "@/lib/types";
import { UrgenciaBadge } from "./ui";
import { categoriaLabel } from "@/lib/categorias";
import CategoryIcon from "./CategoryIcon";
import { Package, Users, Wallet } from "lucide-react";

export default function CampaignCard({ campaign }: { campaign: Campaign }) {
  const img = campaign.imagens?.[0]?.url ? fileUrl(campaign.imagens[0].url) : null;
  return (
    <article className="flex flex-col overflow-hidden rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--surface)] shadow-sm transition hover:shadow-md">
      {img ? (
        <img src={img} alt={`Foto da campanha ${campaign.titulo}`} className="h-44 w-full object-cover" loading="lazy" />
      ) : (
        <div aria-hidden="true" className="flex h-44 w-full items-center justify-center bg-[var(--accent-soft)] text-[var(--text-soft)]">
          <CategoryIcon categoria={campaign.categoria} className="h-12 w-12" />
        </div>
      )}
      <div className="flex flex-1 flex-col gap-2 p-4">
        <div className="flex items-center gap-2">
          <UrgenciaBadge urgencia={campaign.urgencia} />
          <span className="flex items-center gap-1 text-xs text-[var(--text-soft)]">
            <CategoryIcon categoria={campaign.categoria} className="h-3.5 w-3.5" />
            {categoriaLabel(campaign.categoria)}
          </span>
        </div>
        <h3 className="line-clamp-2 font-semibold leading-snug">
          <Link href={`/campanhas/${campaign.id}`} className="hover:text-[var(--primary)]">
            {campaign.titulo}
          </Link>
        </h3>
        <p className="text-sm text-[var(--text-soft)]">
          {campaign.quantidadeAlvo}
          {campaign.instituicao && <> · {campaign.instituicao.nomeFantasia || campaign.instituicao.razaoSocial}</>}
        </p>
        <p className="flex flex-wrap items-center gap-2 text-sm font-semibold text-[var(--primary)]">
          <span className="flex items-center gap-1"><Users className="h-4 w-4" aria-hidden="true" />{campaign.numDoadores ?? 0}</span>
          {(campaign.valorRecebido ?? 0) > 0 && <span className="flex items-center gap-1"><Wallet className="h-4 w-4" aria-hidden="true" />R$ {Number(campaign.valorRecebido).toFixed(2)}</span>}
          {(campaign.numDoacoesItens ?? 0) > 0 && <span className="flex items-center gap-1"><Package className="h-4 w-4" aria-hidden="true" />{campaign.numDoacoesItens}</span>}
        </p>
        <Link
          href={`/campanhas/${campaign.id}`}
          className="mt-auto inline-flex min-h-11 items-center justify-center rounded-lg bg-[var(--accent)] px-4 py-2 text-center text-sm font-bold text-[var(--primary)] hover:brightness-95"
        >
          Doar agora
        </Link>
      </div>
    </article>
  );
}
