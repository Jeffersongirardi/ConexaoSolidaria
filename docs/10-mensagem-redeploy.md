# Mensagem para o André — sincronizar e redeployar

> **Uso:** copie o bloco abaixo e envie ao parceiro (WhatsApp/Discord/PR) quando o `master`
> oficial estiver à frente do fork e for preciso atualizar o deploy.
>
> **Contexto (2026-10-30):** oficial no último commit do `master`; fork em `79d9ab4` (bem atrás).
> Se a Vercel/Railway estiverem ligadas ao fork, o redeploy automático **não** pega as
> mudanças até o sync. O passo a passo operacional completo está em
> [`09-runbook-seguranca-andre.md`](09-runbook-seguranca-andre.md).

---

Oi, André! Atualizei bastante o `master` oficial (`Jeffersongirardi/ConexaoSolidaria`) — está no **último commit do `master`**, bem à frente do seu fork (seu fork ainda está em `79d9ab4`). Por isso o redeploy automático, do jeito que está, **não pega as mudanças**.

**Antes de redeployar, escolha um caminho:**

**A) Se a Vercel/Railway estão ligadas ao seu fork** (mais provável):
1. No GitHub do seu fork → **Sync fork → Update branch**.
2. Confirme que o fork está no último commit do `master`.
3. Railway: **Redeploy** do serviço `api` (ele aplica as migrations MySQL **V5–V8**).
4. Vercel: **Redeploy** (ou aguarde o deploy automático disparar pelo push do sync).

**B) Se estão ligadas ao repo oficial:** só **Redeploy** nos dois — o `master` já tem tudo.

**Ordem importa:** backend primeiro, depois frontend.

**Depois do deploy, confira:**
1. `https://api.conexaosolidarias.com.br/actuator/health` → `{"status":"UP"}` **sem detalhes** de banco/disco.
2. Rodapé do site → deve mostrar **`v0.1.0`** (é a prova visual de que o front novo entrou).
3. Cadastro real → chega o e-mail “Confirme seu e-mail” e o link funciona.
4. “Esqueci senha” → e-mail chega, link válido por **30 min** e só funciona uma vez.
5. 5 logins errados seguidos → **429** (bloqueio temporário).
6. Logar em duas abas e clicar “Sair” em uma → a outra cai.
7. Se houver doação/Pix de teste: e-mail de rejeição/cancelamento chega.

**Só depois de validar o e-mail real**, ligue `SECURITY_REQUIRE_EMAIL_VERIFICATION=true` nas Variables da Railway. Antes disso, deixe `false` (padrão).

**Também confira:** `ADMIN_PASSWORD` com ≥12 caracteres (diferente do e-mail) e `JWT_SECRET` com ≥32. Se trocar o `JWT_SECRET`, todos precisam logar de novo.

**Me devolve, por favor:** hash do deploy (o mesmo do `git log -1` do master), print do health, print do rodapé `v0.1.0` e o resultado dos testes 3–6. Qualquer erro, manda o log (sem tokens/senha).

O passo a passo completo está em **`docs/09-runbook-seguranca-andre.md`** e o estado geral em **`docs/08-handoff-andre.md`**.

**Sugestão para o futuro:** aponta a Vercel/Railway para o **repo oficial** (você já é colaborador Write) — assim o redeploy automático funciona direto, sem depender de sync do fork.

---
