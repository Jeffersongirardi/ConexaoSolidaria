# Plano de Desenvolvimento — Conexões Solidárias

Plataforma Flask que conecta doadores a instituições sociais em Curitiba.

## Fase 0 — Setup do Repositório

- [ ] Inicializar git no diretório `projetoex/`
- [ ] Criar `.gitignore` (`.env`, `__pycache__/`, `*.pyc`, `.venv/`, `.vscode/`)
- [ ] Criar repositório no GitHub com nome `ConexaoSolidaria`
- [ ] Configurar remote e fazer push inicial
- [ ] Conectar ao Render (substituir deploy atual)

---

## Fase 1 — Fundação Técnica

### 1.1 Banco de Dados
- [ ] Adicionar SQLAlchemy ao projeto
- [ ] SQLite em desenvolvimento, PostgreSQL em produção (Render)
- [ ] Modelos iniciais:
  - `User` (base, com tipo: doador/instituicao/admin)
  - `Institution` (dados específicos: CNPJ, endereço, documentação)
  - `Donation` (item, quantidade, status, data)
  - `Need` (descrição, quantidade alvo, urgência, progresso)

### 1.2 Autenticação Real
- [ ] Flask-Login + Flask-Bcrypt
- [ ] Login de doadores (email + senha)
- [ ] Login de instituições (CNPJ + email + senha)
- [ ] Proteção de rotas (`@login_required`)
- [ ] Logout

### 1.3 Templates com Jinja2
- [ ] Migrar HTMLs estáticos para Jinja2
- [ ] `base.html` com navbar e footer (DRY)
- [ ] `extends` e `blocks` para conteúdo
- [ ] Variáveis de ambiente com `python-dotenv`
- [ ] `config.py` para separar configurações

### 1.4 Estrutura do Projeto (após migração)

```
ConexaoSolidaria/
├── app.py
├── config.py
├── requirements.txt
├── .env
├── .gitignore
├── PLAN.md
├── run.py
├── models/
│   ├── __init__.py
│   ├── user.py
│   ├── institution.py
│   ├── donation.py
│   └── need.py
├── routes/
│   ├── __init__.py
│   ├── auth.py
│   ├── main.py
│   ├── dashboard.py
│   ├── contact.py
│   └── blog.py
├── templates/
│   ├── base.html
│   ├── index.html
│   ├── auth/
│   │   ├── login-doador.html
│   │   ├── login-instituicao.html
│   │   └── register.html
│   ├── dashboard/
│   │   ├── doador.html
│   │   └── instituicao.html
│   └── pages/
│       ├── sobre.html
│       ├── contato.html
│       ├── privacidade.html
│       └── blog.html
├── static/
│   ├── css/styles.css
│   ├── js/scripts.js
│   └── images/
└── migrations/
```

---

## Fase 2 — Funcionalidades Core

### 2.1 Cadastro e Onboarding
- [ ] Cadastro de doadores (nome, email, senha, telefone)
- [ ] Cadastro de instituições (CNPJ, razão social, endereço, docs)
- [ ] Validação de CNPJ
- [ ] Verificação de documentação (instituições pendentes até aprovação)

### 2.2 Gerenciamento de Necessidades
- [ ] Instituições criam necessidades (título, descrição, qtd, urgencia)
- [ ] Listar necessidades na home e no dashboard
- [ ] Barra de progresso dinâmica
- [ ] Filtrar por urgência/categoria

### 2.3 Sistema de Doações
- [ ] Doador seleciona necessidade e faz doação
- [ ] Status da doação: pendente → em trânsito → recebido
- [ ] Histórico de doações por usuário
- [ ] Notificação para instituição quando receber doação

### 2.4 Formulário de Contato
- [ ] Backend para salvar mensagens no banco
- [ ] Notificação por email (opcional)
- [ ] Página de FAQ dinâmica

---

## Fase 3 — UX e Conteúdo

### 3.1 Interface
- [ ] Navbar e footer componentizados (Jinja2 includes)
- [ ] Hamburger menu no mobile
- [ ] Animações e transições consistentes
- [ ] Estado de loading para formulários

### 3.2 Conteúdo Real
- [ ] Fotos reais da equipe (substituir `member1.png` repetido)
- [ ] Imagens do blog reais
- [ ] Textos institucionais definitivos
- [ ] Estatísticas reais (números de impacto)

### 3.3 Blog Dinâmico
- [ ] Modelo `Post` (autor, título, conteúdo, data, categoria, imagem)
- [ ] Admin cria/edita posts
- [ ] Páginas individuais de post
- [ ] Categorias e filtros

---

## Fase 4 — Polimento

### 4.1 SEO e Performance
- [ ] Meta tags e Open Graph em todas as páginas
- [ ] Sitemap.xml
- [ ] Minificação de CSS/JS
- [ ] Cache headers

### 4.2 Funcionalidades Avançadas
- [ ] Mapa de instituições próximas (Google Maps API ou Leaflet)
- [ ] Dashboard com gráficos (Chart.js)
- [ ] Relatório de impacto para doadores
- [ ] Área administrativa

### 4.3 Qualidade
- [ ] Testes unitários (pytest)
- [ ] Validação de formulários (front + back)
- [ ] Tratamento de erros (404, 500)
- [ ] Logging

---

## Tecnologias

| Finalidade | Tecnologia |
|------------|-----------|
| Backend | Flask + SQLAlchemy |
| Banco | PostgreSQL (prod), SQLite (dev) |
| Auth | Flask-Login + Flask-Bcrypt |
| Templates | Jinja2 |
| Frontend | CSS puro + JS vanilla |
| Deploy | Render (existente) |
| Migrações | Flask-Migrate / Alembic |
