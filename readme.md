# Conexões Solidárias

Plataforma que conecta doadores a instituições sociais em Curitiba, facilitando doações de forma eficiente e transparente.

## Tecnologias

- **Backend:** Flask + SQLAlchemy (em desenvolvimento)
- **Banco:** PostgreSQL (produção) / SQLite (desenvolvimento)
- **Autenticação:** Flask-Login + Flask-Bcrypt (em desenvolvimento)
- **Frontend:** HTML, CSS, JavaScript vanilla
- **Deploy:** Render

## Estrutura

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

## Setup local

```bash
python -m venv .venv
.venv\Scripts\activate   # Windows
pip install -r requirements.txt
python app.py
```

## Status

Projeto em desenvolvimento — veja [PLAN.md](PLAN.md) para o cronograma completo.
