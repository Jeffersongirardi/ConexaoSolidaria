# Plano executável — fluxos doador ↔ instituição

> **Papel deste documento:** backlog técnico do dono do projeto (lógica de funcionamento),
> separado do trabalho de auth/segurança do parceiro. Cada bloco abaixo é um PR pequeno,
> testável e independente. Regra do projeto: toda entrega entra com linha datada em
> [`00-status.md`](00-status.md) e, quando muda comportamento, atualiza os readmes.
>
> **Base:** `master` em `9f5a357` (PR #1 mergeado). **Produção:** Vercel pausada até o
> hardening de auth do parceiro; API Railway segue no ar.
>
> **Decisão de produto (2026-10-27):** pagamento **somente Pix**, direto do doador para a
> instituição. **Não existirá cartão nem transferência** — os fluxos e telas desses métodos
> serão removidos (bloco P0.5). A plataforma continua sem tocar no dinheiro (taxa R$ 0).

---

## 0. Contrato de trabalho

| Regra | Detalhe |
|---|---|
| Unidade de entrega | 1 bloco = 1 branch (`feat/...` ou `fix/...`) = 1 PR |
| Nome de branch | `fix/p0-1-moderacao-instituicao`, `feat/p0-5-pix-unico`, etc. |
| Testes | `mvn -o test` 100% verde; `tsc --noEmit` e lint no frontend |
| Testes de API | classes novas DEVEM reusar fixture única (`@BeforeAll` + `@TestInstance(PER_CLASS)`, e-mails/CNPJ aleatórios) por causa do `RateLimitFilter` (20 req/min compartilhado) |
| Migrations | nova coluna = `V9__*.sql` em `db/migration` (H2) **e** `V2__*.sql` em `db/migration-mysql`; nunca `RENAME`, nunca `CREATE INDEX IF NOT EXISTS`; rodar nos dois bancos |
| Registro | linha datada em `docs/00-status.md` no mesmo commit |
| Docs | mudança de comportamento → atualizar `readme.md`, `COMO_FUNCIONA.md`, `backend/PLAN.md` quando citarem o fluxo |
| Commit | só com pedido explícito do dono |

### Fronteira com o parceiro (evitar conflito)

| Área | Dono | Parceiro |
|---|---|---|
| Backend | `Donation/Payment/Oferta/Campaign*Controller`, `CampaignStatsService`, repositórios desses domínios | `AuthApiController`, `SecurityConfig`, `RateLimitFilter`, `EmailService`, DTOs de auth/cadastro |
| Frontend | `painel/doador`, `painel/instituicao`, `pagamento/*`, `ofertas/*`, `campanhas/*` | `login`, `cadastro/*`, `recuperar-senha`, `RequireAuth`, `lib/auth.tsx` |
| Compartilhado | `lib/types.ts`, `lib/api.ts` — combinar antes de mexer | idem |
| Dependência | **gate de e-mail verificado** para doar/reivindicar (bloco dele) | entrega o gate |

---

## 1. Decisões fechadas e defaults

| Tema | Decisão |
|---|---|
| Métodos de pagamento | **Somente Pix**; cartão e transferência removidos de UI/API |
| Dinheiro | Direto doador → instituição via BR Code; plataforma não recebe, não concilia, não estorna |
| Pagamento cancelado | BR Code é **invalidado** (copia-e-cola limpo) e a tela avisa "não pague" |
| Instituição pode rejeitar | **Sim** — doação de item e pagamento pendentes, com motivo obrigatório |
| Comprovante de transferência | Deixa de existir (sem upload de comprovante bancário) |
| Prazo de coleta | 7 dias **enforçado** (expiração automática + notificações) |
| Progresso da campanha | Deixa de exibir % fictício; passa a mostrar **itens recebidos** e **valor recebido** reais |
| Valor público | Conta **somente `recebido`** (confirmação da instituição) |
| Exclusão de campanha | **Bloqueada** com mensagem clara quando houver doações/pagamentos; UI sugere pausar |
| Open decision | Data-alvo de encerramento (define quantos P1 entram) |

---

## 2. Blocos P0 (ordem de execução)

### Status de execução

| Bloco | Estado | Evidência |
|---|---|---|
| P0.1 — moderação de instituição | ✅ Concluído (2026-10-27) | `ModeracaoInstituicaoApiTest`; suíte 43/43 |
| P0.2 — privacidade da oferta | ✅ Concluído (2026-10-27) | `OfertaDTOTest` + `OfertaPrivacidadeApiTest`; suíte 46/46 |
| P0.3 — oferta: edição/fotos | ✅ Concluído (2026-10-27) | `OfertaEdicaoModeracaoApiTest`; suíte 47/47 |
| P0.4 … P0.10 | ⏳ Pendente | — |

### P0.1 — Fechar bypass de moderação de instituição
**Branch:** `fix/p0-1-moderacao-instituicao` · **Esforço:** 1 dia

- **Problema:** instituição recusada/desativada continua editando, reativando (toggle), excluindo e enviando fotos de campanha, além de confirmar doações, pagamentos e postar atualizações. Só o `POST /campaigns` exige aprovação.
- **Mudanças:**
  - `CampaignApiController.requireOwner` → validar perfil **aprovado** também em `PUT`, `toggle`, `DELETE` e imagens.
  - `DonationApiController`: `confirmar` e `atualizacoes` exigem perfil aprovado.
  - `PaymentApiController.confirmar-recebimento` exige perfil aprovado.
  - `OfertaApiController`: `reservadas`/`recebidas`/`reivindicar` já exigem aprovação em alguns pontos — uniformizar.
- **Aceite:**
  - Instituição recusada/desativada recebe **4xx (409 — "Instituição ainda não aprovada")** em todas as ações acima.
  - Instituição aprovada continua funcionando igual.
- **Testes (novos em `FluxoGuardsApiTest`):** admin recusa instituição → `PATCH /campaigns/{id}/toggle` = 403; `PATCH /donations/{id}/confirmar` = 403; `PATCH /payments/{uuid}/confirmar-recebimento` = 403.

### P0.2 — Privacidade da oferta antes da reserva
**Branch:** `fix/p0-2-privacidade-oferta` · **Esforço:** 0,5–1 dia

- **Problema:** `OfertaDTO` devolve `doadorWhatsapp` e `enderecoColeta` em qualquer oferta aprovada; a UI mostra "Falar com doador" antes do claim, contrariando `COMO_FUNCIONA.md`.
- **Mudanças:**
  - DTO de lista (`GET /ofertas`) sem contato/endereço.
  - Detalhe expõe contato **somente** para: doador dono, admin, ou instituição que detém a reserva (`reservada`/`entregue`).
  - Frontend: botão de contato condicionado ao claim.
- **Aceite:** instituição B não vê WhatsApp/endereço da oferta reservada pela instituição A; instituição reservante vê; doador e admin veem.
- **Aceite (executado):** `OfertaDTO.from(o)` é fail-closed; lista e detalhe em `disponivel` sem contato; `reservada`/`entregue` de outra instituição = 403; reservante/dono/admin recebem contato; frontend condiciona "Falar com doador" e "Confirmar recebimento" à reserva e ao `instituicaoId`.
- **Testes:** `OfertaDTOTest` (padrão oculta, explícito expõe) + `OfertaPrivacidadeApiTest` (lista, detalhe, reserva, 403 para terceiro).

### P0.3 — Oferta: moderação após edição + fotos
**Branch:** `fix/p0-3-oferta-edicao-fotos` · **Esforço:** 1 dia

- **Problema:** editar oferta aprovada mantém `aprovado=true`; fotos antigas não aparecem/removem na edição; limite de 5 só no front.
- **Mudanças:**
  - `PUT /ofertas/{id}`: qualquer edição de conteúdo volta `aprovado=false` e limpa `motivoRecusa` (fila de moderação).
  - `POST/DELETE /ofertas/imagens`: re-aprovação necessária; limite **5 total** validado no backend.
  - `OfertaForm`: listar fotos existentes, remover e contar no limite.
- **Aceite:** oferta editada some da lista pública até aprovação; 6ª foto = 400; doador consegue trocar foto recusada pela UI.
- **Testes:** fluxo `criar → aprovar → editar → reaprovar`; upload 6 fotos = erro.

### P0.4 — Prazo de 7 dias enforçado
**Branch:** `feat/p0-4-prazo-coleta` · **Esforço:** 1–2 dias

- **Problema:** nenhum scheduler; reserva fica `reservada` para sempre; aceite é só checkbox local.
- **Mudanças:**
  - `@EnableScheduling` + job (ex.: a cada hora) que encontra ofertas `reservada` com `prazoColeta < hoje`.
  - Expiração → `disponivel`, limpa `instituicao`/`reservadaEm`/`prazoColeta`, notifica doador e instituição.
  - `reivindicar` grava aceite (coluna `aceite_coleta` boolean; migration H2/MySQL) e exige `aceite=true`.
  - `confirmar-recebimento` após expiração = 409 com mensagem clara.
- **Aceite:** reserva vencida volta à lista automaticamente; confirmação tardia bloqueada; notificações geradas.
- **Testes:** job com data forçada no passado → status/notificação; claim sem aceite = 400.

### P0.5 — Pix único (remover cartão e transferência)
**Branch:** `feat/p0-5-pix-unico` · **Esforço:** 1–2 dias

- **Problema:** três métodos, endpoints genéricos e telas coletando CVV/dados bancários sem uso. Decisão: **só Pix**.
- **Mudanças backend:**
  - `POST /payments` aceita apenas `metodo=pix` (400 para `cartao`/`transferencia`).
  - Remover `confirmar-cartao` e `confirmar-transferencia` (ou responder 410 por uma janela); manter `confirmar-pix`.
  - Remover upload de comprovante de transferência e uso de `comprovanteUrl`.
  - Ao cancelar: limpar `copiaECola` (invalida o BR Code).
- **Mudanças frontend** (`pagamento/[uuid]`):
  - Só QR + copia-e-cola + "Já paguei".
  - Remover abas de cartão e transferência (incluindo campos de CVV).
  - Pagamento cancelado exibe aviso "não pague" e status correto.
- **Docs:** `readme.md`, `COMO_FUNCIONA.md`, `backend/PLAN.md` — retirar cartão/transferência.
- **Aceite:** criar pagamento com `cartao` = 400; fluxo Pix completo continua verde; cancelado não exibe mais copia-e-cola; nenhuma tela pede dados de cartão.
- **Testes:** `PaymentFlowApiTest` ajustado + `metodo inválido = 400` + cancelamento limpa payload.
- **Compatibilidade:** pagamentos antigos de cartão/transferência já existentes continuam podendo ser marcados `recebido` pela instituição (evita dado preso); não aparecem mais como opção nova.

### P0.6 — Estados do pagamento (rejeitar/cancelar/admin)
**Branch:** `feat/p0-6-estados-pagamento` · **Esforço:** 2 dias

- **Problema:** `confirmado` é estado preso; instituição não rejeita; admin não modera; `dataConfirmacao` tem dois significados.
- **Mudanças:**
  - Novo status `rejeitado` + `motivo_rejeicao` (migration H2/MySQL).
  - `PATCH /payments/{uuid}/rejeitar` (instituição dona; `pendente`/`confirmado`; motivo obrigatório; notifica doador).
  - `PATCH /admin/payments/{uuid}/cancelar` (admin) + tela no `/painel/admin/pagamentos` (listar, filtrar, ver detalhe).
  - Coluna `data_recebimento` (separada de `data_confirmacao`, que volta a significar "declaração do doador").
  - Rejeitado/cancelado não contam em métricas nem progresso.
- **Aceite:** instituição rejeita pagamento declarado; doador é notificado; admin vê e modera; estados não ficam presos.
- **Testes:** fluxo `pendente → confirmado → rejeitado`; `pendente → recebido`; admin cancela; métricas ignoram rejeitado.

### P0.7 — Doação de item: validações e comprovante
**Branch:** `fix/p0-7-validacoes-doacao` · **Esforço:** 1 dia

- **Problema:** `quantidade` aceita `"0"`, `"-5"`, `"abc"`; coleta sem endereço; comprovante de item abre em `pendente`/`cancelado`; admin cancela `recebido` e `dataRecebimento` fica preenchida.
- **Mudanças:**
  - `DonationRequest.quantidade`: `@Pattern("^[1-9]\\d{0,4}$")` (1–99999) ou converter para inteiro (avaliar; padrão atual é string).
  - `precisaColeta=true` exige `enderecoColeta` não vazio (400).
  - Instituição pode **rejeitar** doação `pendente` (`PATCH /donations/{id}/rejeitar`, motivo, notifica doador).
  - Admin cancelar `recebido` → limpar `dataRecebimento` + registrar motivo (se coluna existir).
  - Frontend `/comprovante/doacao/[id]`: só renderizar com `status=recebido`, senão aviso.
- **Aceite:** payloads inválidos = 400; comprovante pendente não abre; rejeição notifica doador.
- **Testes:** quantidade inválida; coleta sem endereço; rejeitar; comprovante pendente bloqueado (front — teste manual roteirizado).

### P0.8 — Exclusão de campanha coerente
**Branch:** `fix/p0-8-exclusao-campanha` · **Esforço:** 0,5 dia

- **Problema:** `DELETE` quebra por FK quando há doações/pagamentos; UI promete "doações ficam sem campanha".
- **Mudanças:**
  - Backend: se houver doações/pagamentos, retornar **409** com "Não é possível excluir campanha com doações. Pause-a em vez de excluir."
  - Frontend: esconder/renomear botão para "Pausar" quando `numDoacoes/valorRecebido > 0`; corrigir texto.
- **Aceite:** campanha vazia exclui; com histórico = 409 com mensagem clara; UI não promete o impossível.
- **Testes:** `DELETE` com e sem vínculos.

### P0.9 — Métricas e progresso reais
**Branch:** `feat/p0-9-metricas` · **Esforço:** 2 dias (depende de validação da decisão)

- **Problema:** valor público soma `confirmado` (autodeclaração); doadores contam pendentes; itens contam linhas; `/stats` mistura unidades; progresso é fictício (+10/+5).
- **Mudanças:**
  - `PaymentRepository.sum...`: somente `recebido`.
  - `numDoadores`: distintos com doação/pagamento **não cancelado e não pendente** (ou rotular claramente "participantes").
  - `itensPorCategoria`: contar doações `recebido` (manter contagem de linhas, mas renomear rótulo para "doações recebidas").
  - `/stats`: `doacoesRecebidas = itens recebidos + pagamentos recebidos + ofertas entregues` com rótulos separados na UI (evitar número único enganoso).
  - Remover `progresso` da exibição (painel/instituição e DTO público); manter coluna no banco por compatibilidade.
  - Corrigir métricas zeradas na página pública da instituição (usar `CampaignStatsService`).
- **Aceite:** "Já paguei" não sobe valor público; somente confirmação da instituição conta; UI sem % fictício.
- **Testes:** `StatsApiTest` novo + casos em `PaymentFlowApiTest`.

### P0.10 — Correções rápidas de UX/consistência
**Branch:** `fix/p0-10-ux-fluxos` · **Esforço:** 1–2 dias (pode virar 2 PRs)

- [ ] `/pagamento/[uuid]/sucesso` só celebra `recebido` (ou `confirmado` com texto correto); `cancelado` = estado de erro.
- [ ] Painel instituição: comprovante financeiro acessível (guard aceita instituição/admin) **ou** remover o link.
- [ ] Erro de confirmação de doação não é mais apagado no `finally`.
- [ ] Confirmações antes de ações irreversíveis (confirmar recebimento, aprovar/recusar, desativar usuário).
- [ ] Página de campanha só mostra formulários para `DOADOR` (instituição/admin não).
- [ ] Remover bloco "Por categoria" duplicado no painel do doador.
- [ ] Admin: blog lista rascunhos (endpoint admin) e novo rascunho não redireciona para 404.
- [ ] CTA `?urgencia=alta` da home passa a filtrar em `/campanhas`.
- [ ] Painel instituição exibe estado **"Recusada"** com `motivoRecusa` (hoje diz "em análise").
- [ ] Datas formatadas em pt-BR (hoje `yyyy-MM-dd` cru).

---

## 3. Blocos P1 (depois dos P0)

| # | Bloco | Esforço | Observação |
|---|---|---|---|
| P1.1 | Notificações completas + truncamento | 1 dia | pagamento criado/cancelado/rejeitado, doação cancelada, aprovação/recusa de instituição, alerta ao admin; cortar `mensagem` em 300 |
| P1.2 | Concorrência nas transições | 2 dias | `@Version` ou lock pessimista em confirmar/rejeitar/cancelar/claim; evita regressão de status e e-mail duplicado |
| P1.3 | Motivos persistidos + auditoria admin | 1 dia | motivo de cancelamento de doação/oferta; quem/quando nas ações admin |
| P1.4 | Fotos de atualização e campanha | 1 dia | renderizar `DonationUpdate.fotoUrl`; remover arquivos órfãos no R2; reordenar capa |
| P1.5 | CNPJ válido/ativo | 1–2 dias | checksum + consulta ReceitaWS + normalização única (coordenar com parceiro) |
| P1.6 | Paginação e busca | 2 dias | instituições, ofertas, usuários, mensagens, doações admin, notificações; debounce |
| P1.7 | Admin: moderação de pagamentos e campanhas | 2 dias | depende de P0.6; pausar campanha individual, contadores de pendências |

## 4. P2 (somente se houver prazo)

Agenda de coleta/entrega, comprovante em PDF, exportação de relatórios, `txid` único no Pix para conciliação, dados bancários da instituição, e-mail nos eventos de oferta.

---

## 5. Roteiro de validação por bloco (local, Vercel off)

1. Backend: `cmd /c "mvn -o test > log-mvn.txt 2>&1"` → 100% verde (base atual: 42 testes).
2. Frontend: `npx tsc --noEmit` e lint.
3. Fumaça manual no perfil `dev` (H2):
   - doador cria item e Pix; instituição confirma/rejeita; comprovante privado;
   - oferta: criar → admin aprova → outra instituição não vê contato → reserva → expira (forçar data) → libera;
   - instituição recusada não opera;
   - excluir campanha vazia × com histórico.
4. Quando a Vercel religar: repetir roteiro no domínio + prints de evidência.

## 6. Definition of Done (por bloco)

- [ ] Testes de API cobrindo o comportamento novo (e regressão do antigo)
- [ ] `mvn -o test`, `tsc`, lint verdes
- [ ] Migrations rodando em H2 **e** MySQL (quando houver schema)
- [ ] Docs de comportamento atualizados (readme/COMO_FUNCIONA/PLAN)
- [ ] Linha datada em `docs/00-status.md`
- [ ] PR pequeno, descrição com "o que mudou / como testar"
- [ ] Nenhum segredo ou hack local commitado
