# Conexões Solidárias

**Conexões Solidárias** é uma plataforma web que conecta doadores a instituições sociais, facilitando a doação de alimentos, roupas, recursos financeiros e outros itens para comunidades carentes de forma eficiente, transparente e acessível.

## Funcionalidades

| Funcionalidade | Descrição |
|----------------|-----------|
| Cadastro de Doadores | CPF, endereço, WhatsApp, data de nascimento |
| Cadastro de Instituições | CNPJ, razão social, chave PIX, validação administrativa |
| Necessidades com Fotos | Múltiplas imagens por necessidade, galeria, zoom modal |
| Doação de Itens Físicos | Doador registra intenção, instituição confirma recebimento |
| Doação Financeira | PIX (QR Code), cartão de crédito, transferência bancária — **simulados** |
| Dashboard do Doador | Histórico de doações físicas e financeiras, impacto |
| Dashboard da Instituição | Gerenciar necessidades, confirmar doações, atualizar destino |
| Atualização de Destino | Instituição posta fotos e mensagens do destino das doações |
| Painel Administrativo | CRUD de instituições, usuários, blog, mensagens de contato |
| Blog Dinâmico | Posts com categorias, páginas individuais |
| Notificações | Notificações in-app, badge de não lidas |
| Autenticação | Login separado para doador, instituição e admin |

## Tecnologias

- **Backend:** Flask + SQLAlchemy
- **Banco:** PostgreSQL (produção) / SQLite (desenvolvimento)
- **Autenticação:** Flask-Login + Werkzeug Security
- **Frontend:** HTML5, CSS3, JavaScript vanilla
- **Máscaras:** CPF, CNPJ, CEP, telefone, cartão (JS vanilla)
- **QR Code:** `qrcode[pil]` (opcional, fallback se não instalado)
- **Uploads:** Sistema de upload com hash SHA1, validação de tipo/tamanho
- **Deploy:** Render (gunicorn)

## Estrutura do Projeto

```
ConexaoSolidaria/
├── app.py                  # Aplicação Flask (factory + migração + seed)
├── config.py               # Configurações (env, banco, upload)
├── requirements.txt        # Dependências
├── .env                    # Variáveis de ambiente
├── PLAN.md                 # Plano de desenvolvimento
├── models/
│   ├── __init__.py         # db, login_manager, imports
│   ├── user.py             # User (cpf, endereço, whatsapp, avatar)
│   ├── institution.py      # InstitutionProfile (pix, nome fantasia)
│   ├── need.py             # Need (aceita_financeiro, imagens)
│   ├── need_image.py       # NeedImage (fotos por necessidade)
│   ├── donation.py         # Donation (tipo físico/financeiro, updates)
│   ├── payment.py          # Payment (gateway fake)
│   ├── donation_update.py  # DonationUpdate (destino das doações)
│   ├── notification.py     # Notification
│   ├── contact_message.py  # ContactMessage
│   └── blog_post.py        # BlogPost
├── routes/
│   ├── auth.py             # Login/cadastro doador, instituição, admin
│   ├── dashboard.py        # Painéis, doações, pagamentos, updates
│   ├── admin.py            # Admin: CRUD instituições, usuários, blog
│   ├── main.py             # Páginas públicas (home, sobre, contato)
│   ├── necessidades.py     # Listagem e detalhe de necessidades
│   ├── perfil.py           # Perfil + avatar + alterar senha
│   ├── notificacoes.py     # Notificações in-app
│   ├── blog.py             # Blog público
│   ├── pagamento.py        # Gateway fake (PIX, cartão, transferência)
│   └── upload.py           # Handler centralizado de upload
├── templates/
│   ├── base.html           # Template base (navbar, footer, notificações)
│   ├── index.html          # Home
│   ├── sobre.html
│   ├── contato.html
│   ├── privacidade.html
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
│   └── admin/
│       ├── dashboard.html
│       ├── instituicoes.html
│       ├── usuarios.html
│       ├── mensagens.html
│       ├── blog.html
│       └── blog-form.html
├── static/
│   ├── css/styles.css
│   ├── js/scripts.js
│   └── js/masks.js         # Máscaras de input
└── uploads/                # Diretório criado automaticamente
    ├── needs/              # Fotos das necessidades
    ├── updates/            # Fotos das atualizações
    ├── avatars/            # Fotos de perfil
    └── comprovantes/       # Comprovantes de pagamento
```

## Setup Local

```bash
# Clone
git clone https://github.com/Jeffersongirardi/ConexaoSolidaria.git
cd ConexaoSolidaria

# Ambiente virtual
python -m venv .venv
.venv\Scripts\activate   # Windows
source .venv/bin/activate # Linux/Mac

# Dependências
pip install -r requirements.txt
pip install qrcode[pil] Pillow  # opcional — QR Code PIX

# Executar
python app.py
```

Acesse `http://127.0.0.1:5000`.

### Credenciais padrão (admin)

- **E-mail:** `admin@conexoessolidarias.org`
- **Senha:** `admin123`

> O admin é criado automaticamente na primeira execução.

## Deploy

Hospedado no **Render** (free tier). O banco PostgreSQL gratuito expira em 30 dias — para uso contínuo além de testes, migrar para Supabase (free tier sem expiração) ou plano pago.

```bash
gunicorn app:app
```

## Pagamentos — Nota Importante

O sistema de **pagamentos é simulado** (não integra com gateways reais):

- **PIX:** Exibe a chave PIX da instituição e gera QR Code (se `qrcode` instalado). O clique em "Confirmar Pagamento Simulado" marca como pago.
- **Cartão:** Formulário de cartão (dados não são salvos). Clique em "Pagar" marca como pago.
- **Transferência:** Exibe dados bancários fictícios. Upload de comprovante é aceito e o status muda para confirmado.

## ODS e Impacto Social

- **ODS 1** — Erradicação da pobreza
- **ODS 2** — Fome zero e agricultura sustentável
- **ODS 10** — Redução das desigualdades
- **ODS 17** — Parcerias e meios de implementação

## Licença

Projeto acadêmico — escopo de teste e desenvolvimento.
