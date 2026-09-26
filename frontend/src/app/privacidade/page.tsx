import type { Metadata } from "next";
import Link from "next/link";

export const metadata: Metadata = { title: "Privacidade (LGPD)" };

export default function PrivacidadePage() {
  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="text-3xl font-bold">Privacidade e LGPD</h1>
      <p className="mt-4 leading-relaxed">
        O Conexões Solidárias trata dados pessoais conforme a Lei Geral de Proteção
        de Dados (Lei nº 13.709/2018). Somos mera plataforma de intermediação — taxa sempre R$ 0.
      </p>

      <h2 className="mt-6 text-lg font-bold">Controlador e encarregado (DPO)</h2>
      <p className="mt-2 text-gray-700">
        Controlador: Conexões Solidárias, via <Link href="/contato" className="underline">Fale conosco</Link>.
        Encarregado LGPD: jefferson@fourpay.com.br — prazo de resposta até 15 dias.
        Use esse canal para acesso, correção, portabilidade, eliminação e revogação de consentimento.
      </p>

      <h2 className="mt-6 text-lg font-bold">Dados coletados</h2>
      <ul className="mt-2 list-disc space-y-1 pl-6 text-gray-700">
        <li>Doadores: nome, e-mail, telefone/WhatsApp, CPF (opcional), endereço e data de nascimento (opcional).</li>
        <li>Instituições: dados do responsável, CNPJ ativo obrigatório, razão social, endereço, contatos e chave Pix + titular.</li>
        <li>Doações e pagamentos: itens, quantidades, valores, mensagens de acompanhamento e comprovantes (imagem).</li>
        <li>Contato: nome, e-mail, assunto e mensagem.</li>
        <li>Cartão: número, nome, validade e CVV são usados só na tela para simulação/registro manual — não armazenamos nem transmitimos a gateway. Não salve CVV.</li>
      </ul>

      <h2 className="mt-6 text-lg font-bold">Finalidade e base legal</h2>
      <ul className="mt-2 list-disc space-y-1 pl-6 text-gray-700">
        <li>Cadastro, login e operação de campanhas/doações: execução de contrato/serviço (art. 7º, V).</li>
        <li>Comunicação, notificações e acompanhamento: execução + legítimo interesse (art. 7º, IX).</li>
        <li>Validação de instituições (CNPJ ativo): cumprimento de obrigação e prevenção a fraude (art. 7º, II e X).</li>
        <li>Marketing opcional e consentimentos de cadastro/contato: consentimento (art. 7º, I), revogável a qualquer momento pelo e-mail do DPO.</li>
      </ul>

      <h2 className="mt-6 text-lg font-bold">Com quem compartilhamos (necessário)</h2>
      <ul className="mt-2 list-disc space-y-1 pl-6 text-gray-700">
        <li>Doador ↔ instituição: nome, WhatsApp/contato e chave Pix/titular exibidos para viabilizar entrega e pagamento direto. Sem esse compartilhamento a doação não funciona.</li>
        <li>Hospedagem, banco de dados e e-mail transacional para operar a plataforma.</li>
        <li>Bancos/operadoras Pix apenas quando você paga direto à instituição — nós não processamos o valor.</li>
        <li>Nunca vendemos ou compartilhamos para fins comerciais. Sem ordem judicial ou obrigação legal, nada além do acima.</li>
      </ul>

      <h2 className="mt-6 text-lg font-bold">Retenção</h2>
      <p className="mt-2 text-gray-700">
        Mantemos cadastros enquanto a conta existir; doações/pagamentos e comprovantes por até 5 anos para fins fiscais e antifraude;
        mensagens de contato por até 12 meses. Depois, anonimizamos ou eliminamos, salvo obrigação legal maior.
      </p>

      <h2 className="mt-6 text-lg font-bold">Seus direitos</h2>
      <p className="mt-2 text-gray-700">
        Acesso, correção, portabilidade, eliminação, revogação e oposição pelo e-mail jefferson@fourpay.com.br ou pela{" "}
        <Link href="/contato" className="underline">página de contato</Link>. Menores de idade só com responsável.
      </p>

      <h2 className="mt-6 text-lg font-bold">Segurança</h2>
      <p className="mt-2 text-gray-700">
        Senhas com BCrypt, autenticação por token, acesso por papéis (doador/instituição/admin). Logs e uploads com controle de acesso.
        Nenhuma medida é 100% infalível — avise incidentes pelo DPO.
      </p>
    </div>
  );
}
