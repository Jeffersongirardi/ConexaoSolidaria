# Guia 2 — Imagens no Cloudflare R2

O backend usa `R2StorageService` no perfil `prod`. Em desenvolvimento, `FileStorageService` continua usando disco local. Os arquivos enviados ao R2 sobrevivem a reinícios e redeploys da Railway.

## 1. Bucket privado

1. Ative R2 Object Storage na conta Cloudflare. Confira os preços e as condições apresentados antes de ativar a assinatura.
2. Crie um bucket **Standard**, com localização automática. No staging usamos `conexao-solidaria-staging`; produção deve usar seu próprio bucket e credenciais.
3. Mantenha **Public Development URL desativada** e sem domínio público no bucket. Não habilite acesso público: o mesmo bucket contém comprovantes e fotos de atualizações de doações.

O backend libera fotos de campanhas, avatares e ofertas pelo caminho `/uploads/{pasta}/{arquivo}`. Ele redireciona o navegador para um link S3 assinado por 15 minutos. O caminho gravado no banco continua permanente, como `/uploads/campaigns/<uuid>.png`.

Comprovantes e fotos de atualizações não têm redirecionamento público. A API gera links assinados apenas nas respostas de pagamentos/doações, após verificar o acesso do doador, instituição participante ou administrador. Quem possui o link consegue abrir o arquivo enquanto ele estiver válido; não compartilhe esses links. Reabra a tela para obter um novo link após a expiração.

## 2. Credenciais S3

Em **R2 → Manage R2 API Tokens**, crie um **Account API Token**:

- Nome no staging: `backend-railway-staging`.
- Permissão: **Object Read & Write**.
- Escopo: **Apply to specific buckets only**, selecionando `conexao-solidaria-staging`.
- TTL conforme a política da equipe; registre e renove antes de expirar, caso defina um prazo.
- Deixe os filtros de IP vazios quando não houver IP de saída fixo na Railway.

Guarde `Access Key ID`, `Secret Access Key` e o endpoint S3 exibidos. O backend usa as duas credenciais **S3**, não o valor de API Token da Cloudflare. Não envie segredos por chat/prints nem coloque no Git.

## 3. Variables na Railway

No serviço do **backend**, configure:

| Variável | Valor |
|---|---|
| `R2_ENDPOINT` | Endpoint S3 HTTPS da conta, sem nome do bucket; exemplo `https://<account-id>.r2.cloudflarestorage.com` |
| `R2_ACCESS_KEY` | Access Key ID |
| `R2_SECRET_KEY` | Secret Access Key |
| `R2_BUCKET` | `conexao-solidaria-staging` neste staging |

Não é necessário `R2_PUBLIC_URL`. As quatro variáveis são obrigatórias para inicializar o perfil `prod`; não existe fallback para disco em produção. O healthcheck confirma a inicialização da aplicação, mas não testa as permissões R2: valide o upload real.

Se as variáveis ainda estiverem pendentes, aplique **Deploy Changes** na Railway. Depois publique o código pelo GitHub: o deploy automático deve usar as variáveis já aplicadas. Confirme que o deploy ativo corresponde ao commit da integração R2.

Na Vercel, mantenha `NEXT_PUBLIC_API_ORIGIN` apontando ao domínio HTTPS do backend. As credenciais R2 ficam apenas na Railway. `fileUrl()` já suporta caminhos do backend e links HTTPS assinados.

## 4. Validação após o deploy

1. Aguarde o serviço ficar **Online** e o healthcheck passar.
2. Pelo frontend, envie um avatar ou foto de campanha (PNG/JPG/JPEG/GIF/WEBP, até 5 MB). Confirme que a imagem aparece e que o objeto existe na aba **Objects** do bucket.
3. Abra a foto pública em uma aba anônima: ela deve carregar pelo redirecionamento do backend, mesmo com o bucket privado.
4. Envie um comprovante de transferência ou uma foto de atualização de doação com usuários de teste. Confirme que os participantes conseguem visualizar a foto pela resposta autorizada da API.
5. O caminho permanente `/uploads/comprovantes/...` ou `/uploads/updates/...` deve negar acesso direto, inclusive quando autenticado. O link temporário S3 exibido na resposta autorizada funciona por até 15 minutos.
6. Redeploy o backend e abra novamente a foto pública: deve continuar disponível.

Arquivos antigos salvos no disco da Railway **não são migrados automaticamente**. Faça novo upload das imagens de teste após ativar R2. A exclusão de registros/imagens nem sempre chama `StorageService.delete`; a limpeza de objetos órfãos continua pendente.

## 5. Custos

R2 Standard possui franquia mensal e cobrança por uso excedente. Acompanhe armazenamento e operações no painel; a franquia não é um limite automático de gastos. Confira valores atuais na [documentação de preços](https://developers.cloudflare.com/r2/pricing/).

Referências: [SDK Java e configuração S3](https://developers.cloudflare.com/r2/examples/aws/aws-sdk-java/) e [links assinados](https://developers.cloudflare.com/r2/api/s3/presigned-urls/).
