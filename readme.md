# Conexões Solidárias

Plataforma que aproxima **doadores** de **instituições validadas** (CNPJ ativo) para doações de itens e valores — com acompanhamento até a confirmação de recebimento.

> **Natureza do produto:** mera vitrine de intermediação, **taxa sempre R$ 0**. Pix/cartão/transferência vão **direto para a conta da instituição**. A plataforma não recebe, segura, repassa ou estorna valores. Sem reembolso pela plataforma — divergências se resolvem direto com a instituição/banco. Ver [Termos de Uso](frontend/src/app/termos/page.tsx) e [Privacidade/LGPD](frontend/src/app/privacidade/page.tsx).

Nascido em Curitiba, aberto a instituições de todo o Brasil.

## Arquitetura

Monorepo com frontend e backend desacoplados, integrados via API REST/JSON:

```
projetoex/
├── frontend/   # Next.js (App Router) + React + TypeScript + Tailwind — PWA
├── backend/    # Spring Boot 3.2.4 + Java 17 — API REST (/api/v1)
```

| Camada | Tecnologia | Detalhes em |
|--------|-----------|-------------|
| Frontend | Next.js + React 19 + TS + Tailwind 4 | [frontend/README.md](frontend/README.md) |
| Backend | Spring Boot 3.2.4, Java 17, JPA/Hibernate, Security + JWT | [backend/readme.md](backend/readme.md) |
| Banco | H2 (dev local) / **MySQL (prod, Railway)** — Flyway (baseline MySQL único em prod) | [backend/readme.md](backend/readme.md) |
| Imagens | Disco local (dev e prod provisório; **R2 na Fase 3**) | [docs/02-cloudflare-r2.md](docs/02-cloudflare-r2.md) |
| E-mails | Log (dev; **Resend na Fase 3**) | [docs/03-resend-emails.md](docs/03-resend-emails.md) |
| Deploy | **Vercel (frontend) + Railway (backend)** — `conexaosolidarias.com.br` | [Guias de produção](#guias-de-produção-passo-a-passo-para-iniciantes) |

## Guias de produção (passo a passo, para iniciantes)

| Guia | Cobre |
|------|-------|
| [00 — Contas e acessos](docs/00-acessos.md) | Quem é dono do quê, colaborador GitHub, staging do parceiro |
| [01 — Backend + MySQL na Railway](docs/01-railway-backend-mysql.md) | Conta, projeto, MySQL, variáveis, healthcheck, deploy |
| [02 — Imagens no Cloudflare R2](docs/02-cloudflare-r2.md) | Bucket, token S3, URL pública |
| [03 — E-mails com Resend](docs/03-resend-emails.md) | API key, verificação do domínio, testes |
| [04 — Domínio e DNS](docs/04-dominio-dns.md) | Apex → Vercel, `api` → Railway, registros Resend |
| [05 — Frontend na Vercel](docs/05-vercel-frontend.md) | Root `frontend`, env vars, previews, rollback |
| [06 — Go-live checklist](docs/06-go-live-checklist.md) | Fumaça, segurança, troubleshooting, rollback |

## Execução local (5 minutos)

Pré-requisitos: Java 17 + Maven, Node 20+.

**1. Backend** — http://localhost:8080 (perfil `dev`, H2 em arquivo `./data/`, persiste entre restarts):

```bash
cd backend
mvn spring-boot:run
```

- API: `http://localhost:8080/api/v1` · Swagger: `http://localhost:8080/swagger-ui/index.html` · H2 Console: `http://localhost:8080/h2-console` (JDBC `jdbc:h2:file:./data/conexoessolidarias`, usuário `sa`)
- Seed automático: `admin@conexoessolidarias.org` / `admin123` (em prod via `ADMIN_EMAIL`/`ADMIN_PASSWORD`)

**2. Frontend** — http://localhost:3000:

```bash
cd frontend
npm install
npm run dev
```

Configure `frontend/.env.local` (não versionado):

```
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_API_ORIGIN=http://localhost:8080
```

**3. Testar o fluxo feliz:** cadastrar doador → abrir campanha → doar item (marcar coleta opcional) → painel doador (“Como entregar”) → logar como instituição → confirmar recebimento.

PWA instalável (“Instalar aplicativo” no navegador).

## Papéis e permissões

| Papel | Pode | Não pode |
|-------|------|----------|
| **Doador** | doar itens/valores, pedir coleta, cancelar intenção pendente, confirmar “já paguei” + comprovante | confirmar recebimentos, ver dados além do necessário à entrega |
| **Instituição** (aprovada, CNPJ 14 dígitos) | publicar campanhas + fotos, confirmar recebimentos, postar atualizações, ver WhatsApp do doador nas suas recebidas | cancelar doações, estornar, publicar sem aprovação |
| **Admin** | aprovar/recusar instituições e ofertas (motivo obrigatório), moderar doações (auditoria + cancelar), gerenciar usuários/blog/mensagens | confirmar entregas, movimentar valores |

## Fluxos principais

**Item físico:** intenção (pendente, “A combinar entrega”) → cartão pós-intenção com WhatsApp + endereço/instruções → entrega ou coleta solicitada → instituição confirma (recebido) → comprovante liberado (só após recebido). Doador pode cancelar se pendente.

**Valor (Pix direto):** pagamento pendente → doador paga no banco + “Já paguei” → instituição confere e confirma (recebido) → comprovante liberado a partir de confirmado. Transferência com anexo opcional (MVP). Cartão é registro manual (sem débito real, dados não salvos).

**Oferta (marketplace reverso):** doador publica com fotos + data de compromisso → admin aprova (ou recusa com motivo) → visível só a instituições aprovadas → instituição reivindica com aceite de coleta em 7 dias → coleta e confirma (ou desiste; doador pode liberar) → entregue. Edição após recusa volta à fila; cancelamento exige motivo.

**Logística:** campanha tem `instrucoesEntrega` + `enderecoEntrega` (fallback: endereço da instituição); doação tem `precisaColeta` + `enderecoColeta`.

## Métricas públicas (só o quantificável)

- 👥 **Doadores** — distintos, intenção não-cancelada
- 💰 **Em valores** — soma de pagamentos **confirmado + recebido** (`PaymentRepository.sumValorRecebido`)
- 🎁 **Itens recebidos** — contagem + breakdown por categoria (herdada da campanha)

Detalhes (o que, quanto, quem) ficam restritos aos painéis. Sem barra de % — removida por ser fictícia.

## Categorias oficiais

`alimento · roupa · calcado · higiene · fralda · material_escolar · brinquedo · movel · racao_animal · emergencia · outro`
Valores técnicos estáveis; rótulos plurais no frontend (`lib/categorias.ts`); backend normaliza desconhecidas para `outro` (`model/Categoria.java`); doação herda a categoria da campanha.

## LGPD em 30 segundos

DPO: `jefferson@fourpay.com.br` (resposta em até 15 dias). Compartilhamento mínimo necessário (contato doador↔instituição para viabilizar entrega). Retenção: conta ativa / doações 5 anos / contato 12 meses. Cartão: dados nunca armazenados. Detalhes na página de Privacidade.

## Deploy (produção)

Frontend na **Vercel**, backend + MySQL na **Railway**, imagens no **Cloudflare R2**, e-mails via **Resend**, domínio `conexaosolidarias.com.br`. Siga os [guias de produção](docs/01-railway-backend-mysql.md) na ordem (01 → 06).

**Env obrigatórias (Railway):** `SPRING_PROFILES_ACTIVE=prod`, `JWT_SECRET`, `FRONTEND_URL=https://conexaosolidarias.com.br`, `ADMIN_EMAIL`/`ADMIN_PASSWORD`. Depois: `RESEND_API_KEY`, `R2_ENDPOINT`/`R2_ACCESS_KEY`/`R2_SECRET_KEY`/`R2_BUCKET`/`R2_PUBLIC_URL`. Na Vercel: `NEXT_PUBLIC_API_URL` + `NEXT_PUBLIC_API_ORIGIN` apontando à API.

## Comandos úteis

```bash
cd backend && mvn test          # 28 testes (Auth:3, CampaignDonation:3, FluxoGuards:4, PaymentFlow:7, RailwayConfig:3, PixBrCode:8)
cd backend && mvn spring-boot:run
cd frontend && npm run dev
cd frontend && npm run build
```

## Troubleshooting

| Sintoma | Causa provável |
|---------|----------------|
| `could not initialize proxy - no Session` | coleção lazy acessada fora de transação — usar `@EntityGraph` + `@Transactional(readOnly = true)` (ver `DonationRepository`, `CampaignStatsService`) |
| `Cannot read properties of null (reading 'filter')` | estado inicial `null` no React — inicializar com `[]` |
| `No property 'inWithUpdates'` no boot | nome de método Spring Data inválido (`In` é operador) — manter nomes derivados + `@EntityGraph` |
| H2 `lock` / banco travado | 2 boots simultâneos no mesmo arquivo — `AUTO_SERVER=TRUE` mitiga; derrube o outro processo |
| Imagem 404 em prod | `NEXT_PUBLIC_API_ORIGIN` apontando para localhost — apontar ao backend público; uploads ainda em disco efêmero = faltam `R2_*` |
| `Flyway validation failed` em prod | Sintaxe não-MySQL no baseline (`IDENTITY`, `RENAME COLUMN`, `IF NOT EXISTS`) — corrigir `db/migration/mysql/` e recriar o database |
| `CORS blocked` no navegador | `FRONTEND_URL` sem `https://`, com `/` no fim, ou apontando ao domínio temporário |
| `429` nos testes | Janela de 1 min do `RateLimitFilter` em `/auth/*` — aguardar, não é bug |

## Histórico de decisões (2026-09 / 2026-10)

- Métrica pública: só doadores + R$ recebido + itens por categoria (sem % fictício)
- Logística pós-intenção com WhatsApp pronto + flag de coleta
- 11 categorias oficiais + herança campanha→doação
- H2 em arquivo **temporário** para testes locais (reverter para `mem:` quando sair do dev local)
- Fotos da campanha em passo único com preview (upload automático na edição)
- **Infra real (2026-10):** Railway (backend + MySQL) + Vercel (frontend) + R2 (imagens) + Resend (e-mails), domínio `conexaosolidarias.com.br`. Render/Postgres descartados
- **PIX direto com BR Code real** gerado da chave da instituição (sem gateway/Asaas: a plataforma nunca toca no valor — coerente com "taxa R$ 0"). Fluxo "Já paguei" mantido como oficial
