# Guia 0 — Contas, acessos e quem é dono do quê

> Leia antes dos Guias 1–6. O projeto é de um dono só, com 1 colaborador no git.
> **Decisão atualizada (2026-10-22):** produção roda **na conta do parceiro**, que já
> provou o ambiente em staging (upload R2, e-mails Resend, privacidade de comprovante,
> login admin — validados pelo dono). O dono mantém **GitHub + domínio/DNS**; os guias
> de deploy (01, 02, 03, 05) continuam válidos, mas são executados pelo parceiro.

## Matriz de contas (decidido — revisto em 2026-10-22)

| Serviço | Quem mantém | Observação |
|---|---|---|
| GitHub | **Dono** (repo oficial) + parceiro como **colaborador (Write)** | Única fonte de código; sem Write o parceiro nem faz push |
| Railway | **Parceiro** — produção + testes (Hobby $5/mês) | Backend + MySQL + volume; custo e fatura na conta dele |
| Vercel | **Parceiro** — produção + previews ($0) | Frontend; hoje `conexao-solidaria-staging.vercel.app` |
| Cloudflare R2 / Resend | **Parceiro** (produção) | Bucket privado de imagens + envio de e-mails |
| Domínio `conexaosolidarias.com.br` | **Dono** — único que mexe no DNS | Cutover (Guia 4): apex → Vercel do parceiro, `api` → Railway do parceiro |

## Riscos e responsabilidades do modelo "produção do parceiro"

- **Custo**: fica na conta do parceiro (Railway Hobby $5/mês é o piso para API + MySQL 24/7). Sem cobrança do dono.
- **Dependência**: se o parceiro sair do projeto/graduar-se, a produção para e as credenciais de deploy estão nas contas dele. Mitigação combinada: repositório e DNS ficam com o dono — migrar para uma nova conta é re-deploy (sem perda de código), não é reescrita.
- **Assentos**: planos Hobby (Railway) e pessoais (Vercel) têm 1 assento — o dono **não entra** na console do parceiro. Logs/variáveis de prod via prints ou acesso momentâneo concedido pelo parceiro.
- Segredos de produção (`JWT_SECRET`, chaves R2/Resend, `ADMIN_PASSWORD`) vivem **nas variables de cada ambiente** — nunca em arquivo versionado.

## Parceiro: ligar o git dele ao deploy próprio

Pré-requisito: ser colaborador **Write** no GitHub (com Read o webhook pode falhar — e sem Write ele não contribui mesmo).

1. Dono: GitHub → repo → **Settings → Collaborators → Add people** → e-mail/usuário do parceiro → papel **Write**.
2. Parceiro: login com GitHub na Railway/Vercel dele → **New Project → Deploy from GitHub repo** → `ConexaoSolidaria` aparece na lista (repos próprios + onde é colaborador).
3. Deploy e redeploy automático funcionam; o consumo cai na conta **dele**, sem tocar na produção.

## Regras que evitam dor de cabeça

- Planos Hobby (Railway) e pessoais (Vercel) têm **1 assento**: o parceiro **nunca** entra na produção do dono. Logs/variáveis de prod passam pelo dono (ou por prints). Gatilho de upgrade para o Pro: parceiro precisar de acesso à produção OU fatura Hobby encostar em $20.
- Conta GitHub nova pode cair em verificação automática na Railway (trial com rede restrita) — normal, resolve com uso ou Hobby.
- Segredos de produção (`JWT_SECRET`, chaves R2/Resend) **nunca** vão para o staging do parceiro — cada ambiente tem os seus.
