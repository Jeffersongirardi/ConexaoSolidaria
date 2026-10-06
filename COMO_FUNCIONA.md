# Como funciona o Conexões Solidárias

Guia simples para entender a plataforma na prática: quem usa, o que cada um faz e como as doações acontecem do início ao fim.

> **Em 1 minuto:** o Conexões Solidárias é uma vitrine que aproxima **doadores** de **instituições validadas**. A plataforma **só intermedia — taxa sempre R$ 0**. Pix e transferências vão **direto para a conta da instituição**. Não guardamos valores, não garantimos entregas e não fazemos reembolso. Dúvidas jurídicas: ver [Termos de Uso](frontend/src/app/termos/page.tsx) e [Privacidade](frontend/src/app/privacidade/page.tsx).

## Índice

1. [Papel: Doador](#1-papel-doador)
2. [Papel: Instituição](#2-papel-instituicao)
3. [Papel: Administrador](#3-papel-administrador)
4. [Fluxo: doar um item](#4-fluxo-doar-um-item)
5. [Fluxo: doar um valor](#5-fluxo-doar-um-valor)
6. [Fluxo: ofertar um item (sofá, piano, violão)](#6-fluxo-ofertar-um-item-sofá-piano-violão)
7. [Regras que valem para todos](#7-regras-que-valem-para-todos)
8. [Glossário](#8-glossário)

---

## 1. Papel: Doador

Quem tem algo a doar: alimentos, roupas, móveis — ou valores via Pix, cartão e transferência.

**O que o doador faz:**
- Cria conta gratuita e acompanha tudo em **Meu painel**.
- **Doa itens:** escolhe uma campanha, informa item e quantidade, combina a entrega pelo WhatsApp e aguarda a confirmação.
- **Doa valores:** escolhe campanha que aceita contribuição, paga **direto à instituição** e anexa o comprovante.
- **Oferta itens grandes:** publica sofá, piano, violão etc. com fotos e data de compromisso; instituições reivindicam.
- **Cancela** intenções pendentes e ofertas (com motivo) a qualquer momento antes da entrega.

**Exemplo — Maria doa 10 kg de arroz:**
1. Maria entra na campanha “Cesta básica de junho”, informa “arroz, 10 kg” e clica em registrar.
2. Aparece o cartão verde: WhatsApp da instituição + endereço de entrega + botão de conversa pronta.
3. Maria chama no WhatsApp, entrega na quinta-feira.
4. A instituição confirma o recebimento — Maria vê “✅ Recebido” e o comprovante no painel.

---

## 2. Papel: Instituição

ONGs, associações e projetos sociais com **CNPJ ativo**, aprovados pelo admin.

**O que a instituição faz:**
- Cadastra-se e **aguarda aprovação** (CNPJ + documentos básicos; resposta em até 2 dias úteis).
- **Publica campanhas** com fotos, meta e instruções de entrega (dias, horários, endereço).
- **Confirma recebimentos** de itens e valores — só aí a doação conta nas métricas.
- **Posta atualizações** (mensagem + foto) para o doador acompanhar.
- **Reivindica ofertas** de doadores e **coleta em até 7 dias**.

**Exemplo — Cantinho Feliz recebe uma cesta:**
1. O Cantinho publica “Cesta básica de junho” com fotos e “entregas seg–sex, 9h–17h”.
2. Recebe a intenção de Maria no painel, com o WhatsApp dela.
3. Combinam, recebem os 10 kg e clicam em **Confirmar recebimento**.
4. Maria é notificada e o painel do Cantinho soma +1 entrega.

---

## 3. Papel: Administrador

Equipe da plataforma. Cuida da **confiança**, não das entregas.

**O que o admin faz:**
- **Aprova ou recusa instituições** (CNPJ) e **ofertas** (com motivo obrigatório na recusa).
- **Modera doações** denunciadas (cancela em caso de fraude — o fluxo normal nunca passa por ele).
- Gerencia usuários, blog e mensagens de contato.

**O que o admin NÃO faz:**
- Não confirma entregas, não movimenta valores, não reembolsa ninguém.

---

## 4. Fluxo: doar um item

```
Doador registra intenção (pendente, "A combinar entrega")
  → recebe WhatsApp + endereço + instruções da campanha
  → entrega no local OU pede coleta (grandes volumes)
  → instituição confirma (recebido ✅) → comprovante liberado
  → doador pode cancelar enquanto pendente
```

- Status possíveis: `pendente` → `recebido` | `cancelado`.
- Contato (WhatsApp/endereço) aparece **só após a intenção**, para os dois lados.

---

## 5. Fluxo: doar um valor

```
Doador cria pagamento (pendente)
  → paga no app do banco pela chave da instituição
  → clica "Já paguei" e anexa o comprovante (confirmado)
  → instituição confere o extrato e confirma (recebido ✅)
```

- **Pix/transferência:** dinheiro direto à instituição; comprovante liberado a partir de confirmado (anexo de transferência opcional no MVP).
- **Cartão:** apenas registro manual (nenhum débito real é feito pela plataforma; dados do cartão não são salvos).
- Comprovante só existe após a confirmação.
- Status possíveis: `pendente` → `confirmado` → `recebido`.

---

## 6. Fluxo: ofertar um item (sofá, piano, violão)

```
Doador publica oferta com fotos + data de compromisso (disponível)
  → admin aprova (visível às instituições) ou recusa (com motivo)
  → instituição reivindica (reservada, prazo de 7 dias para coletar)
  → coleta e confirma (entregue ✅)
  → se não coletar: doador libera de novo OU instituição desiste
  → doador pode editar (só disponível) ou cancelar (com motivo)
```

- Ofertas exigem login: lista e detalhe só para instituições aprovadas (visitantes veem só a chamada na home). Doadores veem as suas no painel.
- Reivindicar exige marcar o aceite de coleta em 7 dias; atraso mostra badge ⚠️ e confirmação atrasada avisa o doador.
- Editou após recusa? Volta automaticamente para a fila de aprovação.

---

## 7. Regras que valem para todos

| Regra | Detalhe |
|-------|---------|
| Taxa | Sempre R$ 0, para todos. Saldo da plataforma: R$ 0. |
| Dinheiro | Vai direto à instituição. Sem custódia, sem repasse nosso. |
| Reembolso | Não fazemos. Erro/arrependimento: tratar com instituição/banco, com comprovante. |
| Garantia | Validação de CNPJ é checagem inicial, não auditoria. Não garantimos entrega, prazo ou uso. |
| Contatos | Compartilhados só após intenção/reserva, só o necessário (LGPD). |
| Prazos | Coleta de oferta em até 7 dias (código); aprovação em até 2 dias úteis e LGPD em até 15 dias (SLA documental, sem enforcement automático). |
| Conduta | Informações verdadeiras, sem cartão/conta de terceiros. Fraude = suspensão. |

---

## 8. Glossário

- **Intenção:** registro de “quero doar X” (ainda não entregue).
- **Reserva:** instituição reivindicou uma oferta; tem 7 dias para coletar.
- **Confirmar recebimento:** ato da instituição que oficializa a entrega.
- **Comprovante:** documento liberado a partir de confirmado (não prova pagamento bancário, prova o registro).
- **Aceite:** compromisso marcado pela instituição ao reivindicar (coleta em 7 dias).
- **Atualização:** mensagem/foto que a instituição posta sobre uma doação.
- **Coleta:** retirada no endereço do doador (grandes volumes).
- **Disponível até:** data-compromisso em que o doador mantém a oferta.
