# Guia 4 — Domínio `conexaosolidarias.com.br` e DNS

> Para quem nunca mexeu em DNS. Tempo estimado: ~30 min (+ propagação de até algumas horas).
> **Mapa final:** `conexaosolidarias.com.br` → frontend (Vercel) · `api.conexaosolidarias.com.br` → backend (Railway) · registros do Resend → e-mails.

## 1. Onde mexer

No painel de onde o domínio foi registrado (registro.br ou o provedor usado). Procure **"DNS" / "Zona DNS" / "Gerenciar DNS"**.

## 2. Passo 1 — Frontend na Vercel (faça primeiro)

1. Na Vercel (projeto do frontend) → **Settings → Domains → Add**: `conexaosolidarias.com.br` e também `www.conexaosolidarias.com.br`.
2. A Vercel mostra **exatamente** o que cadastrar (geralmente 1 registro `A` com IP próprio + 1 `CNAME` para `www`). Copie os valores dela — **a Vercel é a fonte da verdade aqui**, não este guia.
3. Cadastre no DNS e aguarde a Vercel marcar **Valid / Configured**.

## 3. Passo 2 — API na Railway

1. Na Railway (serviço `api`) → **Settings → Networking → Custom Domain** → digite `api.conexaosolidarias.com.br`.
2. A Railway mostra o destino (algo como `xxxx.up.railway.app`). Cadastre um **CNAME**:
   - Nome/host: `api` · Valor: o destino mostrado pela Railway.
3. Teste: `nslookup api.conexaosolidarias.com.br` deve responder; `https://api.conexaosolidarias.com.br/actuator/health` → `{"status":"UP"}`. Certificado HTTPS é automático.

## 4. Passo 3 — E-mails (Resend)

Cadastre os 3 registros que o Resend exibiu ([Guia 3](03-resend-emails.md)): SPF, DKIM e DMARC. São registros `TXT` (o DKIM às vezes é `CNAME`). Volte ao Resend e aguarde **Verified**.

## 5. Tabela-resumo (como deve ficar no final)

| Host | Tipo | Aponta para | Serve para |
|---|---|---|---|
| `@` (apex) | A (valor da Vercel) | Vercel | Site |
| `www` | CNAME (valor da Vercel) | Vercel | Site |
| `api` | CNAME `*.up.railway.app` | Railway | Backend |
| (3 do Resend) | TXT/CNAME | valores do Resend | E-mails |

## 6. Armadilhas comuns

- **Propagação demora**: DNS pode levar de minutos a horas. Teste em aba anônima e com `nslookup`; não saia mudando tudo de novo a cada 5 minutos.
- **`FRONTEND_URL` tem que ser o domínio final com `https://` e sem `/` no fim** (`https://conexaosolidarias.com.br`). Sem isso o CORS (`SecurityConfig` lê `app.cors.allowed-origins`) bloqueia o frontend e os links dos e-mails saem errados.
- Só existe **um** lugar de cada registro: se der conflito, apague duplicatas antigas.
