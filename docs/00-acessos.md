# Guia 0 — Contas, acessos e quem é dono do quê

> Leia antes dos Guias 1–6. O projeto é de um dono só, com 1 colaborador no git.
> Produção fica 100% na conta do dono; o parceiro trabalha com contas próprias.

## Matriz de contas (decidido)

| Serviço | Produção (dono) | Parceiro (staging/testes) |
|---|---|---|
| GitHub | Dono do repo; adiciona o parceiro como **colaborador (Write)** | Push/PR; sem Write ele nem faz push |
| Railway | Workspace atual + **projeto novo** `conexoes-solidarias` (~$21–23/mês projetados) | Conta própria: trial $5/30d → Hobby $5/mês se manter ligado; **dica: pausar o serviço quando não estiver testando** |
| Vercel | Projeto de produção (`conexaosolidarias.com.br`) | Conta própria para previews/testes ($0) |
| Cloudflare / Resend | Conta do dono (produção) | Contas próprias em modo teste/sandbox ($0) |
| Domínio `conexaosolidarias.com.br` | Só o dono mexe no DNS | Não tem acesso — chama o dono 1x no cutover ([Guia 4](04-dominio-dns.md)) |

## Parceiro: ligar o git dele ao deploy próprio

Pré-requisito: ser colaborador **Write** no GitHub (com Read o webhook pode falhar — e sem Write ele não contribui mesmo).

1. Dono: GitHub → repo → **Settings → Collaborators → Add people** → e-mail/usuário do parceiro → papel **Write**.
2. Parceiro: login com GitHub na Railway/Vercel dele → **New Project → Deploy from GitHub repo** → `ConexaoSolidaria` aparece na lista (repos próprios + onde é colaborador).
3. Deploy e redeploy automático funcionam; o consumo cai na conta **dele**, sem tocar na produção.

## Regras que evitam dor de cabeça

- Planos Hobby (Railway) e pessoais (Vercel) têm **1 assento**: o parceiro **nunca** entra na produção do dono. Logs/variáveis de prod passam pelo dono (ou por prints). Gatilho de upgrade para o Pro: parceiro precisar de acesso à produção OU fatura Hobby encostar em $20.
- Conta GitHub nova pode cair em verificação automática na Railway (trial com rede restrita) — normal, resolve com uso ou Hobby.
- Segredos de produção (`JWT_SECRET`, chaves R2/Resend) **nunca** vão para o staging do parceiro — cada ambiente tem os seus.
