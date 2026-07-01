# Plano de Desenvolvimento — Conexões Solidárias

> **Status atual:** Funcionalidades core completas. Projeto em manutenção.

---

## Fase 0 — Setup do Repositório

- [x] Inicializar git no diretório `projetoex/`
- [x] Criar `.gitignore` (`.env`, `__pycache__/`, `*.pyc`, `.venv/`, `.vscode/`)
- [x] Criar repositório no GitHub com nome `ConexaoSolidaria`
- [x] Configurar remote e fazer push inicial
- [x] Conectar ao Render

---

## Fase 1 — Fundação Técnica

### 1.1 Banco de Dados
- [x] Adicionar SQLAlchemy ao projeto
- [x] SQLite em desenvolvimento, PostgreSQL em produção (Render)
- [x] Modelos iniciais:
  - `User` (base, com tipo: doador/instituicao/admin)
  - `Institution` → refatorado para `InstitutionProfile`
  - `Donation` (item, quantidade, status, data)
  - `Need` (descrição, quantidade alvo, urgência, progresso)
  - `Payment`, `NeedImage`, `DonationUpdate`, `Notification`, `BlogPost`, `ContactMessage`

### 1.2 Autenticação Real
- [x] Flask-Login + Werkzeug Security
- [x] Login de doadores (email + senha)
- [x] Login de instituições (email + senha)
- [x] Login de admin (rota separada `/auth/login-admin`)
- [x] Proteção de rotas (`@login_required`)
- [x] Logout

### 1.3 Templates com Jinja2
- [x] Migrar HTMLs estáticos para Jinja2
- [x] `base.html` com navbar e footer (DRY)
- [x] `extends` e `blocks` para conteúdo
- [x] Variáveis de ambiente com `python-dotenv`
- [x] `config.py` para separar configurações

### 1.4 Estrutura do Projeto (atual)

```
ConexaoSolidaria/
├── app.py
├── config.py
├── requirements.txt
├── .env
├── .gitignore
├── PLAN.md
├── models/
│   ├── __init__.py
│   ├── user.py
│   ├── institution.py
│   ├── need.py
│   ├── need_image.py
│   ├── donation.py
│   ├── donation_update.py
│   ├── payment.py
│   ├── notification.py
│   ├── contact_message.py
│   └── blog_post.py
├── routes/
│   ├── auth.py
│   ├── main.py
│   ├── dashboard.py
│   ├── admin.py
│   ├── necessidades.py
│   ├── perfil.py
│   ├── notificacoes.py
│   ├── blog.py
│   ├── pagamento.py
│   └── upload.py
├── templates/
│   ├── base.html
│   ├── index.html
│   ├── login-doador.html
│   ├── login-instituicao.html
│   ├── login-admin.html
│   ├── cadastro-doador.html
│   ├── cadastro-instituicao.html
│   ├── dashboard-doador.html
│   ├── dashboard-instituicao.html
│   ├── necessidades-listar.html
│   ├── necessidades-detalhe.html
│   ├── need-editar.html
│   ├── perfil.html
│   ├── alterar-senha.html
│   ├── notificacoes.html
│   ├── blog-publico.html
│   ├── blog-post.html
│   ├── pagamento-fake.html
│   ├── pagamento-pix.html
│   ├── pagamento-cartao.html
│   ├── pagamento-transferencia.html
│   ├── pagamento-sucesso.html
│   ├── 403.html
│   ├── 404.html
│   ├── admin/
│   │   ├── dashboard.html
│   │   ├── instituicoes.html
│   │   ├── usuarios.html
│   │   ├── mensagens.html
│   │   ├── blog.html
│   │   └── blog-form.html
│   ├── sobre.html
│   ├── contato.html
│   └── privacidade.html
├── static/
│   ├── css/styles.css
│   ├── js/scripts.js
│   └── js/masks.js
└── uploads/ (criado automaticamente)
    ├── needs/
    ├── updates/
    ├── avatars/
    └── comprovantes/
```

---

## Fase 2 — Funcionalidades Core

### 2.1 Cadastro e Onboarding
- [x] Cadastro de doadores (nome, email, senha, telefone, **CPF, endereço, WhatsApp**)
- [x] Cadastro de instituições (CNPJ, razão social, endereço, docs, **PIX, nome fantasia, WhatsApp**)
- [x] Validação de CNPJ (front-end com máscara)
- [x] Verificação de documentação (instituições pendentes até aprovação admin)

### 2.2 Gerenciamento de Necessidades
- [x] Instituições criam necessidades (título, descrição, qtd, urgencia, **fotos**)
- [x] Editar necessidade (título, descrição, fotos, toggle ativo/inativo)
- [x] Listar necessidades na home e no dashboard
- [x] Barra de progresso dinâmica
- [x] Filtrar por urgência/categoria
- [x] Galeria de imagens com zoom modal
- [x] Aceitar doações financeiras por necessidade

### 2.3 Sistema de Doações
- [x] Doador seleciona necessidade e faz doação de item físico
- [x] Status da doação: pendente → recebido / cancelado
- [x] Doação financeira (PIX, cartão, transferência — **simulado**)
- [x] Atualização de destino (instituição posta mensagem + foto)
- [x] Histórico de doações por usuário
- [x] Notificação para instituição quando receber doação
- [x] Confirmação de recebimento pela instituição

### 2.4 Formulário de Contato
- [x] Backend para salvar mensagens no banco
- [x] Painel admin visualiza e gerencia mensagens

---

## Fase 3 — UX e Conteúdo

### 3.1 Interface
- [x] Navbar e footer componentizados (Jinja2 includes)
- [x] Hamburger menu no mobile
- [x] Tabs nos dashboards (necessidades, doações físicas, financeiras)
- [x] Máscaras de input (CPF, CNPJ, CEP, telefone, cartão, CVV)
- [x] Preview de imagens no upload
- [x] Flash messages com auto-dismiss

### 3.2 Conteúdo
- [ ] Fotos reais da equipe (substituir placeholders)
- [ ] Imagens do blog reais
- [ ] Textos institucionais definitivos
- [ ] Estatísticas reais (números de impacto)

### 3.3 Blog Dinâmico
- [x] Modelo `BlogPost` (autor, título, conteúdo, data, categoria, imagem)
- [x] Admin cria/edita posts (admin/blog-form.html)
- [x] Páginas individuais de post (blog-post.html)
- [x] Listagem pública com categorias e filtros (blog-publico.html)

---

## Fase 4 — Polimento

### 4.1 SEO e Performance
- [ ] Meta tags e Open Graph em todas as páginas
- [ ] Sitemap.xml
- [ ] Minificação de CSS/JS
- [ ] Cache headers

### 4.2 Funcionalidades Avançadas
- [ ] Mapa de instituições próximas (Google Maps API ou Leaflet)
- [ ] Dashboard com gráficos (Chart.js) — *parcial: admin com tabelas*
- [ ] Relatório de impacto para doadores

### 4.3 Qualidade
- [ ] Testes unitários (pytest)
- [ ] Validação de formulários (back-end) — *parcial: campos obrigatórios*
- [x] Tratamento de erros (404, 403)
- [ ] Logging

---

## Fase 5 — Recursos Implementados (pós-plano inicial)

- [x] **Upload de imagens** nas necessidades (múltiplas fotos, galeria)
- [x] **Pagamento fake** (PIX com QR Code, cartão, transferência)
- [x] **Destino das doações** (instituição publica atualizações com foto)
- [x] **Editar necessidade** (formulário completo com gerenciamento de fotos)
- [x] **Cadastro estendido** (CPF, WhatsApp, endereço para doadores; PIX, nome fantasia para instituições)
- [x] **Notificações in-app** com badge de não lidas
- [x] **Painel admin completo** (CRUD instituições, usuários, blog, mensagens)
- [x] **Avatar** no perfil do usuário
- [x] **Máscaras JS** (CPF, CNPJ, CEP, telefone, cartão de crédito, CVV, validade)
- [x] **Migração automática de schema** (detecta colunas antigas e recria)
- [x] **Seed automático** de admin na primeira execução

---

## Tecnologias

| Finalidade | Tecnologia |
|------------|-----------|
| Backend | Flask + SQLAlchemy |
| Banco | PostgreSQL (prod), SQLite (dev) |
| Autenticação | Flask-Login + Werkzeug Security |
| Templates | Jinja2 |
| Frontend | CSS puro + JS vanilla |
| Máscaras | JS vanilla (`masks.js`) |
| QR Code PIX | `qrcode[pil]` (opcional) |
| Deploy | Render (gunicorn) |
