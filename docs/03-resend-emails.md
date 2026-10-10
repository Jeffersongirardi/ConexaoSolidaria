# Guia 3 — E-mails com Resend

O `EmailService` usa a API HTTPS do Resend no perfil `prod`. Em desenvolvimento,
usa SMTP local; falhas são registradas sem imprimir o HTML nem links de redefinição
no log. Os usos atuais são notificações de doação e recuperação de senha.

## 1. Criar a chave do ambiente

1. Crie sua conta em [resend.com](https://resend.com).
2. Em **API Keys → Create API Key**, use o nome `conexao-solidaria-staging`
   (ou um nome distinto para produção) e permissão **Sending access**.
3. No serviço backend da Railway, adicione **RESEND_API_KEY** com a chave criada.
   Não salve a chave em arquivos versionados nem a compartilhe em conversas.
4. O perfil `prod` já seleciona `app.mail.provider=resend`. Sem chave, a aplicação
   inicia, mas não envia mensagens e registra um aviso de configuração.

## 2. Teste inicial sem domínio próprio

- Na Railway, configure **MAIL_FROM=onboarding@resend.dev**.
- Esse remetente de teste é disponibilizado nos exemplos oficiais do Resend.
- Para entrega em uma caixa real, use como destinatário o e-mail associado à sua
  conta Resend, consultável no perfil do painel. Outros destinatários reais são
  rejeitados enquanto você usa o domínio de teste.
- Para testar recuperação de senha, esse mesmo e-mail precisa pertencer a um
  usuário cadastrado no banco de staging. `ADMIN_EMAIL` só serve para esse teste
  se coincidir com o e-mail da conta Resend e já tiver sido criado pelo seeder.
- Use **FRONTEND_URL=https://conexao-solidaria-staging.vercel.app** no staging,
  sem barra final, para que os links apontem para o ambiente de teste.

Fontes: [envio via API](https://resend.com/docs/api-reference/emails/send-email)
e [restrições do domínio resend.dev](https://resend.com/docs/knowledge-base/403-error-resend-dev-domain).

## 3. Envio para outros destinatários

1. Adicione um domínio ou subdomínio sob seu controle em **Domains → Add Domain**.
   Coordene o domínio de produção com o dono; staging pode usar um subdomínio próprio.
2. Cadastre no DNS os registros exatos fornecidos pelo painel Resend.
3. Aguarde o status **Verified**.
4. Troque **MAIL_FROM** por um endereço nesse domínio verificado. Por exemplo,
   produção pode usar `contato@conexaosolidarias.com.br`, após a verificação.

## 4. Deploy e validação

1. Publique a alteração do backend no fork e aplique as variáveis na Railway.
2. Confirme que o serviço inicia e `/actuator/health` retorna sucesso.
3. Depois de conectar o frontend à API de staging, use **Esqueci senha** com o
   destinatário permitido e confira a caixa de entrada, spam e o painel Resend.
4. Confira se o link abre o frontend do ambiente correto.
5. Em caso de falha, consulte **Logs** no Resend e os avisos HTTP no backend.

O healthcheck SMTP fica desativado em `prod`: esse ambiente envia via HTTPS,
pois SMTP não está disponível nos planos Railway Trial/Free/Hobby.
Isso não valida entrega de e-mail; a entrega precisa ser testada separadamente.
[Restrição SMTP na Railway](https://docs.railway.com/networking/outbound-networking).

## 5. Comportamento em falhas

- O cliente tem timeout de conexão de 5 segundos e leitura de 10 segundos.
- Rejeições e falhas de rede geram avisos; não há fallback para SMTP em `prod`
  nem repetição automática que possa duplicar mensagens.
- A aceitação pela API não garante entrega na caixa de entrada. Confira o painel.
- O fluxo de doação/reset mantém o comportamento anterior e não é interrompido
  por falhas de e-mail. Na recuperação de senha, sem entrega o usuário não recebe
  o link; o envio ainda precisa ser validado antes de liberar o ambiente.
- Confira limites e consumo no painel Resend e no [plano atual](https://resend.com/pricing).
