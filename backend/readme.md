# Conexões Solidárias — Backend

API REST em Spring Boot 3.2.4 + Java 17. Prefixo de todas as rotas: `/api/v1`. Docs interativas: `http://localhost:8080/swagger-ui/index.html` (springdoc).

## Stack

| Camada | Tecnologia |
|--------|-----------|
| Framework | Spring Boot 3.2.4, Java 17, Maven |
| Banco | H2 em arquivo (dev, `./data/`, temporário) / PostgreSQL (prod) — **Flyway V1–V4** |
| ORM | Spring Data JPA + Hibernate 6 (`ddl-auto=validate`, `open-in-view=false`) |
| Auth | Spring Security 6 + JWT (access 60min + refresh 7 dias, jjwt) + BCrypt |
| Extras | ZXing (QR Pix), springdoc-openapi, Actuator (health/info), JavaMail (log em dev) |

## Perfis e configuração

| Profile | Banco | Arquivo |
|---------|-------|---------|
| `dev` (padrão) | H2 em arquivo `./data/conexoessolidarias` (persiste entre restarts) | `application-dev.properties` |
| `prod` | PostgreSQL via `DATABASE_URL` (Render) | `application-prod.properties` |

Env obrigatórias em prod: `JWT_SECRET`, `FRONTEND_URL` (CORS + links de e-mail), `DATABASE_URL`. Opcionais: `ADMIN_EMAIL`/`ADMIN_PASSWORD`, `MAIL_HOST/PORT/USERNAME/PASSWORD`, `UPLOAD_DIR` (padrão `./uploads` — disco efêmero no Render).

```bash
mvn spring-boot:run        # dev
mvn test                   # 6 testes (AuthApiTest, CampaignDonationApiTest)
mvn clean package -DskipTests   # build do Render
```

Seed: `DataSeeder` cria `admin@conexoessolidarias.org` / `admin123` se não existir admin.

## Migrations (Flyway)

| Versão | Conteúdo |
|--------|----------|
| V1 baseline | Schema completo (users, institutions, campaigns, donations, payments, blog, etc.) |
| V2 | Rename `need` → `campaign` |
| V3 | Índices |
| V4 | Logística: `campaigns.instrucoes_entrega`, `campaigns.endereco_entrega`, `donations.precisa_coleta`, `donations.endereco_coleta` |

## Endpoints

Papel entre parênteses. Públicos não exigem token.

**Auth** (`AuthApiController`): `POST /auth/register/doador`, `POST /auth/register/instituicao` (CNPJ formato validado), `POST /auth/login`, `POST /auth/refresh`, `POST /auth/forgot-password`, `POST /auth/reset-password`.

**Campaigns** (`CampaignApiController`): `GET /campaigns` (filtros categoria/urgência/busca, paginado), `GET /campaigns/destaques`, `GET /campaigns/{id}`, `GET /campaigns/minhas` (INSTITUICAO), `POST /campaigns` (INSTITUICAO aprovada), `PUT /campaigns/{id}`, `PATCH /campaigns/{id}/toggle`, `DELETE /campaigns/{id}`, `POST /campaigns/{id}/imagens` (multipart `imagem`, png/jpg/gif/webp ≤5MB), `DELETE /campaigns/imagens/{imgId}`. Leitura retorna `numDoadores`, `valorRecebido`, `numDoacoesItens`, `itensPorCategoria` (via `CampaignStatsService`).

**Donations** (`DonationApiController`): `POST /donations` (DOADOR, herda categoria da campanha, flags de coleta), `GET /donations/minhas` (DOADOR, `@EntityGraph updates`), `GET /donations/recebidas` (INSTITUICAO dona), `GET /donations/{id}` (participante), `PATCH /donations/{id}/confirmar` (INSTITUICAO, → recebido + progresso), `PATCH /donations/{id}/cancelar` (DOADOR, só pendente), `POST /donations/{id}/atualizacoes` (INSTITUICAO, multipart + notificação ao doador).

**Payments** (`PaymentApiController`): `POST /payments` (DOADOR, valida campanha ativa **e** `aceitaFinanceiro`), `GET /payments/meus`, `GET /payments/recebidos` (INSTITUICAO), `GET /payments/{uuid}`, `GET /payments/{uuid}/comprovante`, `POST /payments/{uuid}/confirmar-pix|confirmar-cartao|confirmar-transferencia` (DOADOR, pendente→confirmado + `TXN-`), `PATCH /payments/{uuid}/confirmar-recebimento` (INSTITUICAO, confirmado→recebido). Confirmação manual no MVP — sem gateway.

**Institutions** (`InstitutionApiController`): `GET /institutions`, `GET /institutions/{id}`, `GET /institutions/minha`, `PUT /institutions/minha`, `POST /institutions/minha/foto`.

**Users** (`UserApiController`): `GET /users/me`, `PUT /users/me`, `POST /users/me/avatar`, `POST /users/me/password`.

**Admin** (`AdminApiController`): `GET /admin/dashboard`, `GET /admin/institutions` (`?filtro=todas|pendentes|aprovadas`), `PATCH /admin/institutions/{id}/aprovar` (exige CNPJ 14 dígitos), `PATCH .../recusar` (motivo obrigatório), `GET /admin/users`, `PATCH /admin/users/{id}/toggle`, `GET /admin/messages`, `PATCH .../ler`, `DELETE ...`.

**Outros:** `BlogApiController` (CRUD + slug/categorias), `NotificationApiController` (`GET /`, `/nao-lidas`, `PATCH /{id}/ler`, `/ler-todas`), `ContactApiController` (`POST /contact`), `StatsApiController` (`GET /stats` → doaçõesRecebidas, instituições, campanhasAtivas).

## Regras de negócio (não quebrar)

- Pagamento exige campanha ativa **e** `aceitaFinanceiro=true`.
- Aprovação de instituição exige CNPJ com 14 dígitos.
- Categoria (`model/Categoria.java`): 11 oficiais; desconhecida → `outro`; doação herda da campanha.
- `Donation.updates` e imagens: `@EntityGraph` + `@Transactional(readOnly = true)` nos GETs (evita `LazyInitializationException`). Nomes de métodos derivados não podem conter `With...` após `In` (parser entende `In` como operador).
- Uploads: `FileStorageService` (UUID, MIME+tamanho, anti-traversal) → `/uploads/**` público. Delete de imagem apaga só o banco (órfão em disco — débito técnico).
- Notificações in-app em: nova doação, confirmação, atualização de doação, pagamento confirmado.

## Estrutura

```
src/main/java/com/conexoessolidarias/
├── api/            # 12 controllers + dto/ + ApiExceptionHandler
├── model/          # User, InstitutionProfile, Campaign, Donation, Payment, ...
├── repository/     # Spring Data (queries + @EntityGraph + @Query agregadas)
├── service/        # CampaignStatsService, NotificationService, EmailService, StorageService, QrCodeService
├── security/       # JWT, CustomUserDetails, filtros
└── config/         # DataSeeder, RenderDataSourceConfig, WebConfig (/uploads)
src/main/resources/db/migration/  # V1–V4
```
