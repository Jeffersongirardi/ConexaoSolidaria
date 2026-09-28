import Link from "next/link";
import { fileUrl } from "@/lib/api";
import type { Campaign } from "@/lib/types";
import { UrgenciaBadge, categoriaIcone } from "./ui";

export default function CampaignCard({ campaign }: { campaign: Campaign }) {
  const img = campaign.imagens?.[0]?.url ? fileUrl(campaign.imagens[0].url) : null;
  return (
    <article className="flex flex-col overflow-hidden rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--surface)] shadow-sm transition hover:shadow-md">
      {img ? (
        <img src={img} alt="" className="h-44 w-full object-cover" loading="lazy" />
      ) : (
        <div aria-hidden="true" className="flex h-44 w-full items-center justify-center bg-[var(--accent-soft)] text-sm font-medium text-[var(--text-soft)]">
          {categoriaIcone(campaign.categoria)}
        </div>
      )}
      <div className="flex flex-1 flex-col gap-2 p-4">
        <div className="flex items-center gap-2">
          <UrgenciaBadge urgencia={campaign.urgencia} />
          <span className="text-xs text-[var(--text-soft)]">
            {categoriaIcone(campaign.categoria)}
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
        <p className="text-sm font-semibold text-[var(--primary)]">
          👥 {campaign.numDoadores ?? 0} {(campaign.numDoadores ?? 0) === 1 ? "doador" : "doadores"}
          {(campaign.valorRecebido ?? 0) > 0 && <> · 💰 R$ {Number(campaign.valorRecebido).toFixed(2)}</>}
          {(campaign.numDoacoesItens ?? 0) > 0 && <> · 🎁 {campaign.numDoacoesItens}</>}
        </p>
        <Link
          href={`/campanhas/${campaign.id}`}
          className="mt-auto rounded-lg bg-[var(--accent)] px-4 py-2 text-center text-sm font-bold text-[var(--primary)] hover:brightness-95"
        >
          Doar agora
        </Link>
      </div>
    </article>
  );
}
