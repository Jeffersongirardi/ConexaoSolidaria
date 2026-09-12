# Plano de Desenvolvimento — Conexões Solidárias (Spring Boot)

> **Status atual:** Migração Flask → Spring Boot concluída. Funcionalidades core operacionais.
> **Última atualização:** Julho/2026

---

## Fase 0 — Migração Flask → Spring Boot

### 0.1 Setup do Projeto Spring Boot
- [x] Criar `pom.xml` com Spring Boot 3.2.4, Java 17, Thymeleaf, Security, JPA, H2, PostgreSQL, ZXing
- [x] Configurar perfis (`dev` com H2, `prod` com PostgreSQL)
- [x] Configurar Spring Security (BCrypt, 3 roles: DOADOR/INSTITUICAO/ADMIN)
- [x] Configurar WebMVC (recursos estáticos, uploads)
- [x] `GlobalControllerAdvice` com dados globais (currentPath, needCount, notificacoesNaoLidas)
- [x] `DataSeeder` para criar admin automaticamente

### 0.2 Modelos JPA
- [x] `User` (tipo: doador/instituicao/admin, ativo, avatar, endereço, WhatsApp)
- [x] `InstitutionProfile` (CNPJ, razão social, PIX, aprovação)
- [x] `Need` (categoria, urgência, aceitaFinanceiro, progresso, imagens)
- [x] `NeedImage` (filename, legenda, ordem)
- [x] `Donation` (físico/financeiro, status, updates)
- [x] `DonationUpdate` (mensagem, foto, data)
- [x] `Payment` (PIX/cartão/transferência, status, UUID, comprovante)
- [x] `Notification` (tipo, mensagem, link, lida)
- [x] `ContactMessage` (nome, email, assunto, lido)
- [x] `BlogPost` (slug único, autor, publicado)
- [x] `PasswordResetToken` (token, expiry, usado)

### 0.3 Repositórios
- [x] 11 interfaces Spring Data JPA com queries customizadas

### 0.4 Controllers
- [x] `MainController` — Home, Sobre, Contato, Privacidade, FAQ
- [x] `AuthController` — Login, Cadastro doador/instituição
- [x] `DashboardController` — Painéis, CRUD necessidades, doações, comprovante
- [x] `NeedController` — Listagem pública com filtros e paginação
- [x] `InstituicaoController` — Perfil público da instituição
- [x] `PaymentController` — Fluxo de pagamento (PIX/cartão/transferência)
- [x] `NotificationController` — Notificações in-app
- [x] `ProfileController` — Perfil e alterar senha
- [x] `AdminController` — CRUD admin (instituições, usuários, blog, mensagens)
- [x] `BlogController` — Blog público
- [x] `PasswordResetController` — Recuperação de senha

### 0.5 Services
- [x] `FileStorageService` — Upload com validação de tipo/tamanho
- [x] `NotificationService` — Criação e gerenciamento de notificações
- [x] `QrCodeService` — Geração de QR Code PIX com ZXing
- [x] `EmailService` — Envio de e-mails (log em desenvolvimento)

### 0.6 Templates Thymeleaf
- [x] `fragments.html` — Header, footer, flash messages, notificações
- [x] 14 templates públicos (home, sobre, contato, privacidade, faq, login, cadastro, necessidades, blog, instituições, FAQ)
- [x] 7 templates de dashboard/painel (doador, instituição, admin)
- [x] 5 templates de pagamento (PIX, cartão, transferência, sucesso, comprovante)
- [x] 4 templates de erro (403, 404, 500, genérico)
- [x] 3 templates auxiliares (recuperar senha, redefinir senha, comprovante)

---

## Fase 1 — Funcionalidades Core

### 1.1 Autenticação e Cadastro
- [x] Login separado para doador, instituição e admin
- [x] Cadastro de doadores com CPF, endereço, WhatsApp, data de nascimento
- [x] Cadastro de instituições com CNPJ, razão social, PIX, aprovação admin
- [x] Proteção de rotas por role (Spring Security)
- [x] Logout
- [x] Recuperação de senha com token por e-mail
- [x] Consentimento LGPD no cadastro

### 1.2 Gerenciamento de Necessidades
- [x] CRUD completo (criar, editar, ativar/desativar)
- [x] Múltiplas imagens por necessidade
- [x] Galeria com zoom modal
- [x] Barra de progresso dinâmica
- [x] Filtros por categoria, urgência e busca textual
- [x] Paginação com números de página

### 1.3 Sistema de Doações
- [x] Doação de itens físicos (intenção → confirmação → destino)
- [x] Doação financeira simulada (PIX, cartão, transferência)
- [x] Atualização de destino com foto (instituição)
- [x] Status: pendente → recebido/confirmado / cancelado
- [x] Comprovante de doação imprimível/PDF

### 1.4 Perfil Público
- [x] Listagem de instituições aprovadas
- [x] Página individual com informações e necessidades

---

## Fase 2 — UX e Dashboard

### 2.1 Interface
- [x] Navbar responsiva com hamburger menu
- [x] Badges de urgência (alta/vermelho, média/amarelo, baixa/verde)
- [x] Tabs nos dashboards
- [x] Máscaras de input (CPF, CNPJ, CEP, telefone)
- [x] Preview de imagens no upload
- [x] Flash messages
- [x] Compartilhamento social (WhatsApp, Facebook, Twitter, copiar link)

### 2.2 Dashboard Analytics
- [x] Estatísticas de doações (total, recebidas, pendentes)
- [x] Total de valor recebido em doações financeiras
- [x] Categorias mais doadas
- [x] Indicadores de impacto

### 2.3 Blog Dinâmico
- [x] CRUD completo (admin cria/edita/deleta)
- [x] Slugs únicos
- [x] Páginas públicas com filtro por categoria
- [x] Páginas individuais de post

---

## Fase 3 — Qualidade e Manutenção

### 3.1 Tratamento de Erros
- [x] Páginas amigáveis para 403, 404, 500 e erros genéricos
- [x] Validação de formulários (front-end: CPF, CNPJ, confirmação de senha)
- [ ] Testes unitários e de integração

### 3.2 Deploy
- [ ] Configurar PostgreSQL no Render
- [ ] Ajustar `application-prod.properties` para ambiente real
- [ ] Configurar variáveis de ambiente no Render

---

## Fase 4 — Melhorias Futuras

| Prioridade | Feature | Descrição |
|-----------|---------|-----------|
| Média | Campanhas Sazonais | Instituições criarem campanhas com data fim |
| Média | Voluntariado | Cadastro de voluntários além de doações |
| Baixa | API REST | Endpoints JSON para integração mobile |
| Baixa | Gamificação | Ranking de doadores com consentimento |
| Baixa | Mapa | Mapa de instituições próximas (Leaflet) |
| Baixa | SEO | Meta tags, Open Graph, sitemap.xml |

---

## Tecnologias

| Finalidade | Tecnologia |
|------------|-----------|
| Backend | Spring Boot 3.2.4 + Java 17 |
| Banco | H2 (dev) / PostgreSQL (prod) |
| ORM | Spring Data JPA + Hibernate 6 |
| Autenticação | Spring Security 6 + BCrypt |
| Templates | Thymeleaf 3 + Spring Security Dialect |
| Frontend | CSS puro + JS vanilla |
| Máscaras | JS vanilla (CPF, CNPJ, CEP, telefone) |
| QR Code PIX | ZXing |
| Build | Maven |
