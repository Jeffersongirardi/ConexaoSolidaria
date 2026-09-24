import Link from "next/link";
import CampaignCard from "@/components/CampaignCard";
import InstallPrompt from "@/components/InstallPrompt";
import { api } from "@/lib/api";
import type { Campaign, Stats } from "@/lib/types";

export const dynamic = "force-dynamic";

async function getData(): Promise<{ destaques: Campaign[]; stats: Stats }> {
  try {
    const [destaques, stats] = await Promise.all([
      api<Campaign[]>("/campaigns/destaques", { token: null }),
      api<Stats>("/stats", { token: null }),
    ]);
    return { destaques, stats };
  } catch {
    return {
      destaques: [],
      stats: { doacoesRecebidas: 0, instituicoes: 0, campanhasAtivas: 0 },
    };
  }
}

export default async function Home() {
  const { destaques, stats } = await getData();

  return (
    <div>
      <section className="rounded-2xl bg-gradient-to-br from-[var(--primary)] to-[var(--primary-light)] px-6 py-12 text-white">
        <h1 className="max-w-2xl text-balance text-3xl font-bold sm:text-4xl">
          Conectando doadores a quem mais precisa
        </h1>
        <p className="mt-3 max-w-xl text-white/90">
          Doe alimentos e roupas para instituições validadas. Acompanhe sua
          doação até a entrega.
        </p>
        <div className="mt-6 flex flex-wrap gap-3">
          <Link href="/campanhas" className="rounded-lg bg-[var(--accent)] px-5 py-2.5 font-semibold text-[var(--primary)] hover:brightness-95">
            Ver campanhas
          </Link>
          <Link href="/cadastro" className="rounded-lg border border-white/60 px-5 py-2.5 font-semibold text-white hover:bg-white/10">
            Quero doar
          </Link>
        </div>
        <dl className="mt-8 flex flex-wrap gap-8 text-sm">
          <div><dt className="opacity-80">Doações recebidas</dt><dd className="text-2xl font-bold">{stats.doacoesRecebidas}</dd></div>
          <div><dt className="opacity-80">Instituições</dt><dd className="text-2xl font-bold">{stats.instituicoes}</dd></div>
          <div><dt className="opacity-80">Campanhas ativas</dt><dd className="text-2xl font-bold">{stats.campanhasAtivas}</dd></div>
        </dl>
      </section>

      <section aria-labelledby="destaques" className="mt-10">
        <div className="flex items-baseline justify-between">
          <h2 id="destaques" className="text-xl font-bold">Campanhas em destaque</h2>
          <Link href="/campanhas" className="text-sm text-[var(--primary)] hover:underline">Ver todas →</Link>
        </div>
        {destaques.length === 0 ? (
          <p className="mt-4 text-sm text-gray-600">Nenhuma campanha ativa no momento. Volte em breve!</p>
        ) : (
          <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {destaques.map((c) => <CampaignCard key={c.id} campaign={c} />)}
          </div>
        )}
      </section>

      <section aria-labelledby="como-funciona" className="mt-10">
        <h2 id="como-funciona" className="text-xl font-bold">Como funciona</h2>
        <ol className="mt-4 grid gap-4 sm:grid-cols-3">
          {[
            ["Escolha uma campanha", "Alimentos, roupas e itens de higiene de instituições validadas."],
            ["Registre sua doação", "Informe o item e a quantidade, ou contribua com valores via Pix, cartão ou transferência."],
            ["Acompanhe até a entrega", "A instituição confirma o recebimento e você acompanha tudo no painel."],
          ].map(([titulo, texto], i) => (
            <li key={titulo} className="rounded-xl border border-[var(--border)] bg-[var(--surface)] p-4">
              <p aria-hidden="true" className="flex h-8 w-8 items-center justify-center rounded-full bg-[var(--accent-soft)] text-sm font-bold text-[var(--primary)]">{i + 1}</p>
              <h3 className="mt-2 font-semibold">{titulo}</h3>
              <p className="mt-1 text-sm text-gray-600">{texto}</p>
            </li>
          ))}
        </ol>
      </section>

      <InstallPrompt />
    </div>
  );
}
