# Conexões Solidárias

Plataforma digital que conecta doadores a instituições de caridade em Curitiba/PR
(Atividade Extensionista III — Engenharia de Software).

## Arquitetura

Monorepo com frontend e backend desacoplados, integrados via API REST/JSON:

```
projetoex/
├── frontend/   # Next.js + React + TypeScript + Tailwind (PWA)
├── backend/    # Spring Boot 3 + Java 17 — API REST (em migração a partir de SSR Thymeleaf)
└── docs/       # Guias do piloto e roteiros de teste (previsto)
```

## Execução local

**Backend** (http://localhost:8080, perfil `dev`/H2):

```bash
cd backend
mvn spring-boot:run
```

**Frontend** (http://localhost:3000):

```bash
cd frontend
npm install
npm run dev
```

Configure `frontend/.env.local` (não versionado):

```
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

## Documentação

- `backend/readme.md` — detalhes do backend (endpoints, perfis, banco).
- `backend/PLAN.md` — planejamento e histórico.
