# Status da migração para produção — registro vivo

> **Regra deste arquivo (para humanos e IAs):** toda mudança executada neste projeto
> DEVE acrescentar uma linha datada aqui, no fuso **America/Sao_Paulo**
> (`Get-Date -Format "yyyy-MM-dd HH:mm"`). Sem data, não conta como feito.
> Ver também: instruções obrigatórias para IAs em [`/AGENTS.md`](../AGENTS.md).

Última atualização: **2026-10-10 17:12 -03:00**

## ✅ Concluído

| Data/hora (-03:00) | O quê | Commit |
|---|---|---|
| 2026-10-10 17:12 | Staging: corrigida ordem de useState na tela de ofertas, detectada pela validação; tipos Next gerados, TypeScript passou e lint sem erros (3 avisos preexistentes). Dependências frontend instaladas sem scripts, sem alteração do lockfile. | Pendente |
| 2026-10-10 17:05 | Staging: integração R2 implementada em prod com bucket privado, redirecionamento de fotos públicas e links de 15 min para comprovantes/atualizações autorizados; caminhos permanentes no banco; 42 testes Maven verdes com Java 21 (10 novos). Deploy/upload real e persistência após redeploy pendentes. | Pendente |
| 2026-10-10 17:05 | Conforme validação informada pelo desenvolvedor: Railway Online com MySQL/Flyway e Java 21, frontend Vercel conectado, login administrador e entrega Resend/recuperação de senha funcionaram. Bucket R2 staging e credenciais criados; variáveis aguardam aplicação. | Pendente |
| 2026-10-10 14:07 | Staging: EmailService integrado à API HTTPS do Resend no perfil prod; healthcheck SMTP desativado; timeouts e logs sem conteúdo sensível. 32 testes Maven verdes (4 novos de e-mail). Deploy e entrega real pendentes. | Pendente |
| 2026-10-10 11:37 | Staging: adicionada dependência `flyway-mysql` ao backend; testes Maven concluídos com BUILD SUCCESS, conforme resultado informado pelo desenvolvedor. Validação na Railway pendente. | Pendente |
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

1. **Staging R2:** revisar e publicar a integração, aplicar as quatro variáveis pendentes na Railway e validar upload/leitura pública, proteção de comprovantes/atualizações e persistência após redeploy (Guia 2).
2. **Resend:** verificar domínio próprio antes de enviar para destinatários além do e-mail da conta; teste sandbox informado como concluído.
3. **Produção:** contas/bucket/credenciais próprios e DNS com o dono do domínio (Guias 0 e 4), deploy e cutover (Guias 5–6). Validações de staging não substituem validação do ambiente definitivo.
4. **Desligar a conta do Render**, caso ainda exista, somente após cutover validado.

## ⚠️ Pendências conscientes (não são bugs)

- R2: validar credenciais e upload real após publicar; o healthcheck não consulta o armazenamento. Arquivos antigos do disco efêmero exigem novo upload.
- `application-dev.properties` (H2 em arquivo + IP local no CORS) e `frontend/next.config.ts` (`allowedDevOrigins` com IP) são **hacks locais, fora do git** — nunca commitar.
- Resend sandbox: verificar domínio antes de enviar para outros destinatários.
- `MAIL_HOST/...` (SMTP) mantido para desenvolvimento; prod usa Resend HTTPS e não testa SMTP no healthcheck.
- Lint frontend mantém 3 avisos preexistentes de imports/componentes não usados nos painéis de doador/instituição.
- Instalação frontend (`npm ci --ignore-scripts`) reportou 9 vulnerabilidades nas dependências (8 altas, 1 crítica); auditar separadamente antes do cutover de produção, sem atualização automática de pacotes nesta integração.
- Limpeza de imagens órfãs no R2 e migração de arquivos locais existentes não estão automatizadas. Dev conserva recursos locais legados; a proteção por links assinados é aplicada em prod.
