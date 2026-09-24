import Link from "next/link";
import { fileUrl } from "@/lib/api";
import type { Campaign } from "@/lib/types";
import { UrgenciaBadge, categoriaIcone } from "./ui";

export default function CampaignCard({ campaign }: { campaign: Campaign }) {
  const img = campaign.imagens?.[0]?.url ? fileUrl(campaign.imagens[0].url) : null;
  return (
    <article className="flex flex-col overflow-hidden rounded-xl border bg-white shadow-sm transition hover:shadow-md">
      {img ? (
        <img src={img} alt="" className="h-44 w-full object-cover" loading="lazy" />
      ) : (
        <div aria-hidden="true" className="flex h-44 w-full items-center justify-center bg-blue-50 text-5xl">
          {categoriaIcone(campaign.categoria)}
        </div>
      )}
      <div className="flex flex-1 flex-col gap-2 p-4">
        <div className="flex items-center gap-2">
          <UrgenciaBadge urgencia={campaign.urgencia} />
          <span className="text-xs text-gray-500">
            {categoriaIcone(campaign.categoria)} {campaign.categoria.replace("_", " ")}
          </span>
        </div>
        <h3 className="font-semibold leading-snug">
          <Link href={`/campanhas/${campaign.id}`} className="hover:text-blue-700">
            {campaign.titulo}
          </Link>
        </h3>
        <p className="text-sm text-gray-600">
          🎯 {campaign.quantidadeAlvo}
          {campaign.instituicao && <> · {campaign.instituicao.nomeFantasia || campaign.instituicao.razaoSocial}</>}
        </p>
        <div
          className="h-2 overflow-hidden rounded-full bg-gray-200"
          role="progressbar"
          aria-valuenow={campaign.progresso ?? 0}
          aria-valuemin={0}
          aria-valuemax={100}
          aria-label={`Progresso: ${campaign.progresso ?? 0}%`}
        >
          <div className="h-full bg-green-500" style={{ width: `${Math.min(100, campaign.progresso ?? 0)}%` }} />
        </div>
        <Link
          href={`/campanhas/${campaign.id}`}
          className="mt-auto rounded-lg bg-blue-600 px-4 py-2 text-center text-sm font-semibold text-white hover:bg-blue-700"
        >
          Ver e doar
        </Link>
      </div>
    </article>
  );
}
