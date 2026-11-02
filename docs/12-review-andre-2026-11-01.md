# Review — commits do André (2026-11-01)

> **Escopo:** revisão de `d359565`, `4fa728c` e `9d81c98` (fork `andrebritovita`),
> comparados ao `master` oficial `01500a1`. Somente leitura de código; os testes dele
> (93/93, Java 21) ainda não foram reproduzidos no ambiente do dono (Java 17).
>
> **Resumo:** trabalho bom e consistente. Integração e deploy **pendentes**; riscos
> principais são fuso horário e ausência das mudanças em produção.

## 1. O que foi entregue

| Commit | Conteúdo |
|---|---|
| `d359565` | Docs: histórico de configuração, status, `07`, `10`, novo `11-ajustes-ux` |
| `4fa728c` | Ofertas (rótulos de moderação), notificações, autenticação (mensagens de lockout, inatividade/logout 30 min, proteção de refresh), horários (offset), `PasswordInput`, `polling` |
| `9d81c98` | Polling de notificações a cada 5 min |

Diff real: **23 arquivos, +715/−149**. Merge-base = `01500a1`, ou seja, já inclui todo
o `master` oficial (P0, segurança, e-mails e UI).

## 2. Pontos fortes

- `LoginAttemptService` reescrito: `Clock` injetável, `record` imutável e
  `ConcurrentHashMap.compute` atômico; bloqueio não prolonga o prazo.
- Mensagens progressivas de tentativas (3ª/4ª/5ª) e liberação após 15 min.
- `api.ts`: não tenta refresh em requisição anônima (`token &&`) e evita corrida do
  logout (`getRefreshToken() !== refresh`).
- Inatividade de 30 min com aviso em 29, compartilhada entre abas via `localStorage`.
- `pollVisible`: guarda de `busy`, respeita visibilidade e reage a `focus`/evento custom.
- `formatarData` trata data-only (`T00:00:00`), corrigindo deslocamento de prazos.
- Ofertas: “Aguardando aprovação” × “Não aprovada” agora usam `aprovado`/`motivoRecusa`.
- Testes novos: `ApiDateTimeConfigTest`, `LoginAttemptServiceTest`, casos extras em
  `AuthApiTest`, `PaymentFlowApiTest` e `EmailServiceTest`.

## 3. Riscos e ajustes recomendados

| # | Prioridade | Item |
|---|---|---|
| 1 | **Alta** | `ApiDateTimeConfig` usa `ZoneId.systemDefault()`. Em container Railway o padrão costuma ser UTC; se os `LocalDateTime` foram gravados em horário de Brasília, o front exibirá **+3h**. Fixar `America/Sao_Paulo` (config) ou migrar para `Instant`, validando dados antigos. |
| 2 | **Alta** | Produção **não tem** estes commits: o HTML do login não contém “Mostrar/Ocultar senha”. Falta PR + deploy. |
| 3 | Média | 5ª falha lança 429 sem registrar `login_bloqueado` em `auth_events` (só as tentativas seguintes registram). |
| 4 | Média | Aviso de inatividade `fixed bottom-4` pode sobrepor a barra mobile; revisar posição/`z-index` em 360px. |
| 5 | Baixa | `docs/11` diz “sem commit, push ou deploy”, mas os commits existem no fork. Atualizar a linha. |
| 6 | Baixa | Confirmar suíte em **Java 17** (relato foi Java 21) e `tsc`/lint. |

## 4. Integração

- Não há PR novo no repo oficial.
- Como o merge-base é `01500a1`, um PR do fork exibirá **apenas os 23 arquivos**
  (os commits antigos do PR #1 ficam fora por causa do squash) — integração viável.
- Sugestão: PR de `andrebritovita:master` → `master`, Squash and merge após os ajustes 1–3.

## 5. Checklist manual (do `docs/11`, a validar após deploy)

1. Criar oferta → só “Aguardando aprovação”; aprovar no admin → “Disponível”.
2. Notificação gerada em outra conta → sino/lista em até 5 min; marcar como lida.
3. Mostrar/ocultar senha em login, cadastro e redefinição.
4. Inatividade: aviso em 29 min; logout aos 30; “Continuar conectado”; duas abas.
5. 3ª/4ª/5ª senha errada → mensagens e 429; liberar após 15 min.
6. Pix novo → horário local coerente; prazos de oferta no mesmo dia.
