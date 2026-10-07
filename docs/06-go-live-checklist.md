# Guia 6 — Go-live: checklist final + troubleshooting

> Execute na ordem, marcando cada item. Responsável: quem fez o deploy, com o parceiro revisando.

## Checklist

- [ ] **1. Variáveis Railway conferidas**: `SPRING_PROFILES_ACTIVE=prod`, `JWT_SECRET` (novo e forte), `FRONTEND_URL=https://conexaosolidarias.com.br` (https, sem `/` no fim), `ADMIN_EMAIL/ADMIN_PASSWORD`, `RESEND_API_KEY`, `R2_*` ([Guia 1](01-railway-backend-mysql.md)).
- [ ] **2. Banco**: Flyway criou as tabelas no MySQL (log mostra `Successfully validated` + `migrated`). `ddl-auto=validate` passando.
- [ ] **3. Health**: `https://api.conexaosolidarias.com.br/actuator/health` → `{"status":"UP"}`.
- [ ] **4. DNS**: apex abre o site, `api` responde a API, Resend `Verified` ([Guia 4](04-dominio-dns.md)).
- [ ] **5. Fumaça nos 3 papéis**: doador cadastra → doa item → "Como entregar"; instituição confirma recebimento; admin aprova instituição/oferta.
- [ ] **6. Dinheiro (PIX direto)**: instituição cadastra chave PIX válida; QR gerado é lido por **2 apps de banco diferentes**; "Já paguei" → `confirmado`; instituição confirma → `recebido`; comprovante abre.
- [ ] **7. Fotos**: upload vai para o R2 (URL `https://...`, não `/uploads/...`) e abre em aba anônima.
- [ ] **8. E-mails reais**: "esqueci senha" chega como `contato@conexaosolidarias.com.br` com link do domínio final.
- [ ] **9. Segurança**: `ADMIN_PASSWORD` inicial **trocada**; `admin123` não existe mais em lugar nenhum.
- [ ] **10. Limpeza**: conta/serviços antigos do Render desligados; `render.yaml` e driver Postgres removidos do código; hacks locais (`application-dev.properties` com H2 em arquivo, `allowedDevOrigins` com IP) **fora** do git.
- [ ] **11. Vigília 48h**: logs Railway + Vercel sem erro 500; checar `/actuator/health` 1x ao dia na primeira semana.

## Troubleshooting

| Sintoma | Causa provável / ação |
|---|---|
| `Flyway validation failed` / `migration failed` | Sintaxe não-MySQL (`GENERATED ... AS IDENTITY`, `RENAME COLUMN/CONSTRAINT`, `CREATE INDEX IF NOT EXISTS`). Banco é novo: corrija o baseline `db/migration/mysql/` e recrie o database |
| `CORS blocked` no console do navegador | `FRONTEND_URL` sem `https://`, com `/` no fim, ou apontando ao domínio temporário |
| `401` em tudo de repente | `JWT_SECRET` foi trocado — todos precisam logar de novo (esperado) |
| Imagem 404 | `NEXT_PUBLIC_API_ORIGIN` com localhost na Vercel, ou upload ainda em disco efêmero (faltam `R2_*`) |
| `429 Too Many Requests` nos testes | Janela de 1 min do `RateLimitFilter` em `/auth/*` — aguarde, não é bug (ver `PaymentFlowApiTest`, que usa fixture única por classe) |
| H2 local "travado" (`lock`) | Dois boots no mesmo arquivo — derrube o outro processo (`AUTO_SERVER=TRUE` só mitiga) |
| E-mail não chega | Domínio Resend ainda `Pending`, ou `RESEND_API_KEY` ausente (nesse caso cai no fallback de log — ver logs da Railway) |

## Rollback

- **Backend**: Railway → Deployments → deploy anterior → Rollback.
- **Frontend**: Vercel → Deployments → deploy anterior → Promote to Production.
- **Banco**: migrations Flyway não têm "desfazer" automático para DDL — em emergência, restaure o snapshot do MySQL na Railway (ative backups antes do go-live).
