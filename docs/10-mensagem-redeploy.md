# Redeploy — validação de 01/11 e mensagem histórica


## Resultado registrado em 01/11/2026

Este resumo substitui as pendências de deploy da mensagem histórica abaixo.
As evidências são relatos e capturas enviados pelo André; não houve acesso direto
às contas dos provedores nem leitura de valores secretos nesta atualização.

| Etapa | Evidência / resultado |
|---|---|
| Git | Fork `andrebritovita/ConexaoSolidaria` publicado até `d359565`, incluindo merges do upstream até `01500a1`. |
| Validação local anterior | 86 testes Maven verdes; TypeScript e lint limpos. Não repetidos para esta alteração só de documentação. |
| Railway | Plano atualizado pelo André; novo backend Active. Captura do deployment `d78c4ac8`, de 01/11 12:18. Esse identificador é do deployment, não prova do hash Git. |
| MySQL | Às 12:19: “Successfully validated 8 migrations” e “Schema railway is up to date. No migration necessary.” Sem migrations pendentes para esse deployment. |
| Vercel | André confirmou o commit publicado; site em `https://conexaosolidarias.com.br` acessível. |
| R2 | Novo upload funcionou e objeto apareceu no bucket; credenciais em execução funcionais para o teste realizado. |
| Resend | Domínio Verified; mensagem de confirmação registrada como Delivered, enviada por `noreply@conexaosolidarias.com.br`. |
| Cadastro e confirmação | Novo cadastro recebeu e-mail; link confirmou a conta; saída e novo login funcionaram. |
| Exigência de confirmação | `SECURITY_REQUIRE_EMAIL_VERIFICATION=true` aplicada com deploy; tentativa de oferta sem confirmação bloqueada; conta confirmada conseguiu seguir o fluxo. |
| Recuperação de senha | André confirmou recebimento do e-mail, redefinição e login com nova senha. |

### Diagnóstico esclarecido

O e-mail de `notifications@link.com` era relacionado ao pagamento da Railway,
não à confirmação do site. Antes do novo deploy, o backend ainda executava a versão
antiga de R2. A migration V6 marca usuários preexistentes como verificados; por isso
uma conta anterior à atualização pode não exibir “Reenviar e-mail”. O teste com uma
nova conta após o deploy validou o fluxo atual.

### Manutenção e testes complementares

- Logs mostram **Flyway 9.22.3 / MySQL 9.7** com aviso de compatibilidade não testada.
  O deploy iniciou e validou as migrations; planejar atualização compatível com a
  stack e testes H2/MySQL. Nenhuma dependência foi alterada nesta sessão.
- André confirmou manualmente: health `{"status":"UP"}`, bloqueio visual após cinco
  falhas de login, logout em outro navegador após atualizar a página e rejeição de
  reutilização do link de reset. Expiração por tempo e status HTTP 429 não foram
  capturados neste teste manual.
- Pix de teste sem movimentação real: início e cancelamento geraram e-mails com
  status Delivered; painel refletiu cancelamento. Comprovante abriu para a instituição;
  outro doador recebeu “Comprovante não encontrado” e visitante foi direcionado ao login.

### Cobertura automatizada complementar

- `PaymentFlowApiTest`: verifica destinatário e assunto de Pix iniciado, declarado,
  recebido, cancelado e rejeitado; repetição de recebimento não gera outro evento.
  Também exercita autenticação real via MockMvc: dono, instituição responsável e admin
  acessam detalhes e comprovante; outro doador e outra instituição recebem 403;
  visitante recebe resposta 4xx.
- `EmailServiceTest`: usa transação H2 real e servidor HTTP simulado para comprovar
  envio somente após commit e nenhum envio em rollback.
- Validação local: **91 testes Maven aprovados**, TypeScript e lint sem erros.
  Os testes não enviam e-mails reais, não movimentam dinheiro e não acessam o R2.
  A matriz HTTP testa autorização da API; acesso à URL assinada continua coberto
  separadamente pelo teste unitário existente `PrivateUploadAccessTest`.

- A preferência do André é seguir no próprio repositório; alinhar a colaboração
  com Jefferson. A sugestão de usar o repositório original abaixo é histórica.
- Segredos permanecem nos provedores. Este registro contém somente nomes de
  variáveis, endereços públicos e resultados de testes.

## Mensagem histórica de 30/10/2026

A referência abaixo ao fork desatualizado descreve o estado de 30/10, já resolvido.
Não é necessário repetir o sync/deploy por causa desse texto.

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
