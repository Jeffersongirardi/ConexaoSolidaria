import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = { title: "Termos de Uso" };

export default function TermosPage() {
  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="text-3xl font-bold">Termos de Uso</h1>
      <p className="mt-4 leading-relaxed text-gray-700">
        O Conexões Solidárias é uma vitrine de aproximação entre doadores e
        instituições. Ao usar a plataforma você concorda com estes Termos e com a{" "}
        <Link href="/privacidade" className="text-[var(--primary)] underline">Política de Privacidade (LGPD)</Link>.
      </p>

      <h2 className="mt-6 text-lg font-bold">1. Natureza: só intermediamos, taxa sempre R$ 0</h2>
      <ul className="mt-2 list-disc space-y-1 pl-6 text-gray-700">
        <li>Somos mera aproximação. Não somos instituição financeira, não recebemos, não seguramos, não repassamos e não custodiamos valores ou itens.</li>
        <li>Pix, cartão ou transferência vão direto para a conta indicada pela instituição. O saldo em conta da plataforma é R$ 0.</li>
        <li>Não cobramos taxa ou comissão de doadores ou instituições — sempre R$ 0. Se isso mudar um dia, avisaremos antes e pediremos novo aceite.</li>
      </ul>

      <h2 className="mt-6 text-lg font-bold">2. Validação não é garantia</h2>
      <ul className="mt-2 list-disc space-y-1 pl-6 text-gray-700">
        <li>Exigimos CNPJ ativo e conferimos documentos básicos antes de aprovar instituições e campanhas.</li>
        <li>Essa checagem inicial não é auditoria, fiscalização ou endosso. Conteúdo, meta, veracidade e uso dos recursos são responsabilidade exclusiva da instituição que publica.</li>
      </ul>

      <h2 className="mt-6 text-lg font-bold">3. Entrega e uso dos recursos</h2>
      <ul className="mt-2 list-disc space-y-1 pl-6 text-gray-700">
        <li>Itens: após registrar a intenção, combine a entrega diretamente com a instituição (painel, WhatsApp exibido, ponto de entrega). Só considere entregue após a instituição confirmar na plataforma.</li>
        <li>Valores: confira titular, chave Pix e valor antes de pagar. O botão “Já paguei — confirmar” apenas registra sua declaração na plataforma, não é débito feito por nós.</li>
        <li>Não garantimos que item/valor chegará, será suficiente, será bem aplicado, nem prazo.</li>
      </ul>

      <h2 className="mt-6 text-lg font-bold">4. Acordos diretos</h2>
      <p className="mt-2 text-gray-700">
        Qualquer contato, entrega, pagamento, Pix feito fora da chave/QR exibido, acordo verbal ou combinação por WhatsApp é ato exclusivo entre doador e
        instituição. A plataforma não é parte e não responde por esses atos, dentro ou fora da plataforma.
      </p>

      <h2 className="mt-6 text-lg font-bold">5. Sem reembolso pela plataforma</h2>
      <p className="mt-2 text-gray-700">
        Não fazemos estorno, reembolso ou devolução. Erro de valor, chave errada, arrependimento, campanha pausada/cancelada ou divergência:
        trate diretamente com a instituição e/ou seu banco/operadora Pix, com comprovante em mãos. Podemos ajudar a intermediar o contato via{" "}
        <Link href="/contato" className="underline">Fale conosco</Link>, sem garantir resultado.
      </p>

      <h2 className="mt-6 text-lg font-bold">6. Regras de uso</h2>
      <ul className="mt-2 list-disc space-y-1 pl-6 text-gray-700">
        <li>Forneça informações verdadeiras. Não use cartão ou conta de terceiros sem autorização.</li>
        <li>Use a plataforma apenas para doações legítimas e respeite as instituições parceiras.</li>
        <li>Podemos suspender ou remover contas, campanhas ou conteúdos em caso de fraude, golpe ou violação destes Termos.</li>
        <li>Cartão: mantemos o formulário para simulação/registro manual. Não salvamos número, validade ou CVV. Nenhum débito é feito pela plataforma.</li>
      </ul>

      <h2 className="mt-6 text-lg font-bold">Contato</h2>
      <p className="mt-2 text-gray-700">
        Dúvidas sobre os termos? Fale conosco pela <Link href="/contato" className="underline">página de contato</Link> ou com o encarregado LGPD em jefferson@fourpay.com.br.
      </p>
    </div>
  );
}
