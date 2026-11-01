# Ajustes de UX e autenticação — 01/11/2026

Implementados na cópia Staging; sem commit, push ou deploy nesta etapa.

| Ajuste | Comportamento |
|---|---|
| Ofertas | Oferta disponível no modelo, mas não aprovada, mostra Aguardando aprovação ou Não aprovada. Disponível aparece após aprovação. |
| Notificações | Sino e lista consultam a API a cada 5 minutos com a página visível; voltar à aba e marcar como lida também atualizam. Não é entrega instantânea por WebSocket. |
| Senhas | Campos que usam TextInput de senha oferecem Mostrar/Ocultar, sem alterar o valor digitado. |
| Inatividade | Após 29 minutos aparece aviso; aos 30 ocorre logout. Interações de teclado, toque, clique e rolagem renovam o prazo. Consultas automáticas não renovam. Abas do mesmo navegador compartilham atividade. |
| Login | Terceira falha informa duas tentativas restantes; quarta informa uma; quinta retorna 429 e bloqueia por 15 minutos contados dessa falha. Novas tentativas durante o bloqueio não prolongam o prazo. |
| Horários | API serializa LocalDateTime com offset do fuso do servidor. Navegador converte para seu fuso; datas de calendário permanecem no dia indicado. |
| Métricas | Total agregado passa a se chamar Registros (itens, Pix e ofertas); pendências são identificadas como Itens / Pix pendentes. Não alteramos a fórmula do total. |

## Implementação

- `ApiDateTimeConfig`: serialização central de datas com horário; sem migration ou alteração de valores persistidos.
- `LoginAttemptService`, `LoginCredentialsException`, controller e handler: contagem, prazo e mensagens específicas de autenticação.
- `PasswordInput` e `ui`: controle reutilizável de visibilidade e formatação de datas.
- `polling`, `Header` e página de notificações: atualização periódica, sem sobreposição de consultas e com limpeza de timers.
- `auth` e `api`: controle de inatividade e proteção contra uma resposta de refresh restaurar tokens após logout.
- Painel do doador e login: apresentação de moderação, métricas e mensagens retornadas pela API.

## Validação executada

- Maven offline com Java 21: 93 testes, sem falhas ou erros.
- TypeScript (`tsc --noEmit --incremental false`) e lint: aprovados.
- Testes específicos cobrem mensagens da terceira à quinta falha, expiração do bloqueio e serialização de datas com offset.
- Preservados os testes de Pix/e-mails e a documentação que já estavam modificados antes desta tarefa.

## Validação manual pendente após deploy

1. Criar oferta: conferir somente Aguardando aprovação; aprovar no admin e recarregar o painel para conferir Disponível.
2. Com admin aberto, gerar notificação em outra conta: conferir sino e lista em até 5 minutos; marcar como lida e conferir contador.
3. Mostrar/ocultar senha em login, cadastro e redefinição.
4. Ficar 29 minutos sem interação: conferir aviso. Esperar até 30: conferir logout. Repetir usando Continuar conectado e duas abas. Conferir também ao retornar a uma aba suspensa.
5. Usar conta de teste para terceira, quarta e quinta senha incorreta; conferir mensagens e liberação após 15 minutos.
6. Criar intenção Pix e comparar horário local. Conferir que prazos de ofertas não mudaram de dia.

## Limites

Logout por inatividade é um controle do navegador, não uma política de inatividade persistida no backend; indisponibilidade de rede pode impedir a revogação remota, embora os tokens locais sejam removidos. O bloqueio de tentativas continua em memória por instância. A interpretação dos horários antigos pressupõe que foram gravados no mesmo fuso padrão do servidor; mudanças históricas de fuso exigem revisão separada. Não subtrair três horas manualmente no banco.
