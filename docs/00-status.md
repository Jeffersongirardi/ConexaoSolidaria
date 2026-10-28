# Status da migração para produção — registro vivo

> **Regra deste arquivo (para humanos e IAs):** toda mudança executada neste projeto
> DEVE acrescentar uma linha datada aqui, no fuso **America/Sao_Paulo**
> (`Get-Date -Format "yyyy-MM-dd HH:mm"`). Sem data, não conta como feito.
> Ver também: instruções obrigatórias para IAs em [`/AGENTS.md`](../AGENTS.md).

Última atualização: **2026-10-27 21:33 -03:00** (America/Sao_Paulo)

## ✅ Concluído

| Data/hora (-03:00) | O quê | Commit |
|---|---|---|
| 2026-10-10 11:37 | Staging (parceiro): dependência `flyway-mysql` no backend; build Maven OK | `761d54a` |
| 2026-10-10 14:07 | Staging (parceiro): `EmailService` via Resend no perfil prod, healthcheck SMTP desativado, logs sem conteúdo sensível; 32 testes | `97e769d` |
| 2026-10-10 17:05 | Staging (parceiro): Railway online com MySQL/Flyway, Vercel conectado, login admin e e-mail de reset funcionando | `97e769d` |
| 2026-10-10 17:05 | Staging (parceiro): R2 privado + redirecionamento de fotos públicas + links assinados de 15 min; 42 testes (Java 21) | `79d9ab4` |
| 2026-10-10 17:12 | Staging (parceiro): correção da ordem de hooks em `/ofertas/[id]`; TS/lint ok; 9 vulnerabilidades de dependências reportadas | `79d9ab4` |
| 2026-10-07 15:40 | PIX direto real: `PixBrCodeService` (BR Code EMV + CRC16 + validação CPF/CNPJ/e-mail/telefone/aleatória), `Payment.copiaECola` (migration H2 `V8`), QR real no lugar do `pix://` falso, validação de `pixKey` no cadastro/edição, copia-e-cola + copiar no frontend, preview no perfil | `976340e` |
| 2026-10-07 15:50 | Base prod (código): `RailwayDataSourceConfig` (MySQL), baseline `db/migration-mysql/V1__baseline.sql`, `forward-headers-strategy=native`, `app.mail.from`, `mysql-connector-j` | `976340e` |
| 2026-10-07 15:55 | Descarte do Render: `render.yaml`, driver Postgres e `RenderDataSourceConfig` removidos | `976340e` |
| 2026-10-07 16:00 | Validação: **28/28 testes verdes** (Auth 3, CampaignDonation 3, FluxoGuards 4, PaymentFlow 7, RailwayConfig 3, PixBrCode 8), `tsc` limpo, eslint limpo | `976340e` |
| 2026-10-07 16:30 | Decisão de orçamento aprovada: Railway Hobby **$5/mês** (Free de $1/mês não comporta API+MySQL 24/7; trial expira e apaga volumes). Guias 01/02/03/05/06 atualizados com custos e guardrails | (este commit) |
| 2026-10-07 16:32 | Auditoria dos readmes: contagem 14→**28 testes**, R2/Resend marcados como Fase 3 (não implementados), Render removido das docs, `migration-mysql` + `RailwayDataSourceConfig` na árvore | (este commit) |
| 2026-10-07 16:43 | Modelo de contas decidido: produção 100% do dono (Railway ~$21–23/mês), parceiro com contas próprias + colaborador Write no GitHub. Novo `docs/00-acessos.md`; guias 01/05 ajustados | (este commit) |
| 2026-10-22 19:26 | Staging do parceiro **validado pelo dono** (admin fornecido pelo parceiro): R2 upload, e-mails Resend, privacidade de comprovante, login admin ok. **Decisão: produção fica na conta do parceiro** (dono mantém GitHub + DNS). `docs/00-acessos.md` atualizado; PR #1 destravado | (este commit) |
| 2026-10-22 19:40 | **Horizonte esclarecido**: projeto extensionista, provavelmente será tirado do ar em breve — rigor de infra longeva rebaixado a opcional; cutover de domínio passou a ser condicional; fluxo simplificado nos próximos passos | (este commit) |
| 2026-10-27 20:53 | **PR #1 mergeado** (`9f5a357`) e **domínio próprio validado** (apex 200, API UP com MySQL, CORS correto, R2 assinado + comprovantes bloqueados, Resend verificado). Vercel **pausada pelo parceiro** até o hardening de auth. Criado `docs/07-fluxos-backlog.md` (plano executável dos fluxos doador/instituição) com decisão **PIX único** (sem cartão/transferência) | (este commit) |
| 2026-10-27 21:08 | **P0.1 executado**: instituição recusada/desativada não edita, reativa nem exclui campanha, não confirma doação/pagamento, não publica atualização e não lê dados de doadores/ofertas. Novo `ModeracaoInstituicaoApiTest`; **43/43 testes verdes** | `89a023e` |
| 2026-10-27 21:23 | **P0.2 executado**: `OfertaDTO` fail-closed (WhatsApp/endereço do doador só após reserva, para reservante/dono/admin); lista e detalhe de oferta disponível sem contato; reserva alheia = 403; frontend mostra "Falar com doador"/"Confirmar" só para a instituição reservante. Limite do `RateLimitFilter` virou configurável (`app.rate-limit.auth-max`, 10000 nos testes). **46/46 testes verdes**, `tsc` limpo | `d86d8d8` |
| 2026-10-27 21:28 | **P0.3 executado**: qualquer edição de conteúdo, adição ou remoção de foto devolve a oferta para moderação; limite de 5 fotos validado no backend; remoção apaga o arquivo; formulário lista/remove fotos existentes e avisa da nova análise. **47/47 testes verdes**, `tsc` limpo, lint só com 3 avisos preexistentes | `a69b509` |
| 2026-10-27 21:33 | **P0.4 executado**: aceite de coleta obrigatório e persistido (`aceite_coleta`, migrations H2 V9 + MySQL V2); job horário libera reservas vencidas e notifica doador/instituição; confirmação após o prazo = 409. `OfertaPrazoColetaApiTest`; **48/48 testes verdes**, `tsc` limpo | (este commit) |
| 2026-10-06 19:30 | Fluxo de pagamento corrigido (confirmação em 1 clique, `PATCH /cancelar`), `PaymentFlowApiTest`, docs iniciais | `4331e3d` |
| 2026-10-06 18:00 | 6 guias de produção em `docs/01–06` + atualização de `readme`, `backend/readme`, `frontend/README`, `PLAN`, `COMO_FUNCIONA` | `2ead1f1` |

## 🕐 Próximos passos (ordem sugerida; [P] = parceiro, [D] = dono)

> **Contexto:** produção pausada na Vercel até o parceiro concluir o hardening de auth.
> **Divisão de trabalho:** parceiro = auth/cadastro/e-mail/segurança; dono = lógica de
> funcionamento dos fluxos (plano detalhado em [`07-fluxos-backlog.md`](07-fluxos-backlog.md)).

1. **[P] Hardening de auth** (ver `docs/07`, seção "Fronteira com o parceiro"): senha ≥ 8, verificação de e-mail, bloqueio por conta, Turnstile, `/actuator/health` sem detalhes públicos.
2. **[D] Executar os blocos P0 do `07-fluxos-backlog.md`** na ordem definida, 1 branch + 1 PR por bloco, com testes de API verdes.
3. **[D+P] Alinhar o gate de confiança**: só usuário com e-mail verificado pode doar/reivindicar — pré-requisito para religar com pessoas reais.
4. **[D] Religar a Vercel**, repetir a fumaça completa (domínio, e-mail, Pix, comprovante privado) e registrar data/evidências aqui.
5. **Takedown** quando o prazo chegar: pausar Railway/Vercel, encerrar R2/Resend e registrar a data.

## ⚠️ Pendências conscientes (não são bugs)

- `application-dev.properties` (H2 em arquivo + IP local no CORS) e `frontend/next.config.ts` (`allowedDevOrigins` com IP) são **hacks locais, fora do git** — nunca commitar.
- **9 vulnerabilidades** reportadas no `npm` (8 altas, 1 crítica) — auditar antes de religar o site ao público (sem upgrade automático).
- Lint do frontend mantém **3 avisos** preexistentes (imports/componentes não usados nos painéis).
- Produção nas contas do parceiro (decisão 2026-10-22): com horizonte curto, a transição de contas deixa de ser risco relevante.
- Limpeza de imagens órfãs no R2 e migração de arquivos locais antigos não são automáticas (banco de produção novo torna o impacto baixo).
- **PIX único**: cartão e transferência serão removidos de UI/API (bloco P0.5); dados de staging desses métodos serão descartados, não migrados.
