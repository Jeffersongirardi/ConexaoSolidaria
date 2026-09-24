# Conexões Solidárias

Conecte-se a quem transforma doações em impacto real — campanhas de instituições validadas, com acompanhamento até a entrega.

Nascido em Curitiba e aberto a instituições de todo o Brasil.

## Arquitetura

Monorepo com frontend e backend desacoplados, integrados via API REST/JSON:

```
projetoex/
├── frontend/   # Next.js + React + TypeScript + Tailwind (PWA)
├── backend/    # Spring Boot 3 + Java 17 — API REST
```

## Execução local

**Backend** (http://localhost:8080, perfil `dev`/H2):

```bash
cd backend
mvn spring-boot:run
```

Documentação da API: `http://localhost:8080/swagger-ui/index.html`

**Frontend** (http://localhost:3000):

```bash
cd frontend
npm install
npm run dev
```

Configure `frontend/.env.local` (não versionado):

```
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXT_PUBLIC_API_ORIGIN=http://localhost:8080
```

Instale como app: no navegador compatível, use “Instalar aplicativo” / “Adicionar à tela inicial” (PWA com suporte offline limitado).
