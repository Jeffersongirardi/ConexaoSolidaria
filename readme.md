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
| Banco | H2 (dev, arquivo local temporário) / PostgreSQL (prod) — Flyway V1–V4 | [backend/readme.md](backend/readme.md) |

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
| **Admin** | aprovar/recusar instituições (motivo obrigatório), gerenciar usuários/blog/mensagens | — |

## Fluxos principais

**Item físico:** intenção (pendente, “A combinar entrega”) → cartão pós-intenção com WhatsApp + endereço/instruções → entrega ou coleta solicitada → instituição confirma (recebido) → comprovante. Doador pode cancelar se pendente.

**Valor (Pix direto):** pagamento pendente → doador paga no banco + “Já paguei” (+ comprovante) → instituição confere e confirma (recebido). Cartão é registro manual (sem débito real, dados não salvos). Transferência com anexo opcional.

**Logística:** campanha tem `instrucoesEntrega` + `enderecoEntrega` (fallback: endereço da instituição); doação tem `precisaColeta` + `enderecoColeta`.

## Métricas públicas (só o quantificável)

- 👥 **Doadores** — distintos, intenção não-cancelada
- 💰 **Em valores** — soma de pagamentos **recebidos** (confirmados pela instituição)
- 🎁 **Itens recebidos** — contagem + breakdown por categoria (herdada da campanha)

Detalhes (o que, quanto, quem) ficam restritos aos painéis. Sem barra de % — removida por ser fictícia.

## Categorias oficiais

`alimento · roupa · calcado · higiene · fralda · material_escolar · brinquedo · movel · racao_animal · emergencia · outro`
Valores técnicos estáveis; rótulos plurais no frontend (`lib/categorias.ts`); backend normaliza desconhecidas para `outro` (`model/Categoria.java`); doação herda a categoria da campanha.

## LGPD em 30 segundos

DPO: `jefferson@fourpay.com.br` (resposta em até 15 dias). Compartilhamento mínimo necessário (contato doador↔instituição para viabilizar entrega). Retenção: conta ativa / doações 5 anos / contato 12 meses. Cartão: dados nunca armazenados. Detalhes na página de Privacidade.

## Deploy (Render)

Backend via `backend/render.yaml` (Java + Postgres free). **Obrigatórias:** `JWT_SECRET`, `FRONTEND_URL` (CORS + links), `MAIL_*` para e-mails reais. Limitações conhecidas: uploads em disco efêmero (fotos somem no restart — migrar para storage externo), frontend sem serviço dedicado (recomendado Vercel com `NEXT_PUBLIC_API_URL` apontando ao backend público).

## Comandos úteis

```bash
cd backend && mvn test          # 6 testes de API
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
| Imagem 404 em prod | `NEXT_PUBLIC_API_ORIGIN` apontando para localhost — apontar ao backend público |

## Histórico de decisões (2026-09)

- Métrica pública: só doadores + R$ recebido + itens por categoria (sem % fictício)
- Logística pós-intenção com WhatsApp pronto + flag de coleta
- 11 categorias oficiais + herança campanha→doação
- H2 em arquivo **temporário** para testes locais (reverter para `mem:` quando sair do dev local)
- Fotos da campanha em passo único com preview (upload automático na edição)
