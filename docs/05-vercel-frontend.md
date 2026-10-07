# Guia 5 — Frontend na Vercel

> Para quem nunca usou a Vercel. Tempo estimado: ~30 min.
> Nosso frontend: Next.js `16.3.5` + React 19, build `next build` (43 rotas), sem config especial.

## 1. Importar o projeto (atenção ao monorepo!)

1. Conta na [vercel.com](https://vercel.com) (login com GitHub) → **Add New → Project** → **Import** `Jeffersongirardi/ConexaoSolidaria`.
2. ⚠️ **Passo que todo iniciante erra**: expanda **Build and Output Settings → Root Directory** e selecione **`frontend`**. Sem isso, a Vercel tenta buildar a raiz (que é Java) e falha.
3. Framework Preset: **Next.js** (detectado sozinho). Não mude Build Command (`next build`) nem Output Directory.

## 2. Variáveis de ambiente (Production)

Em **Settings → Environment Variables**, adicione (marque **Production**; repita em **Preview** se quiser testar PRs contra a API de produção):

| Variável | Valor |
|---|---|
| `NEXT_PUBLIC_API_URL` | `https://api.conexaosolidarias.com.br/api/v1` |
| `NEXT_PUBLIC_API_ORIGIN` | `https://api.conexaosolidarias.com.br` |

> Por que duas? `API_URL` é a base das chamadas (`/api/v1/...`); `API_ORIGIN` é a base das imagens legadas `/uploads/...` (ver `frontend/src/lib/api.ts`). URLs novas do R2 são absolutas e ignoram as duas.

## 3. Deploy e domínio

1. **Deploy**. Cada push em `master` faz redeploy; **cada PR ganha uma Preview URL automática** — use-a para revisar sem tocar na produção.
2. **Rollback em 1 clique**: **Deployments → ⋯ no deploy anterior → Promote to Production**.
3. Domínio: **Settings → Domains** → adicione `conexaosolidarias.com.br` (+ `www`) e siga o [Guia 4](04-dominio-dns.md).

## 4. Limpeza local (importante)

Seu `frontend/.env.local` é **seu, não versionado**, e hoje aponta para IP de teste (`192.168.x`). Para desenvolver contra a API nova:

```
NEXT_PUBLIC_API_URL=https://api.conexaosolidarias.com.br/api/v1
NEXT_PUBLIC_API_ORIGIN=https://api.conexaosolidarias.com.br
```

E remova o `allowedDevOrigins` com IP local do `next.config.ts` antes de qualquer commit (é hack temporário de teste no celular).

## 5. Se o build falhar

| Sintoma | Causa provável |
|---|---|
| `Module not found` / erro de TS | Rode `npx tsc --noEmit` local — a Vercel trata TS como erro de build, o `next dev` nem sempre |
| ESLint falha o build | Rode `npx next lint` local antes do push |
| Preview ok, produção com API errada | Variável marcada só em Preview — confira o escopo (Production × Preview) |
