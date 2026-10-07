# Conexões Solidárias — Frontend

Next.js 16.3.5 (App Router, Turbopack) + React 19 + TypeScript + Tailwind 4. PWA instalável (só em contexto seguro + build produção — `SwRegister` não registra em dev). Consome `NEXT_PUBLIC_API_URL` (API) e `NEXT_PUBLIC_API_ORIGIN` (apenas imagens legadas `/uploads`; URLs R2 absolutas passam direto). Deploy: **Vercel** (Root Directory `frontend`) — ver [docs/05-vercel-frontend.md](../docs/05-vercel-frontend.md).

```bash
npm install
npm run dev      # http://localhost:3000
npm run build    # 43 rotas
```

`.env.local` (não versionado): `NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1`, `NEXT_PUBLIC_API_ORIGIN=http://localhost:8080`.

## Rotas (43)

- **Públicas:** `/`, `/campanhas`, `/campanhas/[id]`, `/instituicoes`, `/instituicoes/[id]`, `/blog`, `/blog/[slug]`, `/sobre`, `/faq`, `/contato`, `/termos`, `/privacidade`, `/offline`
- **Auth:** `/login`, `/cadastro`, `/cadastro/doador`, `/cadastro/instituicao`, `/recuperar-senha`, `/redefinir-senha`
- **Doador:** `/painel/doador`, `/pagamento/[uuid]`, `/pagamento/[uuid]/sucesso`, `/pagamento/[uuid]/comprovante`, `/comprovante/doacao/[id]`
- **Ofertas (doador publica; lista/detalhe só instituição):** `/ofertas`, `/ofertas/nova`, `/ofertas/[id]`, `/ofertas/[id]/editar`
- **Instituição:** `/painel/instituicao`, `/painel/instituicao/perfil`, `/painel/instituicao/campanhas/nova`, `/painel/instituicao/campanhas/[id]/editar`
- **Admin:** `/painel/admin`, `/painel/admin/usuarios`, `/painel/admin/instituicoes`, `/painel/admin/mensagens`, `/painel/admin/blog`, `/painel/admin/blog/novo`, `/painel/admin/blog/[id]/editar`, `/painel/admin/ofertas`, `/painel/admin/doacoes`
- **Comuns:** `/perfil`, `/notificacoes` (+ `robots.txt`, `sitemap.xml`, `_not-found`, `loading.tsx`, `error.tsx`)

`RequireAuth` protege por `tipos={["doador"|"instituicao"|"admin"]}` (ex.: comprovante financeiro só doador; `/ofertas` e detalhe só instituição aprovada — doador vê as suas no painel).

## Componentes e libs-chave

| Arquivo | Papel |
|---------|-------|
| `components/CampaignForm.tsx` | Criar/editar em passo único: dados + fotos com preview, publica → sobe fotos com progresso → navega ao painel; na edição, upload imediato ao selecionar (máx. 5) |
| `components/CampaignCard.tsx` | Card público: foto, urgência, “👥 N doadores · 💰 R$ · 🎁 N” (só métricas >0) |
| `components/SafeImage.tsx` | `<img>` client que some se o arquivo não existir (uso em Server Components — `onError` inline quebra o build) |
| `components/ui.tsx` | Botões, Field, Alert, EmptyState, Spinner, Pagination, `UrgenciaBadge` |
| `components/Skeletons.tsx`, `RequireAuth.tsx`, `InstallPrompt.tsx` | Loading, gate de papel, instalação PWA |
| `components/Voltar.tsx` | Botão “← Voltar” (`router.back()` + fallback; usado em ~20 telas de detalhe/edição/comprovante) |
| `components/OfertaForm.tsx`, `CancelarOferta.tsx`, `useConfirm.tsx`, `ConfirmDialog.tsx` | Form de ofertas, cancelamento com motivo, modal de confirmação acessível |
| `components/dashboard.tsx` | `PainelHeader`, `StatCard`, `StatGrid`, `SecaoTitulo` (linguagem visual dos 3 painéis) |
| `components/Header.tsx`, `MobileBottomNav.tsx`, `Footer.tsx` | Nav em 2 níveis por papel (principais md+, completos lg+), bottom nav até md, footer sempre visível |
| `lib/api.ts` | `api()` (Bearer automático, FormData sem Content-Type manual) + `fileUrl()` (URLs `http(s)/data:` passam direto — R2; resto prefixa `API_ORIGIN`) + tipos de erro |
| `lib/auth.tsx` | Sessão (`useAuth`, login/logout/refresh) |
| `lib/categorias.ts` | **Fonte única** das 11 categorias (valor técnico + rótulo plural + emoji) |
| `lib/whatsapp.ts` | `waLink(phone, msg)` — normaliza DDD+número para `55...` |
| `lib/ofertas.ts` | `MOTIVOS_CANCELAMENTO`, `diasAtrasoColeta()`, `dataPrevistaColeta()` |
| `lib/types.ts` | Tipos espelhando os DTOs (`Campaign.numDoadores/valorRecebido(confirmado+recebido)/numDoacoesItens/itensPorCategoria`, `Donation` com contatos/coleta, `Oferta` completa) |
| `app/layout.tsx`, `app/manifest.ts`, `SwRegister.tsx` | `suppressHydrationWarning` no body, manifest PWA (`theme_color #1a4d3e`), SW só em produção |

## Fluxos de tela

- **Doar item:** `campanhas/[id]` (item, quantidade, observação, coleta + endereço) → cartão verde pós-intenção (WhatsApp pronto + endereço/instruções) → painel doador (“Como entregar”) → instituição confirma.
- **Doar valor:** `campanhas/[id]` → `POST /payments` → `/pagamento/[uuid]` (QR **BR Code real** + copia-e-cola da chave da instituição, cartão mock, transferência) → `/sucesso` → comprovante (liberado de confirmado em diante). Dinheiro vai direto à instituição — sem gateway.
- **Ofertar item:** `/ofertas/nova` (fotos com preview, data de compromisso) → aguarda aprovação admin → instituição reivindica com aceite → coleta em 7 dias (badge de atraso) → confirmação (com aviso se atrasada).
- **Painel doador:** header “Meu impacto”, stats, campanhas recentes, “continue onde parou”, itens (cancelar/comprovante só recebido/updates), financeiros (concluir/comprovante), Minhas ofertas (foto, pills, datas, liberar/cancelar com motivo). Estado inicial `[]` (nunca `null` — evita `filter` em null).
- **Painel instituição:** header com razão social, stats, campanhas, intenções (WhatsApp do doador, coleta, confirmar, atualizações), financeiros, ofertas reservadas (desistir) + recebidas.
- **Painel admin:** stats com alerta em pendências, aprovar/recusar instituições e ofertas, moderar doações, blog, mensagens, usuários.

## Imagens

`public/img/` (hero, urgentes, categorias, depoimentos, pix, entrega). `public/icons/` (PWA). Uploads: disco local em dev; **Cloudflare R2 em prod** (URLs absolutas — ver [docs/02-cloudflare-r2.md](../docs/02-cloudflare-r2.md)).

## Deploy (Vercel)

Root Directory **`frontend`**, envs `NEXT_PUBLIC_API_URL` + `NEXT_PUBLIC_API_ORIGIN` (valores em [docs/05-vercel-frontend.md](../docs/05-vercel-frontend.md)). Previews automáticas por PR; rollback em 1 clique. **Não commitar** `allowedDevOrigins` com IP local nem `.env.local` com IP de teste.

## Convenções

- Server Components por padrão; `"use client"` só onde há estado/evento. Nunca `onError` inline em Server Component (usar `SafeImage`).
- Toasts via `sonner` (`toast.success/error`).
- Paleta verde-âmbar via CSS vars (`--primary #1a4d3e`, `--accent #e8a838`).
- Após mudar DTO no backend, atualizar `lib/types.ts` no mesmo commit.
