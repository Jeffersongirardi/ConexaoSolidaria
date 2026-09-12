# Conexões Solidárias

**Conexões Solidárias** é uma plataforma web que conecta doadores a instituições sociais, facilitando a doação de alimentos, roupas, recursos financeiros e outros itens para comunidades carentes de forma eficiente, transparente e acessível.

Projeto acadêmico — Atividade Extensionista III: Análise (Engenharia de Software).

## Funcionalidades

| Funcionalidade | Descrição |
|----------------|-----------|
| Cadastro de Doadores | Nome, CPF, e-mail, endereço, WhatsApp, data de nascimento |
| Cadastro de Instituições | CNPJ, razão social, chave PIX, validação administrativa |
| Necessidades com Fotos | Múltiplas imagens por necessidade, galeria, zoom modal |
| Doação de Itens Físicos | Doador registra intenção, instituição confirma recebimento |
| Doação Financeira | PIX (QR Code), cartão de crédito, transferência bancária — simulados |
| Dashboard do Doador | Histórico de doações físicas e financeiras, impacto, comprovante |
| Dashboard da Instituição | Gerenciar necessidades, confirmar doações, atualizar destino, analytics |
| Atualização de Destino | Instituição posta fotos e mensagens do destino das doações |
| Painel Administrativo | CRUD de instituições, usuários, blog, mensagens de contato |
| Blog Dinâmico | Posts com categorias, slugs, páginas individuais |
| Recuperação de Senha | Fluxo completo com token e e-mail |
| Notificações | Notificações in-app com badge de não lidas |
| Autenticação | Login separado para doador, instituição e admin (Spring Security) |
| LGPD | Consentimento no cadastro com link para política de privacidade |
| Compartilhamento Social | WhatsApp, Facebook, Twitter/X, copiar link |
| FAQ Interativo | Página de perguntas frequentes com accordion |
| Perfil Público | Página pública para cada instituição com suas necessidades |
| Comprovante de Doação | Página imprimível/PDF para doações físicas e financeiras |
| Busca e Filtros | Filtro por categoria, urgência e busca textual |

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
| Uploads | Sistema próprio com validação de tipo/tamanho |

## Estrutura do Projeto

```
projetoex/
├── pom.xml
├── readme.md
├── PLAN.md
├── src/
│   ├── main/
│   │   ├── java/com/conexoessolidarias/
│   │   │   ├── ConexoesSolidariasApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── WebConfig.java
│   │   │   │   ├── GlobalControllerAdvice.java
│   │   │   │   └── DataSeeder.java
│   │   │   ├── controller/
│   │   │   │   ├── MainController.java
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── NeedController.java
│   │   │   │   ├── InstituicaoController.java
│   │   │   │   ├── BlogController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── NotificationController.java
│   │   │   │   ├── ProfileController.java
│   │   │   │   ├── AdminController.java
│   │   │   │   └── PasswordResetController.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── InstitutionProfile.java
│   │   │   │   ├── Need.java
│   │   │   │   ├── NeedImage.java
│   │   │   │   ├── Donation.java
│   │   │   │   ├── DonationUpdate.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── Notification.java
│   │   │   │   ├── ContactMessage.java
│   │   │   │   ├── BlogPost.java
│   │   │   │   └── PasswordResetToken.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── InstitutionProfileRepository.java
│   │   │   │   ├── NeedRepository.java
│   │   │   │   ├── NeedImageRepository.java
│   │   │   │   ├── DonationRepository.java
│   │   │   │   ├── DonationUpdateRepository.java
│   │   │   │   ├── PaymentRepository.java
│   │   │   │   ├── NotificationRepository.java
│   │   │   │   ├── ContactMessageRepository.java
│   │   │   │   ├── BlogPostRepository.java
│   │   │   │   └── PasswordResetTokenRepository.java
│   │   │   ├── security/
│   │   │   │   ├── CustomUserDetails.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   └── service/
│   │   │       ├── FileStorageService.java
│   │   │       ├── NotificationService.java
│   │   │       ├── QrCodeService.java
│   │   │       └── EmailService.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       ├── static/
│   │       │   ├── css/styles.css
│   │       │   ├── js/scripts.js
│   │       │   └── js/masks.js
│   │       └── templates/
│   │           ├── fragments.html
│   │           ├── index.html
│   │           ├── sobre.html
│   │           ├── contato.html
│   │           ├── privacidade.html
│   │           ├── faq.html
│   │           ├── login-doador.html
│   │           ├── login-instituicao.html
│   │           ├── login-admin.html
│   │           ├── cadastro-doador.html
│   │           ├── cadastro-instituicao.html
│   │           ├── dashboard-doador.html
│   │           ├── dashboard-instituicao.html
│   │           ├── necessidades-listar.html
│   │           ├── necessidades-detalhe.html
│   │           ├── need-editar.html
│   │           ├── perfil.html
│   │           ├── alterar-senha.html
│   │           ├── notificacoes.html
│   │           ├── blog-publico.html
│   │           ├── blog-post.html
│   │           ├── instituicoes.html
│   │           ├── instituicao-publica.html
│   │           ├── recuperar-senha.html
│   │           ├── redefinir-senha.html
│   │           ├── comprovante.html
│   │           ├── pagamento-pix.html
│   │           ├── pagamento-cartao.html
│   │           ├── pagamento-transferencia.html
│   │           ├── pagamento-sucesso.html
│   │           ├── error/404.html
│   │           ├── error/403.html
│   │           ├── error/500.html
│   │           ├── error/error.html
│   │           └── admin/
│   │               ├── dashboard.html
│   │               ├── instituicoes.html
│   │               ├── usuarios.html
│   │               ├── mensagens.html
│   │               ├── blog.html
│   │               └── blog-form.html
│   └── test/ (a implementar)
└── uploads/ (criado automaticamente)
    ├── needs/
    ├── updates/
    ├── avatars/
    └── comprovantes/
```

## Setup Local

### Pré-requisitos

- Java 17+
- Maven 3.8+

### Executar

```bash
# Clonar
git clone https://github.com/Jeffersongirardi/ConexaoSolidaria.git
cd projetoex

# Compilar e executar
mvn spring-boot:run
```

Acesse `http://localhost:8080`.

### Perfil de Desenvolvimento

Por padrão, a aplicação usa H2 em memória (dados recriados a cada restart) e o profile `dev`.

```bash
mvn spring-boot:run
```

Para usar PostgreSQL local:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Credenciais Padrão (Admin)

- **E-mail:** `admin@conexoessolidarias.org`
- **Senha:** `admin123`

> O admin é criado automaticamente na primeira execução (DataSeeder).

### H2 Console

Disponível em `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:conexoessolidarias`).

## Perfis

| Profile | Banco | Finalidade |
|---------|-------|-----------|
| `dev` (padrão) | H2 em memória | Desenvolvimento local |
| `prod` | PostgreSQL | Produção / homologação |

## Pagamentos — Nota Importante

O sistema de **pagamentos é simulado** (não integra com gateways reais):

- **PIX:** Exibe a chave PIX da instituição e gera QR Code (ZXing). Confirmação manual marca como pago.
- **Cartão:** Formulário de cartão (dados não são salvos). Confirmação manual.
- **Transferência:** Exibe dados bancários da instituição. Upload de comprovante é aceito.

## ODS e Impacto Social

- **ODS 1** — Erradicação da pobreza
- **ODS 2** — Fome zero e agricultura sustentável
- **ODS 10** — Redução das desigualdades
- **ODS 17** — Parcerias e meios de implementação

## Licença

Projeto acadêmico — escopo de teste e desenvolvimento.
