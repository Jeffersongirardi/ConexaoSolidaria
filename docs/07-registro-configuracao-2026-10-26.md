# Registro de configuração e recuperação do ambiente — 26/10/2026

> **Registro histórico de 26/10.** As pendências abaixo refletem o conhecimento daquela sessão. Depois, o status recebido do Jefferson registrou validação do domínio/Resend/R2 em 27/10 e novas entregas de segurança/e-mails em 30–31/10. Em 01/11, essas entregas foram incorporadas à cópia local do fork; consulte [o status atual](00-status.md) e [o runbook](09-runbook-seguranca-andre.md) para deploy e validações pendentes. O fluxo informado por André passa a usar `andrebritovita/ConexaoSolidaria` como repositório de trabalho.

## Escopo e origem das informações

Este registro reúne as ações de 26/10/2026 e o contexto necessário das sessões anteriores. Ações nos painéis são registradas conforme relatos e capturas fornecidos pelo responsável; não representam auditoria independente das contas. Nenhum valor de senha, token ou chave foi incluído.

A cópia de trabalho escolhida é `Projetos/Staging/ConexaoSolidaria`, vinculada a `andrebritovita/ConexaoSolidaria`. A pasta `Projetos/ConexaoSolidaria` aponta para `Jeffersongirardi/ConexaoSolidaria` e permanece separada. Não foi confirmado merge do PR para o repositório do Jefferson nem migração completa para infraestrutura independente de produção.

## O que foi feito hoje

| Área | Ação e finalidade | Evidência / limite |
|---|---|---|
| Credenciais | Responsável confirmou geração de novas chaves nos serviços Vercel, Resend, Railway e Cloudflare, após o trabalho anterior de revogação. | Confirmação geral nesta sessão; escopo e tipo de cada token de plataforma não foram apresentados. Não confundir geração com validação da integração. |
| Railway | Inspecionado código que lê variáveis e autentica usuários. Confirmado que a chave criada em `Variables` tinha nome `API_KEY`. | Não existe consumo dessa variável genérica no backend inspecionado. Ela não é um token da API da Railway. |
| Vercel / domínio | Responsável informou adição do domínio próprio `conexaosolidarias.com.br` ao projeto. | Verificação final de DNS/HTTPS e do vínculo exato do domínio ainda deve ser conferida no painel. Um domínio próprio não muda automaticamente o ambiente de staging para produção. |
| Backend / domínio | Orientada atualização de `FRONTEND_URL` para `https://conexaosolidarias.com.br`, sem barra final, seguida de deploy. | O código usa a variável tanto para CORS quanto para links de e-mail. Aplicação final desse valor não foi demonstrada nesta etapa. |
| Vercel / deploy | Responsável confirmou redeploy na Vercel. | Testes finais do novo domínio e login após essa alteração ainda precisam ser registrados. |
| Resend | Orientada nova chave de envio e configuração do domínio próprio em Domains, com os registros DNS fornecidos pelo Resend. Responsável confirmou alterações no serviço e geração de nova chave. | Não foi apresentado status Verified, remetente final ou teste de entrega com as novas credenciais. |
| Cloudflare R2 | Orientada nova credencial S3 de conta, com Object Read & Write limitado ao bucket de staging. Responsável confirmou geração de novas chaves. | O backend usa Access Key ID e Secret Access Key, não o campo Token Value. Novo teste de upload/leitura ainda pendente. |
| Pausa do site | Explicado Pause Project / Resume Project na Vercel. | Pausa foi apenas consultada; não houve confirmação de execução. Não pausa backend/MySQL na Railway. |
| Diagnóstico 502 | Inspecionadas configurações das duas cópias e identificada divergência entre elas. | Sem logs do deploy com erro, causa não confirmada. Não houve correção de código aplicada nesta sessão. |

## Configuração esperada, sem segredos

### Railway — serviço do backend

| Variável | Finalidade / configuração |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod`; seleciona MySQL, Resend e R2 nesta cópia. |
| `RAILPACK_JDK_VERSION` | `21`, conforme execução validada anteriormente. |
| `DATABASE_URL` | Referência `${{MySQL.MYSQL_URL}}`, confirmada durante a rotação. |
| `JWT_SECRET` | Segredo aleatório atualizado após o incidente; somente no painel. |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Dados de criação inicial do administrador. Conta existente precisa de troca de senha no aplicativo; DataSeeder não redefine sua senha a cada deploy. |
| `FRONTEND_URL` | Endereço do frontend autorizado pelo CORS e usado nos links de e-mail; novo endereço pretendido: `https://conexaosolidarias.com.br`. |
| `RESEND_API_KEY` | Nova chave de envio emitida pelo Resend. |
| `MAIL_FROM` | Remetente pertencente ao domínio verificado no Resend. Valor final não confirmado. |
| `R2_ENDPOINT` | Endpoint S3 HTTPS da conta Cloudflare, sem caminho do bucket. |
| `R2_ACCESS_KEY`, `R2_SECRET_KEY` | Novo par de credenciais S3; nunca publicar no frontend ou no Git. |
| `R2_BUCKET` | Bucket privado `conexao-solidaria-staging`, salvo alteração posterior. |
| `PORT` | Porta fornecida/configurada na Railway; código usa `${PORT:8080}`. Porta de destino do domínio deve corresponder à porta de escuta. |

Root Directory: `/backend`. Healthcheck: `/actuator/health`. A API da aplicação usa JWT; `API_KEY`, `RAILWAY_API_TOKEN` e tokens da Vercel não são necessários para autenticar usuários do site. Tokens de plataforma servem para administrar a respectiva plataforma por API/automação.

### Vercel — frontend

- `NEXT_PUBLIC_API_URL`: `https://conexaosolidaria-production-14ee.up.railway.app/api/v1`.
- `NEXT_PUBLIC_API_ORIGIN`: `https://conexaosolidaria-production-14ee.up.railway.app`.
- Esses endereços permanecem iguais enquanto o backend não mudar de domínio.
- Nunca colocar segredos R2, Resend ou JWT em variáveis `NEXT_PUBLIC_*`.
- Após alterar variáveis do frontend, gerar novo deployment. Alterar apenas credenciais do backend requer aplicação na Railway, não redeploy da Vercel.
- O endereço anterior era `https://conexao-solidaria-staging.vercel.app`. Sua remoção ou redirecionamento não foi confirmado.

## Código já entregue antes de hoje

O histórico local confirma três commits, somando 26 arquivos alterados em relação a `ba410e7`:

- `761d54a` — suporte MySQL no Flyway (`backend/pom.xml`).
- `97e769d` — envio por Resend HTTPS no perfil prod, timeouts e tratamento de falhas sem registrar conteúdo sensível; teste `EmailServiceTest`; SMTP permanece para desenvolvimento e sai do healthcheck prod.
- `79d9ab4` — armazenamento privado no R2 e ajuste de hook na tela de ofertas.

Na integração R2, `StorageService` ganhou resolução de URL de leitura; `FileStorageService` e `WebConfig` ficaram restritos a perfis diferentes de prod. `R2Config` cria cliente S3 e assinador, valida endpoint e credenciais. `R2StorageService` valida pasta, extensão, MIME declarado e tamanho de até 5 MB, usa nomes aleatórios, salva caminhos estáveis e gera URLs com validade de 15 minutos. A validação de MIME declarado não equivale a inspeção profunda do conteúdo do arquivo.

`UploadController` redireciona fotos públicas para URLs assinadas; `SecurityConfig` bloqueia acesso direto às pastas privadas em prod. `PaymentApiController` verifica proprietário e estado antes de salvar comprovante. Controllers de pagamento, doação e administração usam os DTOs para resolver links; respostas selecionadas de pagamento/doação receberam no-store. `PaymentDTO` e `DonationDTO` aceitam o resolvedor sem gravar URLs temporárias no banco. Foram adicionados `R2StorageServiceTest` e `PrivateUploadAccessTest`.

`application-prod.properties`, `backend/PLAN.md`, os readmes, o status e os guias R2/Resend acompanharam as integrações. A página `frontend/src/app/ofertas/[id]/page.tsx` teve a ordem de um hook corrigida. O bucket continua privado; fotos públicas são disponibilizadas pela aplicação. Quem recebe uma URL assinada pode usá-la até expirar.

## Contexto anterior: deploy, colaboração e incidente

Antes da rotação, o responsável confirmou Railway Online, login administrador, recuperação de senha e entrega de e-mail, upload no R2 e persistência da imagem após redeploy com acesso em janela anônima. O teste manual de acesso aos comprovantes privados foi adiado.

Foi confirmado convite do colaborador no GitHub e abertura de PR em rascunho do fork para o repositório do Jefferson. Número/link e merge não foram confirmados. Fluxo combinado: branches de tarefa, revisão por PR, staging, testes e revisão antes de integrar à branch ligada à produção. Acesso ao código não exige acesso a todos os painéis.

Em 22/10, após o comunicado de segurança da Railway, foram confirmados:

1. Exclusão do token R2 antigo e da API Key antiga do Resend nos provedores.
2. Regeneração da senha MySQL pela interface do banco e manutenção da referência DATABASE_URL.
3. Geração de novo JWT_SECRET e aplicação por deployment.
4. Troca da senha efetiva do administrador pelo aplicativo, atualização de ADMIN_PASSWORD e novo deployment.
5. Login com a nova senha após deploy concluído.
6. Revisão dos e-mails enviados e dos objetos do bucket: responsável reconheceu os e-mails e encontrou apenas sua imagem de teste.
7. Eventos OAuth Resend/GitHub considerados compatíveis com o login informado. Gráficos R2 mostraram 11 operações de cada classe; atribuição por horário não foi concluída.

Essas verificações não demonstram ausência de leitura/download indevido. Revisão mais ampla dos logs, consumo e outros segredos de projetos/serviços não foi concluída na conversa. As credenciais substitutas haviam sido adiadas em 22/10; sua geração foi confirmada hoje. Account ID e endpoint S3 não são credenciais de autenticação e não precisam desaparecer após revogar o token.

## Diagnóstico e limites da validação

- O código já usa a porta PORT; o problema 502 não foi atribuído a uma causa comprovada. Conferir logs de startup, perfil ativo, conexão MySQL, porta de destino e estado do deployment.
- Variáveis R2 ausentes/vazias ou endpoint inválido podem impedir startup em prod. Credenciais preenchidas porém revogadas normalmente falham na operação S3; não há teste de autenticação R2 no healthcheck.
- RESEND_API_KEY vazia não impede startup: o serviço registra aviso ao tentar enviar. O healthcheck prod não consulta SMTP e não valida entrega de e-mails.
- Falha CORS após troca do domínio e credenciais de login incorretas são problemas distintos de 502.
- Na inspeção de 26/10, não foram aplicadas alterações no código. Teste da cópia antiga falhou antes de executar por bloqueio de escrita no cache Maven; execução de testes na cópia staging não foi autorizada. A contagem histórica de 42 testes verdes, TypeScript aprovado e lint sem erros (3 avisos) não representa nova execução hoje.

## Próximos passos

- [ ] Confirmar escopo/tipo dos tokens novos de Vercel e Railway e que nenhum segredo foi colocado no frontend.
- [ ] Confirmar DNS/HTTPS do novo domínio, FRONTEND_URL aplicado e login a partir dele.
- [ ] Confirmar domínio de envio Verified no Resend, MAIL_FROM e aplicação da nova chave; testar entrega a destinatário autorizado fora da conta de teste.
- [ ] Confirmar novo par R2 aplicado; testar upload, leitura e persistência após redeploy novamente.
- [ ] Testar comprovantes privados com usuário autorizado, outro usuário e sem login.
- [ ] Revisar logs/consumo e inventário de segredos restantes, sem declarar encerrada a investigação do incidente.
- [ ] Executar testes atualizados antes de alterações de código/merge; tratar a auditoria de dependências registrada no status.
- [ ] Revisar PR com o colaborador e confirmar configuração separada de produção antes do cutover.

Não houve commit, push, mudança DNS ou operação de painel executada pelo agente nesta atualização documental.
