# Guia 1 — Backend + MySQL na Railway

> Para quem nunca usou a Railway. Tempo estimado: ~1 hora.
> Pré-requisitos: conta no GitHub com acesso de **colaborador (Write)** ao repositório
> `ConexaoSolidaria` (ver [Guia 0](00-acessos.md)) — sem isso o repo nem aparece para importar.

## 1. O que vamos montar

- **Serviço web**: nossa API Spring Boot (pasta `backend/`) rodando 24h na Railway.
- **Database MySQL**: banco relacional gerenciado pela Railway, na mesma conta/projeto.
- Cada `git push` na branch conectada faz **redeploy automático**.

## 2. Criar a conta e o projeto

1. Acesse [railway.app](https://railway.app) e clique em **Login with GitHub**. Autorize o acesso.
2. **Cadastre um cartão de crédito no dia 1** (Billing). Sem ele, a conta fica como trial limitado (rede restrita) e **os volumes/banco de trial são apagados** após expirar o crédito. Com cartão, nada é cobrado além do plano contratado.

## 2.1. Quanto custa (sem surpresa) — decisão aprovada: Hobby $5/mês

- **Trial**: $5 de crédito por 30 dias. Depois vira plano **Free de $1/mês** — insuficiente para nós (API + MySQL ligados 24/7 estouram $1; a Railway **não** pausa serviços ociosos como o Render free fazia).
- **Hobby ($5/mês)**: inclui $5 de uso; nosso porte (API 512MB + MySQL pequeno) cabe dentro. É o piso real do projeto.
- Onde acompanhar: projeto → aba **Usage/Metrics**. Regra de economia: manter só os 2 serviços (API + MySQL); não criar serviços/bancos extras à toa.
2. Clique em **New Project → Deploy from GitHub repo** → escolha `Jeffersongirardi/ConexaoSolidaria`.
   - **Se você já tem um workspace** (é o caso da produção): crie um **projeto novo** dentro dele em vez de conta nova — mesma fatura, deploys e banco isolados, consumo visível por projeto no Usage.
3. A Railway vai detectar o monorepo. Configure o serviço para usar **apenas a pasta `backend`**:
   - Clique no serviço criado → **Settings → Source → Root Directory**: `backend`.
   - O build usa **Nixpacks** (detecta Java/Maven sozinho, sem Dockerfile): comando padrão `mvn clean package -DskipTests` e start `java -jar target/*.jar`. Nada a configurar aqui.
4. Renomeie o serviço para `api` (clique no nome, edite). Organização ajuda depois.

## 3. Criar o banco MySQL

1. Dentro do projeto: **+ New → Database → MySQL** (não "PostgreSQL" — migramos para MySQL).
2. Pronto. A Railway **injeta sozinha** estas variáveis no serviço `api`:
   `MYSQLHOST`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE`, `MYSQLPORT`, `MYSQL_URL`, `DATABASE_URL`.
   > ⚠️ Nosso código da Fase 1 lê a conexão dessas variáveis (formato `mysql://usuario:senha@host:porta/banco`). Você não precisa digitar nada aqui.

## 4. Variáveis de ambiente do backend

No serviço `api` → aba **Variables → + New Variable**, adicione uma por uma:

| Variável | Valor | De onde vem |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | Fixo — ativa `application-prod.properties` |
| `JWT_SECRET` | (gerar, ver abaixo) | Segredo do login; **nunca** commitar |
| `FRONTEND_URL` | `https://conexaosolidarias.com.br` | Domínio final (CORS + links de e-mail). Sem `/` no fim |
| `ADMIN_EMAIL` | seu e-mail de admin | Lido pelo `DataSeeder` no primeiro boot |
| `ADMIN_PASSWORD` | senha forte inicial | Idem — **troque após o primeiro login** |
| `JAVA_OPTS` | `-Xmx512m -Xms256m` | Limite de memória (plano básico) |
| `RESEND_API_KEY` | `re_...` | Guia 3 (pode deixar para depois; sem ela, e-mails só vão para o log) |
| `R2_*` | (5 variáveis) | Guia 2 (Fase 3; sem elas, uploads usam disco efêmero — fotos somem no restart) |

**Gerar o `JWT_SECRET`** (PowerShell, rode no seu PC):

```powershell
[Convert]::ToBase64String((1..48 | ForEach-Object { Get-Random -Max 256 }))
```

Copie o resultado para a variável. Guarde uma cópia num gerenciador de senhas — se perder, dá para gerar outro, mas **todos os logins ativos caem** (esperado e seguro).

## 5. Domínio temporário + healthcheck

1. Serviço `api` → **Settings → Networking → Generate Domain**. Anote a URL (`https://api-production-xxxx.up.railway.app`). É temporária até o [Guia 4](04-dominio-dns.md).
2. **Settings → Deploy → Healthcheck Path**: `/actuator/health`. A Railway só considera o deploy "saudável" se esse endpoint responder `{"status":"UP"}`.

## 6. Deploy e verificação

1. Conecte a branch: **Settings → Source → Branch**: `master` (ou a branch de produção que combinarmos).
2. Faça um push (ou clique **Redeploy**) e acompanhe em **Deployments → View Logs** até aparecer:
   `Started ConexoesSolidariasApplication ...`
3. Teste no navegador:
   - `https://<seu-dominio>/actuator/health` → `{"status":"UP"}`
   - `https://<seu-dominio>/swagger-ui/index.html` → documenta a API
4. Primeiro login: use `ADMIN_EMAIL`/`ADMIN_PASSWORD` no frontend local apontando para a API remota (ou via `curl` no `/auth/login`).

## 7. Rotina de trabalho

- Deploy é automático a cada push. Para **reverter** um deploy ruim: **Deployments → ⋯ no deploy anterior → Rollback**. Leva segundos.
- Logs: aba **Deployments → View Logs**. Métricas básicas (CPU/RAM): aba **Metrics**.
- **Nunca** edite variável em produção sem avisar o parceiro — o serviço reinicia na hora.

## 8. Se algo der errado

| Sintoma | Causa provável |
|---|---|
| `Flyway validation failed` no log | Migration nova com erro de sintaxe MySQL — ver checklist de go-live ([Guia 6](06-go-live-checklist.md)) |
| `DATABASE_URL environment variable is required` | Profile `prod` sem banco ligado — confira se o MySQL está no mesmo projeto |
| `401` em tudo após trocar `JWT_SECRET` | Normal: tokens antigos invalidados, logue de novo |
| Build Maven falha (` Exit code 1`) | Ver o log: geralmente dependência nova no `pom.xml` sem versão ou teste quebrado — rode `mvn test` local antes do push |
