# Runbook de segurança — o que o André executa

> **Para o parceiro (infra):** este documento lista, bloco por bloco, o que precisa ser
> feito **fora do código** (variáveis, deploy, validação). O código/testes entram pelo
> `master`; você só aplica, valida e devolve evidência.
>
> Regra: **features novas entram desligadas por flag** quando dependem de infra.
> Nada quebra produção por variável ausente.

---

## 0. Antes de cada bloco (sempre)

1. Sincronizar o repositório oficial (`Jeffersongirardi/ConexaoSolidaria`), **não** o fork antigo.
2. Deploy **backend primeiro** (Railway) — é ele que aplica as migrations Flyway.
3. Deploy **frontend** (Vercel) depois — variáveis `NEXT_PUBLIC_*` exigem rebuild.
4. Registrar: data, commit, print do deploy e resultado do smoke.
5. Se algo falhar: rolagem para o deploy anterior e aviso no PR/WhatsApp.

---

## 1. Variáveis de ambiente

### Já existentes (não mudam)
| Variável | Onde | Observação |
|---|---|---|
| `SPRING_PROFILES_ACTIVE=prod` | Railway | — |
| `JWT_SECRET` | Railway | **mínimo 32 bytes** |
| `FRONTEND_URL` | Railway | `https://conexaosolidarias.com.br` |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Railway | senha **≥12**, diferente do e-mail |
| `RESEND_API_KEY`, `MAIL_FROM` | Railway | Resend verificado |
| `R2_*` | Railway | bucket privado |
| `NEXT_PUBLIC_API_URL`, `NEXT_PUBLIC_API_ORIGIN` | Vercel | API pública |

### Novas (por bloco)
| Variável | Bloco | Padrão | Obrigatória? |
|---|---|---|---|
| `TURNSTILE_SECRET_KEY` | S3 | vazio | só se `TURNSTILE_ENABLED=true` |
| `NEXT_PUBLIC_TURNSTILE_SITE_KEY` | S3 | vazio | idem |
| `TURNSTILE_ENABLED` | S3 | `false` | ligar após criar as chaves |
| `SECURITY_EMAIL_VERIFICATION` | S2 | `false` | ligar quando o e-mail de verificação estiver validado |
| `SECURITY_LOCKOUT` | S3 | `true` | — |

> Enquanto a flag estiver `false`, o recurso simplesmente não é exigido; o código novo
> continua deployado e testado.

---

## 2. S0 — Higiene (2026-10-27)

**Código entregue:** health sem detalhes; boot prod falha com `ADMIN_PASSWORD` fraca e
`JWT_SECRET` < 32 bytes; headers de segurança no frontend.

**Você faz:**
- [ ] Conferir `ADMIN_PASSWORD` nas Variables do Railway: ≥12 caracteres, diferente do e-mail.
- [ ] Conferir `JWT_SECRET`: ≥32 caracteres (se trocar, todos os logins caem — avisar).
- [ ] Deploy backend → ver logs: deve subir normal.
- [ ] `https://api.conexaosolidarias.com.br/actuator/health` → só `{"status":"UP"}` (sem DB/disco).
- [ ] Deploy frontend → conferir no DevTools: `X-Frame-Options`, `Permissions-Policy`, `Strict-Transport-Security`.

**Falha esperada (e correta):** boot em prod sem `ADMIN_PASSWORD` forte → aplicação não sobe (fail-closed).

---

## 3. S1 — Política de senha (2026-10-27)

**Código entregue:** mínimo 10 caracteres, denylist de senhas comuns, bloqueio de senha
igual a e-mail/nome; aplicado em cadastro, troca e reset. Frontend com hint/mín. 10.

**Você faz:**
- [ ] Deploy backend + frontend.
- [ ] Smoke: cadastro com `curta123` → erro 400; com `1234567890` → erro 400; com `SenhaForte2026` → ok.
- [ ] Troca de senha no perfil com senha fraca → erro 400.
- [ ] Nada de env nova.

> Usuários antigos continuam logando normalmente; a política vale a partir de novo cadastro/reset/troca.

---

## 4. S2 — Verificação de e-mail (pendente)

Quando implementado, você fará:
- [ ] Confirmar `MAIL_FROM`/Resend (já ok).
- [ ] Cadastrar conta real, receber o link, verificar.
- [ ] Testar: doar antes de verificar (bloqueado com mensagem), depois (liberado).
- [ ] Ligar `SECURITY_EMAIL_VERIFICATION=true` após validar no staging.
- [ ] Evidência: print do e-mail + antes/depois da ação.

---

## 5. S3 — Lockout, CAPTCHA e auditoria (pendente)

- [ ] Criar site no Cloudflare Turnstile (modo gerenciado).
- [ ] Railway: `TURNSTILE_SECRET_KEY`, `TURNSTILE_ENABLED=true`.
- [ ] Vercel: `NEXT_PUBLIC_TURNSTILE_SITE_KEY` + **redeploy**.
- [ ] Smoke: 5 tentativas erradas → bloqueio temporário; widget aparece no login/cadastro.
- [ ] Evidência: print do widget e do 429.

---

## 6. S4 — Sessões (pendente)

- [ ] Deploy backend + frontend.
- [ ] Smoke: logar em duas abas; trocar senha em uma; a outra deve cair na próxima ação.
- [ ] Smoke: “Sair” invalida refresh (não renova mais).

---

## 7. S5 — Reset de senha (implementado 2026-10-30)

**Código entregue:** token guardado como hash (nunca em texto plano), validade de 30 min,
uso único, limite de 3 pedidos por hora e invalidação das sessões antigas (`token_version`).
Migrations: H2 `V12` / MySQL `V5`.

- [ ] Deploy backend (Flyway aplica `V12`/`V5`).
- [ ] Solicitar reset com e-mail real; link chega com domínio próprio.
- [ ] Usar link duas vezes → segunda falha.
- [ ] Após redefinir, a sessão antiga cai (na próxima ação) e o login antigo não entra mais.
- [ ] Pedir reset 4× no mesmo minuto → só 3 e-mails são emitidos.
- [ ] Evidência: prints do e-mail e das respostas.

---

## 8. S6/S7 — E-mails e frontend (pendente)

- [ ] Validar entrega dos templates (verificação, reset, boas-vindas).
- [ ] Conferir que links usam `https://conexaosolidarias.com.br`.
- [ ] Deploy Vercel e smoke visual (banner de verificação, mensagens de erro).

---

## 9. Ordem de deploy (resumo)

```
git pull (repo oficial) → Railway (backend) → aguardar health → Vercel (frontend) → smoke
```

**Rollback:** Vercel → Promover deploy anterior; Railway → Rollback. As migrations são
aditivas (não exigem rollback de schema).

---

## 10. Evidência para o dono

A cada bloco, devolver:
1. Commit/deploy (hash).
2. Print do health e do smoke.
3. Qualquer erro de infra com log (sem tokens/senhas).
4. Confirmação das variáveis aplicadas (nomes, nunca valores).
