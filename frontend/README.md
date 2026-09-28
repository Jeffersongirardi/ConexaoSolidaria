# Conexões Solidárias — Frontend

Next.js (App Router) + React + TypeScript + Tailwind 4. PWA instalável. Consome `NEXT_PUBLIC_API_URL` (API) e `NEXT_PUBLIC_API_ORIGIN` (arquivos `/uploads`).

```bash
npm install
npm run dev      # http://localhost:3000
npm run build    # 39 rotas
```

`.env.local` (não versionado): `NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1`, `NEXT_PUBLIC_API_ORIGIN=http://localhost:8080`.

## Rotas (39)

- **Públicas:** `/`, `/campanhas`, `/campanhas/[id]`, `/instituicoes`, `/instituicoes/[id]`, `/blog`, `/blog/[slug]`, `/sobre`, `/faq`, `/contato`, `/termos`, `/privacidade`, `/offline`
- **Auth:** `/login`, `/cadastro`, `/cadastro/doador`, `/cadastro/instituicao`, `/recuperar-senha`, `/redefinir-senha`
- **Doador:** `/painel/doador`, `/pagamento/[uuid]`, `/pagamento/[uuid]/sucesso`, `/pagamento/[uuid]/comprovante`, `/comprovante/doacao/[id]`
- **Instituição:** `/painel/instituicao`, `/painel/instituicao/perfil`, `/painel/instituicao/campanhas/nova`, `/painel/instituicao/campanhas/[id]/editar`
- **Admin:** `/painel/admin`, `/usuarios`, `/instituicoes`, `/mensagens`, `/blog`, `/blog/novo`, `/blog/[id]/editar`
- **Comuns:** `/perfil`, `/notificacoes` (+ `robots.txt`, `sitemap.xml`, `_not-found`)

`RequireAuth` protege por `tipos={["doador"|"instituicao"|"admin"]}` (ex.: comprovante financeiro só doador — instituição cai no gate).

## Componentes e libs-chave

| Arquivo | Papel |
|---------|-------|
| `components/CampaignForm.tsx` | Criar/editar em passo único: dados + fotos com preview, publica → sobe fotos com progresso → navega ao painel; na edição, upload imediato ao selecionar (máx. 5) |
| `components/CampaignCard.tsx` | Card público: foto, urgência, “👥 N doadores · 💰 R$ · 🎁 N” (só métricas >0) |
| `components/SafeImage.tsx` | `<img>` client que some se o arquivo não existir (uso em Server Components — `onError` inline quebra o build) |
| `components/ui.tsx` | Botões, Field, Alert, EmptyState, Spinner, Pagination, `UrgenciaBadge` |
| `components/Skeletons.tsx`, `RequireAuth.tsx`, `InstallPrompt.tsx` | Loading, gate de papel, instalação PWA |
| `lib/api.ts` | `api()` (Bearer automático, FormData sem Content-Type manual) + `fileUrl()` (`API_ORIGIN + path`) + tipos de erro |
| `lib/auth.tsx` | Sessão (`useAuth`, login/logout/refresh) |
| `lib/categorias.ts` | **Fonte única** das 11 categorias (valor técnico + rótulo plural + emoji) |
| `lib/whatsapp.ts` | `waLink(phone, msg)` — normaliza DDD+número para `55...` |
| `lib/types.ts` | Tipos espelhando os DTOs (`Campaign.numDoadores/valorRecebido/numDoacoesItens/itensPorCategoria`, `Donation` com contatos/coleta) |

## Fluxos de tela

- **Doar item:** `campanhas/[id]` (item, quantidade, observação, coleta + endereço) → cartão verde pós-intenção (WhatsApp pronto + endereço/instruções) → painel doador (“Como entregar”) → instituição confirma.
- **Doar valor:** `campanhas/[id]` → `POST /payments` → `/pagamento/[uuid]` (Pix QR/chave, cartão mock, transferência) → `/sucesso` → comprovante.
- **Painel doador:** stats, campanhas recentes, “continue onde parou”, itens (cancelar/comprovante/updates), financeiros (concluir/comprovante). Estado inicial `[]` (nunca `null` — evita `filter` em null).
- **Painel instituição:** stats, campanhas (ver/editar/pausar/remover), intenções (WhatsApp do doador, coleta, confirmar, atualizações com foto), financeiros recebidos.

## Imagens

`public/img/` (hero, urgentes, categorias, depoimentos, pix, entrega). `public/icons/` (PWA). Uploads de campanha vêm do backend (`/uploads/**`).

## Convenções

- Server Components por padrão; `"use client"` só onde há estado/evento. Nunca `onError` inline em Server Component (usar `SafeImage`).
- Toasts via `sonner` (`toast.success/error`).
- Paleta verde-âmbar via CSS vars (`--primary #1a4d3e`, `--accent #e8a838`).
- Após mudar DTO no backend, atualizar `lib/types.ts` no mesmo commit.
