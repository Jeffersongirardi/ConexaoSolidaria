# Instruções para IAs trabalhando neste repositório (LER ANTES DE TUDO)

## 1. Registro de progresso — OBRIGATÓRIO

Este projeto é tocado por 2 desenvolvedores em momentos diferentes. Para o próximo
sempre saber em que pé está:

1. Leia `docs/00-status.md` **antes** de qualquer tarefa.
2. Ao concluir qualquer mudança (código, doc, config), **acrescente uma linha
   datada** na tabela de concluídos de `docs/00-status.md`, fuso **America/Sao_Paulo**
   (`Get-Date -Format "yyyy-MM-dd HH:mm"`). Sem data/hora, o trabalho NÃO conta como feito.
3. Se ficar algo pendente de infra/verificação, registre em "Próximos passos" ou
   "Pendências conscientes" do mesmo arquivo.
4. Inclua `docs/00-status.md` **no mesmo commit** da mudança.

## 2. Stack e comandos

- Backend: Spring Boot 3.2.4 + Java 17 (`backend/`). Frontend: Next.js 16 + React 19 (`frontend/`).
- Validações antes de commitar: `mvn -o test` no `backend/` (meta: 100% verde),
  `tsc --noEmit` e lint no `frontend/`.
- Via ferramenta shell: rode Maven como `cmd /c "mvn -o test > <log> 2>&1"` e leia o
  log com `Select-String` (a saída direta trunca). Passar `-Dflag=x` quebra o wrapper `rtk`;
  prefira `rtk mvn -o "-Dtest=NomeDoTeste" test` ou `cmd /c "mvn ..."`.
- `mvn clean` apaga `target/` (seguro; rebuild é offline a partir do `.m2` local).

## 3. Regras que quebram o build se ignoradas

- **Testes e `RateLimitFilter`** (20 req/min em `/auth/*`, filtro em memória compartilhado
  na suíte): classes novas de teste de API DEVEM reusar fixture única (`@BeforeAll` +
  `@TestInstance(PER_CLASS)`, emails/CNPJ aleatórios) — ver `PaymentFlowApiTest`.
- **Flyway**: dev/teste usam `db/migration/V1–V8` (H2). Prod usa `db/migration-mysql/`
  (pasta separada — o Flyway escaneia subpastas!). Regras: nunca `RENAME COLUMN`,
  `RENAME CONSTRAINT` nem `CREATE INDEX IF NOT EXISTS` (não existem em MySQL);
  novas migrations precisam rodar em **H2 e MySQL**. Se mover arquivos `.sql`,
  rode `mvn clean` (o `target/classes` guarda cópia obsoleta e dá "more than one migration").
- **Hibernate naming**: campos camelCase com sigla grudada (ex.: `copiaECola`) geram
  nomes físicos inesperados — use `@Column(name = "copia_e_cola")` explícito.
- Imports: controllers ficam em `...api`, services em `...service` — **não esquecer o `import`**.

## 4. O que NUNCA commitar

- `backend/src/main/resources/application-dev.properties` (H2 em arquivo + IP local — hack temporário).
- `frontend/next.config.ts` com `allowedDevOrigins` de IP local e `frontend/.env.local` com IP de teste.
- Secrets (`JWT_SECRET`, `RESEND_API_KEY`, `R2_SECRET_KEY`, senhas). Vão nas Variables da Railway/Vercel.

## 5. Convenções

- Commits em português, padrão do histórico: `feat:`, `fix:`, `docs:`, `refine:` (+ escopo opcional).
- Mensagens de erro da API em pt-BR. `ApiExceptionHandler`: `IllegalArgumentException` → 400, `IllegalStateException` → 409.
- Dinheiro SEMPRE direto à instituição (sem gateway/Asaas/custódia — ver Termos). PIX = BR Code real via `PixBrCodeService`.
- Docs que acompanham código: `readme.md`, `backend/readme.md`, `frontend/README.md`, `backend/PLAN.md`, `COMO_FUNCIONA.md`, guias em `docs/01–06`.
- Commits/push só com pedido explícito do usuário.
