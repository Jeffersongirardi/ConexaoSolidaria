# Guia 2 — Imagens no Cloudflare R2

> Para quem nunca usou a Cloudflare. Tempo estimado: ~40 min.
> **Por quê?** Hoje as fotos ficam no disco do servidor (`FileStorageService` → `/uploads/**`). Na Railway o disco é **efêmero**: a cada redeploy, as fotos somem. O R2 é um "disco na nuvem" barato que sobrevive a restarts.

## 1. Conceito em 1 minuto (leia se é sua 1ª vez)

- **Bucket** = uma pasta gigante na nuvem (vamos criar uma chamada `conexoes-solidarias`).
- O R2 fala o protocolo **S3** (o mesmo da Amazon). Nosso backend usa o SDK da AWS apontando para o endereço da Cloudflare — o frontend **não muda nada**, porque `fileUrl()` (`frontend/src/lib/api.ts`) já aceita URLs absolutas `https://...`.
- Em dev local continua tudo igual (disco `./uploads/`). O R2 só entra no profile `prod`.

## 2. Criar a conta e o bucket

1. Crie a conta em [cloudflare.com](https://cloudflare.com) (pede cartão, mas o tier gratuito cobre nosso uso com folga).
2. Menu lateral → **R2 Object Storage** → **Create bucket**:
   - Nome: `conexoes-solidarias`
   - Região: automática (deixe o padrão).
3. Entre no bucket → **Settings → Public access → Allow Access**.
   Anote a **URL pública** que aparece (formato `https://pub-<id>.r2.dev/...`). É por ela que o navegador vai carregar as fotos.
   > Mais tarde podemos trocar por `cdn.conexaosolidarias.com.br` (Guia 4). Não é obrigatório no dia 1.

## 3. Criar o token de API (o backend usa isso para enviar fotos)

1. **R2 → Manage R2 API Tokens → Create API Token**:
   - Nome: `backend-railway`
   - Permissão: **Object Read & Write**
   - Escopo: só o bucket `conexoes-solidarias` (princípio do menor privilégio).
2. A tela mostra **uma única vez**. Anote os 3 valores:
   - `Access Key ID`
   - `Secret Access Key`
   - **Endpoint S3**: `https://<id>.r2.cloudflarestorage.com`

## 4. Entregar ao backend

Na Railway (serviço `api` → **Variables**), adicione quando a Fase 3 do código estiver pronta:

| Variável | Exemplo |
|---|---|
| `R2_ENDPOINT` | `https://<id>.r2.cloudflarestorage.com` |
| `R2_ACCESS_KEY` | (Access Key ID) |
| `R2_SECRET_KEY` | (Secret Access Key — **nunca** em chat/print) |
| `R2_BUCKET` | `conexoes-solidarias` |
| `R2_PUBLIC_URL` | `https://pub-<id>.r2.dev` (sem `/` no fim) |

## 5. Como testar

1. Com o backend em `prod`, faça upload de uma foto de campanha (ou avatar) pelo frontend.
2. O retorno da API deve ser uma URL `https://...` (não mais `/uploads/...`).
3. Abra a URL em aba anônima: a imagem carrega = R2 ok.
4. Regras mantidas do `FileStorageService`: só `png/jpg/jpeg/gif/webp`, máx. 5MB, nome aleatório UUID (sem nome original — anti-enumeração).

## 6. Custos e limites (para não ter surpresa)

- Tier gratuito do R2: 10 GB/mês de armazenamento + milhões de leituras. Nosso volume (fotos de campanha/ofertas/avatares ≤5MB) cabe com folga.
- Sem taxa de saída (egress) — vantagem do R2 sobre a AWS S3.
