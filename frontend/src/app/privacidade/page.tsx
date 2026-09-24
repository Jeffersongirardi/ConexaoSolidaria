import type { Metadata } from "next";

export const metadata: Metadata = { title: "Privacidade (LGPD)" };

export default function PrivacidadePage() {
  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="text-3xl font-bold">Privacidade e LGPD</h1>
      <p className="mt-4 leading-relaxed">
        O Conexões Solidárias trata dados pessoais conforme a Lei Geral de Proteção
        de Dados (Lei nº 13.709/2018).
      </p>
      <h2 className="mt-6 text-lg font-bold">Dados coletados</h2>
      <ul className="mt-2 list-disc space-y-1 pl-6 text-gray-700">
        <li>Doadores: nome, e-mail, telefone/WhatsApp, CPF (opcional), endereço e data de nascimento (opcional).</li>
        <li>Instituições: dados do responsável, CNPJ, razão social, endereço, contatos e chave Pix.</li>
        <li>Doações e pagamentos: itens, quantidades, valores e comprovantes.</li>
      </ul>
      <h2 className="mt-6 text-lg font-bold">Finalidade e base legal</h2>
      <p className="mt-2 text-gray-700">
        Os dados são usados exclusivamente para operar a plataforma (cadastro,
        campanhas, doações e comunicação), com base no seu consentimento e na
        execução do serviço.
      </p>
      <h2 className="mt-6 text-lg font-bold">Seus direitos</h2>
      <p className="mt-2 text-gray-700">
        Você pode solicitar acesso, correção, portabilidade ou eliminação dos seus
        dados a qualquer momento pela página de contato.
      </p>
      <h2 className="mt-6 text-lg font-bold">Segurança</h2>
      <p className="mt-2 text-gray-700">
        Senhas são armazenadas com criptografia (BCrypt) e o acesso é protegido
        por autenticação. Nunca compartilhamos seus dados com terceiros para fins
        comerciais.
      </p>
    </div>
  );
}
