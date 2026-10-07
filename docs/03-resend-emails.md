# Guia 3 — E-mails com Resend

> Para quem nunca usou o Resend. Tempo estimado: ~30 min (+ propagação do DNS).
> **Por quê?** Hoje o `EmailService` tenta SMTP (Gmail) e, sem credencial, só **loga** o e-mail no console. Em produção precisamos de entrega real: boas-vindas, confirmação de doação e **redefinição de senha** (sem e-mail, o "esqueci senha" não funciona).

## 1. Criar a conta e a chave

1. Crie a conta em [resend.com](https://resend.com) (login com GitHub funciona).
2. **API Keys → Create API Key**:
   - Nome: `conexoes-railway-prod`
   - Permission: **Sending access** (só envio, nada de gerenciar domínios).
3. Copie a chave (`re_...`). Na Railway (serviço `api` → **Variables**): `RESEND_API_KEY=re_...`.
   > Sem essa variável, o comportamento atual se mantém (só log, sem quebrar nada). Dá para subir a infra antes do DNS.

## 2. Verificar o domínio (obrigatório para enviar como `@conexaosolidarias.com.br`)

1. Resend → **Domains → Add Domain** → digite `conexaosolidarias.com.br`.
2. O Resend mostra 3 registros DNS: **SPF** (TXT), **DKIM** (TXT/CNAME) e **DMARC** (TXT). Cadastre-os onde o domínio está registrado (detalhes no [Guia 4](04-dominio-dns.md)).
3. Aguarde o status **Verified** (minutos a poucas horas). Enquanto estiver `Pending`, o Resend só envia para o seu próprio e-mail de cadastro — ótimo para testar.
4. Remetente padrão que o código usará: `contato@conexaosolidarias.com.br` (hoje é `noreply@conexoessolidarias.org` em `EmailService.java`).

## 3. O que o código faz (para entender, não precisa mexer)

- A interface `EmailService` continua igual; só a implementação interna troca SMTP por chamada HTTP à API do Resend.
- Mesmos 3 usos: `notificarNovaDoacao`, `notificarDoacaoConfirmada`, `enviarLinkRedefinicaoSenha` (link com `FRONTEND_URL` — por isso essa variável precisa ser o domínio final).
- Em `dev` local, sem chave, continua o fallback de log. Nada muda no seu dia a dia.

## 4. Como testar de verdade

1. Com domínio verificado e `RESEND_API_KEY` na Railway, use **"Esqueci senha"** no frontend de produção com seu e-mail pessoal.
2. Chegou com remetente `contato@conexaosolidarias.com.br` e link `https://conexaosolidarias.com.br/redefinir-senha?...` = ok.
3. Não chegou? Resend → **Logs** mostra cada envio e o motivo (bounce, domínio não verificado, etc.).

## 5. Custos

- Tier gratuito: 100 e-mails/dia, 3.000/mês. Nosso volume transacional (cadastro/confirmação/reset) cabe por muito tempo.
