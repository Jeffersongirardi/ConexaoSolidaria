import type { Metadata } from "next";

export const metadata: Metadata = { title: "Perguntas frequentes" };

const itens = [
  ["Como faço uma doação?", "Escolha uma campanha, informe item e quantidade — ou contribua com valor via Pix direto, cartão (registro manual) ou transferência — e acompanhe no painel. Item: combine a entrega com a instituição. Valor: confira titular/chave antes de pagar."],
  ["A plataforma fica com o dinheiro?", "Não. Taxa sempre R$ 0. Pix/transferência vão direto para a conta da instituição. Não recebemos, seguramos ou repassamos. Saldo da plataforma: R$ 0."],
  ["Preciso me cadastrar para doar?", "Sim. O cadastro é gratuito e permite acompanhar o status informado pela instituição, sem garantia da plataforma."],
  ["Como as instituições são validadas?", "Exigimos CNPJ ativo e conferimos documentos básicos antes de aprovar. Isso não é auditoria nem garantia de entrega ou uso correto."],
  ["Como sei que minha doação chegou?", "A instituição confirma na plataforma e você recebe notificação + comprovante. Se não confirmar ou houver divergência, fale direto com a instituição — não conseguimos forçar entrega nem devolver valor."],
  ["Posso doar valores em dinheiro?", "Sim, nas campanhas que aceitam. Pix é direto à instituição; cartão é só registro manual (não salvamos dados, nenhum débito pela plataforma); transferência pede anexo do comprovante."],
  ["E se eu errar o valor ou me arrepender?", "Sem reembolso pela plataforma. Trate direto com a instituição e/ou seu banco/operadora Pix, com comprovante em mãos."],
  ["Meus dados estão seguros?", "Sim. Seguimos a LGPD, coletamos o mínimo e você pode pedir acesso/correção/eliminação pelo jefferson@fourpay.com.br. Veja Privacidade."],
  ["Sou de uma instituição. Como participo?", "Cadastre com CNPJ ativo e aguarde validação. Aprovada, publique campanhas, confirme recebimentos e poste atualizações (mensagem/foto)."],
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
