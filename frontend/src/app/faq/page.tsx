import type { Metadata } from "next";

export const metadata: Metadata = { title: "Perguntas frequentes" };

const itens = [
  ["Como faço uma doação?", "Escolha uma campanha, informe o item e a quantidade — ou contribua com um valor via Pix, cartão ou transferência — e acompanhe o status no seu painel."],
  ["Preciso me cadastrar para doar?", "Sim. O cadastro é gratuito e permite acompanhar suas doações até a confirmação de recebimento."],
  ["Como as instituições são validadas?", "Toda instituição passa por análise (CNPJ, dados e atuação) antes de publicar campanhas."],
  ["Como sei que minha doação chegou?", "A instituição confirma o recebimento na plataforma e você recebe uma notificação, além do comprovante disponível no painel."],
  ["Posso doar valores em dinheiro?", "Sim, nas campanhas que aceitam contribuição em valor — por enquanto a confirmação é manual e a integração com gateways de pagamento está em evolução."],
  ["Meus dados estão seguros?", "Sim. Seguimos a LGPD: coletamos apenas o necessário, com seu consentimento, e você pode solicitar correção ou exclusão. Veja a página de Privacidade."],
  ["Sou de uma instituição. Como participo?", "Cadastre a instituição na plataforma e aguarde a validação. Após aprovada, você pode publicar campanhas e gerenciar doações."],
];

export default function FaqPage() {
  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="text-3xl font-bold">Perguntas frequentes</h1>
      <div className="mt-6 space-y-3">
        {itens.map(([pergunta, resposta]) => (
          <details key={pergunta} className="rounded-xl border p-4">
            <summary className="cursor-pointer font-semibold">{pergunta}</summary>
            <p className="mt-2 text-gray-700">{resposta}</p>
          </details>
        ))}
      </div>
    </div>
  );
}
