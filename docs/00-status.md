# Status da migração para produção — registro vivo

> **Regra deste arquivo (para humanos e IAs):** toda mudança executada neste projeto
> DEVE acrescentar uma linha datada aqui, no fuso **America/Sao_Paulo**
> (`Get-Date -Format "yyyy-MM-dd HH:mm"`). Sem data, não conta como feito.
> Ver também: instruções obrigatórias para IAs em [`/AGENTS.md`](../AGENTS.md).

Última atualização: **2026-10-07 16:20 -03:00**

## ✅ Concluído

| Data/hora (-03:00) | O quê | Commit |
|---|---|---|
| 2026-10-07 15:40 | PIX direto real: `PixBrCodeService` (BR Code EMV + CRC16 + validação CPF/CNPJ/e-mail/telefone/aleatória), `Payment.copiaECola` (migration H2 `V8`), QR real no lugar do `pix://` falso, validação de `pixKey` no cadastro/edição, copia-e-cola + copiar no frontend, preview no perfil | `976340e` |
| 2026-10-07 15:50 | Base prod (código): `RailwayDataSourceConfig` (MySQL), baseline `db/migration-mysql/V1__baseline.sql`, `forward-headers-strategy=native`, `app.mail.from`, `mysql-connector-j` | `976340e` |
| 2026-10-07 15:55 | Descarte do Render: `render.yaml`, driver Postgres e `RenderDataSourceConfig` removidos | `976340e` |
| 2026-10-07 16:00 | Validação: **28/28 testes verdes** (Auth 3, CampaignDonation 3, FluxoGuards 4, PaymentFlow 7, RailwayConfig 3, PixBrCode 8), `tsc` limpo, eslint limpo | `976340e` |
| 2026-10-07 16:30 | Decisão de orçamento aprovada: Railway Hobby **$5/mês** (Free de $1/mês não comporta API+MySQL 24/7; trial expira e apaga volumes). Guias 01/02/03/05/06 atualizados com custos e guardrails | (este commit) |
| 2026-10-07 16:32 | Auditoria dos readmes: contagem 14→**28 testes**, R2/Resend marcados como Fase 3 (não implementados), Render removido das docs, `migration-mysql` + `RailwayDataSourceConfig` na árvore | (este commit) |
| 2026-10-07 16:43 | Modelo de contas decidido: produção 100% do dono (Railway ~$21–23/mês), parceiro com contas próprias + colaborador Write no GitHub. Novo `docs/00-acessos.md`; guias 01/05 ajustados | (este commit) |
| 2026-10-06 19:30 | Fluxo de pagamento corrigido (confirmação em 1 clique, `PATCH /cancelar`), `PaymentFlowApiTest`, docs iniciais | `4331e3d` |
| 2026-10-06 18:00 | 6 guias de produção em `docs/01–06` + atualização de `readme`, `backend/readme`, `frontend/README`, `PLAN`, `COMO_FUNCIONA` | `2ead1f1` |

## 🕐 Próximos passos (ordem sugerida, dono = parceiro salvo indicação)

1. **Contas e DNS** (Guia 1–4): Railway + MySQL, Cloudflare R2, Resend, `api.conexaosolidarias.com.br` — sem código, só cliques.
2. **Executar o baseline MySQL contra a Railway** e marcar como verificado aqui (hoje está só revisado, nunca executado).
3. **Pacote 2 (código)**: `R2StorageService` + `ResendEmailService` via HTTP (interfaces já existem).
4. **Deploy Vercel** (Guia 5) + cutover (Guia 6, itens 1–11).
5. **Desligar a conta do Render** (só após o item 4 verde).

## ⚠️ Pendências conscientes (não são bugs)

- `application-dev.properties` (H2 em arquivo + IP local no CORS) e `frontend/next.config.ts` (`allowedDevOrigins` com IP) são **hacks locais, fora do git** — nunca commitar.
- `MAIL_HOST/...` (SMTP) mantido como fallback até o Resend entrar.
- `backend/readme.md` ainda cita `UPLOAD_DIR`/disco como legado até o R2 entrar.
