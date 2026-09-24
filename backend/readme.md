# Conexões Solidárias — Backend

API REST em Spring Boot 3.2.4 + Java 17 que serve o app Conexões Solidárias. Conecta doadores a instituições validadas, com campanhas, doações (itens e valores), pagamentos, notificações, blog e painel administrativo.

## Funcionalidades

| Funcionalidade | Descrição |
|----------------|-----------|
| Cadastro de Doadores | Nome, CPF, e-mail, endereço, WhatsApp, data de nascimento |
| Cadastro de Instituições | CNPJ, razão social, chave PIX, validação administrativa |
| Campanhas com Fotos | Múltiplas imagens por campanha, galeria |
| Doação de Itens | Intenção → confirmação → atualizações com foto |
| Doação em Valor | PIX (QR Code), cartão, transferência — confirmação manual (MVP) |
| Dashboards | Doador e instituição com histórico, impacto e comprovantes |
| Painel Administrativo | Instituições, usuários, blog, mensagens |
| Blog | Posts com categorias, slugs, páginas individuais |
| Autenticação | JWT (access+refresh) + BCrypt, 3 papéis |
| Notificações | In-app com contador de não lidas |
| Recuperação de Senha | Token por e-mail (expira em 1h) |

## Stack

| Camada | Tecnologia |
|--------|-----------|
| Framework | Spring Boot 3.2.4, Java 17 |
| Banco | H2 (dev) / PostgreSQL (prod) — Flyway |
| ORM | Spring Data JPA + Hibernate 6 |
| Auth | Spring Security 6 + JWT (jjwt) |
| Docs | springdoc-openapi (Swagger UI) |
| QR Code | ZXing |
| Build | Maven |

## Executar

```bash
cd backend
mvn spring-boot:run        # dev (H2)
mvn spring-boot:run -Dspring-boot.run.profiles=prod  # prod (PostgreSQL)
```

- API: `http://localhost:8080/api/v1`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- H2 Console (dev): `http://localhost:8080/h2-console`

Credencial seed: `admin@conexoessolidarias.org` / `admin123` (via `ADMIN_EMAIL`/`ADMIN_PASSWORD` em prod).

## Pagamentos

Confirmação manual no MVP — sem gateway real por enquanto. PIX gera QR Code (ZXing) a partir da chave da instituição; cartão/transferência registram confirmação e notificação.

## Perfis

| Profile | Banco |
|---------|-------|
| `dev` | H2 em memória |
| `prod` | PostgreSQL (via `DATABASE_URL`) |
