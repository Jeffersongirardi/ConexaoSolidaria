# Conexões Solidárias

**Conexões Solidárias: Um Aplicativo para Doação de Recursos a quem necessita.**

Plataforma web que conecta doadores a instituições sociais, facilitando a doação de alimentos, roupas e outros recursos para comunidades carentes de forma eficiente, transparente e acessível.

## Objetivo

Facilitar a doação de alimentos e roupas para comunidades carentes de forma simplificada e acessível a todas as partes interessadas, aumentando a eficiência na distribuição de recursos para quem realmente precisa.

## ODS e Impacto Social

O projeto está alinhado aos seguintes Objetivos de Desenvolvimento Sustentável da ONU:

- **ODS 1** — Erradicação da pobreza
- **ODS 2** — Fome zero e agricultura sustentável
- **ODS 10** — Redução das desigualdades
- **ODS 17** — Parcerias e meios de implementação

## Setor de Aplicação

O projeto será aplicado inicialmente na cidade de Curitiba, Paraná, como projeto piloto junto a instituições interessadas, podendo posteriormente capilarizar para regiões metropolitanas.

## Funcionalidades Planejadas

| ID | Funcionalidade | Descrição |
|----|---------------|-----------|
| RF01 | Cadastro e Validação de Instituições | Instituições se cadastram com CNPJ e documentação; perfil só fica público após aprovação administrativa |
| RF02 | Publicação de Campanhas | Instituições criam campanhas com título, descrição, itens necessários, metas e período de vigência |
| RF03 | Intenção de Doação | Doador seleciona itens e registra intenção; gera notificação para instituição e "doações pendentes" no perfil |
| RF04 | Confirmação de Recebimento | Instituição marca doação como "recebida"; atualiza progresso da campanha e notifica o doador |
| RF05 | Painel de Gerenciamento | Dashboard com progresso de campanhas ativas, gerenciamento de intenções e histórico de doações |

## Tecnologias

- **Backend:** Flask + SQLAlchemy
- **Banco:** PostgreSQL (produção) / SQLite (desenvolvimento)
- **Autenticação:** Flask-Login + Flask-Bcrypt
- **Frontend:** HTML5, CSS3, JavaScript vanilla
- **Deploy:** Render

## Estrutura do Projeto

```
ConexaoSolidaria/
├── app.py              # Aplicação Flask
├── requirements.txt    # Dependências
├── PLAN.md             # Plano de desenvolvimento por fases
├── index.html          # Página inicial
├── css/styles.css      # Estilos
├── js/scripts.js       # Scripts
├── pages/              # Páginas HTML
│   ├── login-doadores.html
│   ├── login-instituicoes.html
│   ├── dashboard-doador.html
│   ├── dashboard-instituicao.html
│   ├── blog.html
│   ├── contato.html
│   ├── sobre.html
│   └── privacidade.html
└── images/
    └── team/
```

## Setup Local

```bash
python -m venv .venv
.venv\Scripts\activate   # Windows
pip install -r requirements.txt
python app.py
```

## Status

Projeto em desenvolvimento. Acesse o [PLAN.md](PLAN.md) para o cronograma completo das fases de implementação.
