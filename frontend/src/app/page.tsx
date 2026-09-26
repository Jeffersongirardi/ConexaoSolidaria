import Link from "next/link";
import CampaignCard from "@/components/CampaignCard";
import SafeImage from "@/components/SafeImage";
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
      <section className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-[var(--primary)] to-[var(--primary-light)] px-6 py-12 text-white">
        <SafeImage src="/img/hero-doacao.jpg" alt="Voluntária entregando cesta básica para uma família" className="pointer-events-none absolute inset-0 h-full w-full object-cover opacity-25" loading="eager" />
        <div className="relative">
          <p className="inline-flex items-center gap-2 rounded-full bg-white/15 px-3 py-1 text-xs font-semibold">🔥 Campanhas urgentes precisando de você hoje</p>
          <h1 className="mt-3 max-w-2xl text-balance text-3xl font-bold sm:text-4xl">
            Sua doação ainda hoje alimenta uma família
          </h1>
          <p className="mt-3 max-w-xl text-white/90">
            Pix direto à instituição, taxa R$ 0, CNPJ validado. Doe cesta, roupa ou R$ 20 — e acompanhe no painel.
          </p>
          <div className="mt-6 flex flex-wrap gap-3">
            <Link href="/campanhas?urgencia=alta" className="rounded-lg bg-[var(--accent)] px-5 py-2.5 font-semibold text-[var(--primary)] hover:brightness-95">
              Doar agora
            </Link>
            <Link href="/campanhas" className="rounded-lg border border-white/60 px-5 py-2.5 font-semibold text-white hover:bg-white/10">
              Ver campanhas
            </Link>
          </div>
          <div className="mt-4 flex flex-wrap gap-2 text-xs font-semibold">
            <span className="rounded-full bg-white/15 px-3 py-1">✓ CNPJ ativo validado</span>
            <span className="rounded-full bg-white/15 px-3 py-1">⚡ Pix direto</span>
            <span className="rounded-full bg-white/15 px-3 py-1">R$ 0 taxa</span>
          </div>
          <dl className="mt-8 flex flex-wrap gap-8 text-sm">
            <div><dt className="opacity-80">Doações recebidas</dt><dd className="text-2xl font-bold">{stats.doacoesRecebidas}</dd></div>
            <div><dt className="opacity-80">Instituições</dt><dd className="text-2xl font-bold">{stats.instituicoes}</dd></div>
            <div><dt className="opacity-80">Campanhas ativas</dt><dd className="text-2xl font-bold">{stats.campanhasAtivas}</dd></div>
          </dl>
        </div>
      </section>

      <section aria-label="Quanto sua doação vale" className="mt-6 grid gap-3 rounded-2xl border bg-amber-50 p-4 text-sm sm:grid-cols-3">
        <p><strong>R$ 20</strong> = 4 marmitas quentes 🍲</p>
        <p><strong>R$ 50</strong> = 1 cesta básica 🧺</p>
        <p><strong>1 mochila</strong> = 1 volta às aulas 🎒</p>
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
            ["Escolha uma campanha", "Alimentos, roupas e higiene de instituições com CNPJ validado."],
            ["Doe direto", "Item com entrega combinada, ou valor via Pix direto — taxa R$ 0."],
            ["Acompanhe no painel", "A instituição confirma e você vê comprovante. Sem reembolso pela plataforma."],
          ].map(([titulo, texto], i) => (
            <li key={titulo} className="rounded-xl border border-[var(--border)] bg-[var(--surface)] p-4">
              <p aria-hidden="true" className="flex h-8 w-8 items-center justify-center rounded-full bg-[var(--accent-soft)] text-sm font-bold text-[var(--primary)]">{i + 1}</p>
              <h3 className="mt-2 font-semibold">{titulo}</h3>
              <p className="mt-1 text-sm text-gray-600">{texto}</p>
            </li>
          ))}
        </ol>
      </section>

      <section aria-labelledby="depoimentos" className="mt-10">
        <h2 id="depoimentos" className="text-xl font-bold">Quem já doou</h2>
        <div className="mt-4 grid gap-4 sm:grid-cols-2">
          <figure className="rounded-xl border p-4">
            <SafeImage src="/img/depoimento-doadora.jpg" alt="Doadora sorrindo" className="h-14 w-14 rounded-full object-cover" loading="lazy" />
            <blockquote className="mt-2 text-sm text-gray-700">“Doei uma cesta pelo Pix em 2 minutos e vi a confirmação no painel. Virei doadora mensal.”</blockquote>
            <figcaption className="mt-1 text-xs font-semibold">Mariana, doadora</figcaption>
          </figure>
          <figure className="rounded-xl border p-4">
            <SafeImage src="/img/depoimento-voluntario.jpg" alt="Voluntário de instituição parceira" className="h-14 w-14 rounded-full object-cover" loading="lazy" />
            <blockquote className="mt-2 text-sm text-gray-700">“As doações chegam direto na nossa conta. Confirmamos no mesmo dia e a família recebe.”</blockquote>
            <figcaption className="mt-1 text-xs font-semibold">Carlos, instituição parceira</figcaption>
          </figure>
        </div>
      </section>

      <InstallPrompt />
    </div>
  );
}
