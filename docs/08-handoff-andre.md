# Handoff — André (auth/segurança) ↔ fluxos da plataforma

> **Para quem é este documento:** parceiro responsável por autenticação, cadastro,
> e-mails e endurecimento de segurança. Ele explica o que mudou no `master` entre
> 2026-10-27 (P0.1 → P1.1), o que já está decidido, o que depende de você e como
> retomar sem conflito.
>
> Leia também: [`00-status.md`](00-status.md) (registro datado) e
> [`07-fluxos-backlog.md`](07-fluxos-backlog.md) (backlog completo).

---

## 1. Onde estamos

| Item | Estado |
|---|---|
| `master` oficial | **`818a5a5`** — inclui P0.1–P1.1, segurança S0–S5, e-mails E1–E4 e versão no rodapé |
| Testes backend | **86/86 verdes** (`mvn -o test`, Java 17) |
| Frontend | `tsc --noEmit` limpo; lint sem erros (3 avisos preexistentes); rodapé mostra `v0.1.0` |
| Produção | **Vercel pausada por você**; API Railway segue `UP` |
| Domínio/Resend | `conexaosolidarias.com.br` validado, `api.` na Railway, Resend `Verified` |
| Migrations novas | H2 `V9–V15` e MySQL `V2–V8` (aplicam no próximo deploy) |

O trabalho foi feito em **cadeia de branches** e consolidado em `master` com fast-forward;
as branches antigas foram apagadas. A partir daqui, trabalhe em branches do **repo oficial**
(e não no fork), para não divergir de novo.

---

## 2. O que mudou nos fluxos (decisões fechadas)

### Pagamento: **somente Pix** (sem cartão/transferência)
- `POST /payments` só aceita `metodo=pix`; endpoints de cartão/transferência foram removidos.
- Cancelamento (doador ou admin) **limpa o copia-e-cola** — BR Code inválido.
- Instituição pode **rejeitar** Pix (`PATCH /payments/{uuid}/rejeitar`) com motivo obrigatório.
- Admin modera em `GET /admin/payments` + `PATCH /admin/payments/{uuid}/cancelar`.
- `data_confirmacao` = declaração do doador ("Já paguei"); `data_recebimento` = confirmação da instituição.
- Status: `pendente → confirmado → recebido` (ou `rejeitado` / `cancelado`).

### Doação de item
- Quantidade validada (número positivo + unidade opcional: `10`, `5 kg`).
- Coleta exige endereço.
- Instituição pode **recusar** (`PATCH /donations/{id}/rejeitar`, motivo) → status `rejeitado`.
- Comprovante de item só é renderizado com status `recebido`.
- Admin cancela doação recebida e limpa `dataRecebimento` + devolve progresso.

### Ofertas (marketplace reverso)
- WhatsApp/endereço do doador **só após a reserva** (reservante/dono/admin); terceiro recebe 403.
- Qualquer edição ou mudança de foto devolve a oferta para moderação; limite de **5 fotos**.
- Aceite de coleta é persistido; `PATCH /ofertas/{id}/reivindicar` exige `{"aceiteColeta":true}`.
- Job horário (`OfertaExpiracaoService`) libera reservas vencidas (7 dias) e notifica as partes.
- Confirmação após o prazo = 409.

### Moderação e campanhas
- Instituição recusada/desativada não edita, reativa, exclui campanha, confirma doações/pagamentos
  nem lê dados de doadores/ofertas.
- `DELETE /campaigns/{id}` retorna **409** se houver doações/pagamentos (UI orienta pausar).

### Métricas
- Valor público = somente pagamentos `recebido`.
- Doadores distintos ignoram pendente/cancelado/rejeitado.
- `%` fictício deixou de ser exibido; `/stats` agora separa `itensRecebidos`, `pagamentosRecebidos`,
  `ofertasEntregues` e mantém `doacoesRecebidas` como soma.

### Notificações
- Admin avisado de: nova instituição, nova oferta, nova mensagem de contato.
- Instituição avisada de: aprovação/recusa, novo Pix, cancelamentos de doação/Pix.
- Mensagens truncadas nos limites das colunas (tipo 30 / mensagem 300 / link 200).

---

## 3. Migrations — atenção antes de subir

O código novo **exige** as migrations abaixo; se o deploy rodar antes delas, o Hibernate/`ddl-auto=validate` falha:

| Banco | Arquivos |
|---|---|
| H2 (dev/testes) | `V9` oferta aceite, `V10` pagamento rejeição/recebimento, `V11` doação motivo, `V12` token_version/reset, `V13` verificação de e-mail, `V14` auth_events, `V15` email_lembretes |
| MySQL (Railway) | `V2` oferta aceite, `V3` pagamento rejeição/recebimento, `V4` doação motivo, `V5` token_version/reset, `V6` verificação de e-mail, `V7` auth_events, `V8` email_lembretes |

Regras que continuam valendo: sem `RENAME COLUMN`, sem `CREATE INDEX IF NOT EXISTS`,
e a pasta MySQL é separada (`db/migration-mysql`).

### O que a migration `V13`/`V6` faz com contas antigas

Marca **todas as contas existentes como verificadas** (`UPDATE users SET email_verificado = TRUE`).
Isso evita bloquear quem já usava o sistema quando a flag de verificação for ligada.
Só cadastros novos nascem como não verificados.

### E-mails e lembretes (E1–E4)

- Envio **após o commit** (não sai e-mail de operação desfeita).
- E-mails existentes: verificação de e-mail, reset (30 min), doação registrada/confirmada,
  doação rejeitada/cancelada, instituição aprovada/recusada, Pix criado/declarado/recebido/
  rejeitado/cancelado.
- Lembretes diários (idempotentes, tabela `email_lembretes`):
  - verificação pendente há 24h (**só** com `SECURITY_REQUIRE_EMAIL_VERIFICATION=true`);
  - reserva de oferta vencendo em até 48h (instituição).
- Nenhuma variável nova é necessária; o job usa `FRONTEND_URL` já existente.

---

## 4. O que VOCÊ (André) faz agora

**Já implementado pelo dono** (não precisa refazer): senha mín. 10 + denylist, verificação de
e-mail com gate por flag, lockout 5/15min, auditoria `auth_events`, reset com token hasheado
(30 min/uso único/3 por hora), `token_version` (reset/troca/logout derrubam sessões),
access 15 min/refresh 1 dia, health sem detalhes, headers de segurança, e-mails E1–E4 e
versão no rodapé.

**Sua parte (infra/validação):**

1. **Deploy backend primeiro** (Railway aplica Flyway `V5–V8` MySQL), depois **frontend**.
2. Validar no staging/produção:
   - health `{"status":"UP"}` **sem** detalhes;
   - cadastro → e-mail de verificação → link funcional;
   - 5 logins errados → **429**;
   - logout em uma aba derruba a outra;
   - reset de senha real (link 30 min, uso único);
   - e-mails de aprovação/recusa, Pix e doação (quando houver evento).
3. Só depois de validar o e-mail real: `SECURITY_REQUIRE_EMAIL_VERIFICATION=true`.
4. Conferir `ADMIN_PASSWORD` ≥12 (diferente do e-mail) e `JWT_SECRET` ≥32.
5. **CAPTCHA (Turnstile)** ficou de fora desta rodada: quando quiserem, criar as chaves,
   aplicar `TURNSTILE_SECRET_KEY`/`NEXT_PUBLIC_TURNSTILE_SITE_KEY` e implementar o widget
   (o restante da segurança não depende dele).
6. Revisar os diffs de S2/S4 e, se der, evoluir: e-mail de atualização de doação,
   e-mail de cancelamento de Pix pelo admin, versão texto puro/Reply-To e CSP no frontend.

Ao retomar: **sincronize o repo oficial** (o fork está atrás), use branch + PR, rode
`mvn -o test` (meta 86/86) e `tsc`. O passo a passo operacional está em
[`09-runbook-seguranca-andre.md`](09-runbook-seguranca-andre.md).

---

## 5. Divisão para evitar conflito

| Área | Dono | Você (André) |
|---|---|---|
| Fluxos/domínio | `Donation/Payment/Oferta/Campaign*Controller`, `CampaignStatsService`, repositórios desses domínios, `OfertaExpiracaoService` | — |
| Auth/segurança | — | `AuthApiController`, `SecurityConfig`, `RateLimitFilter`, `EmailService`, DTOs de auth/cadastro |
| Frontend | `painel/doador`, `painel/instituicao`, `pagamento/*`, `ofertas/*`, `campanhas/*` | `login`, `cadastro/*`, `recuperar-senha`, `RequireAuth`, `lib/auth.tsx` |
| Compartilhado | `lib/types.ts`, `lib/api.ts`, `NotificationService` — avisar antes de mexer | idem |

Observação: `NotificationService` ganhou `notificarAdmins(...)` — o cadastro de instituição
e o contato já disparam avisos ao admin. Se mexer no fluxo de cadastro, mantenha esse aviso.

---

## 6. Como validar rápido

```powershell
# backend
cd backend
cmd /c "mvn -o test > log-mvn.txt 2>&1"
Select-String -Path log-mvn.txt -Pattern "Tests run:|BUILD"

# frontend
cd ..\frontend
npx tsc --noEmit
npm run lint
```

- Testes de API compartilham o mesmo contexto/IP; o surefire eleva o rate limit com
  `app.rate-limit.auth-max=10000`. **Não** baixe esse valor nos testes sem revisar os fixtures.
- H2 de testes é arquivo (`./data`), então fixtures novas devem usar e-mails/CNPJ aleatórios.

### Fumaça mínima após religar a Vercel
1. Cadastro de doador + e-mail real.
2. Instituição criada → admin aprova → instituição publica campanha com foto.
3. Doação de item: doador cria → instituição confirma → comprovante abre (e terceiro não acessa).
4. Pix: cria → QR/copia-e-cola → "Já paguei" → instituição confirma → valor entra em “recebido”.
5. Instituição rejeita um Pix e um item → doador vê motivo; BR Code invalidado.
6. Oferta: criar → admin aprova → outra instituição **não** vê WhatsApp → reserva → prazo expira (forçar data) → volta à lista.
7. Excluir campanha com histórico → 409; sem histórico → remove.

---

## 7. Pendências conhecidas (P1.2 → P1.7)

- **P1.2** Concorrência/lock nas transições (confirmar/rejeitar/cancelar/claim).
- **P1.3** Motivos persistidos de cancelamento + auditoria de ações admin.
- **P1.4** Exibir foto de atualização, limpeza de órfãos no R2, reordenar capa.
- **P1.5** CNPJ com dígito verificador + consulta de situação ativa (coordenar com auth).
- **P1.6** Paginação/busca nas listas grandes (instituições, ofertas, admin, notificações).
- **P1.7** Moderação admin de campanhas e contadores de pendências.

Detalhes, critérios de aceite e estimativas: [`07-fluxos-backlog.md`](07-fluxos-backlog.md).

---

## 8. Segurança e segredos

- Nada de segredo no git: `JWT_SECRET`, `RESEND_API_KEY`, `R2_*`, `ADMIN_PASSWORD` ficam nas Variables.
- `application-dev.properties` e `next.config.ts` com IP local continuam **hacks locais fora do git**.
- O domínio e o DNS são do dono; mudanças de cutover passam por ele.
