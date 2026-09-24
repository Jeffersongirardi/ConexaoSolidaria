"use client";

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import RequireAuth from "@/components/RequireAuth";
import { Alert, Field, PrimaryButton, Spinner, TextInput } from "@/components/ui";
import { api, type ApiError } from "@/lib/api";
import type { Payment } from "@/lib/types";

function PagamentoConteudo({ uuid }: { uuid: string }) {
  const router = useRouter();
  const [pagamento, setPagamento] = useState<Payment | null>(null);
  const [erro, setErro] = useState("");
  const [processando, setProcessando] = useState(false);
  const [comprovante, setComprovante] = useState<File | null>(null);

  const [cartao, setCartao] = useState({ numero: "", nome: "", validade: "", cvv: "" });

  useEffect(() => {
    api<Payment>(`/payments/${uuid}`)
      .then(setPagamento)
      .catch(() => setErro("Pagamento não encontrado."));
  }, [uuid]);

  const confirmar = async (metodo: string, form?: FormData) => {
    setProcessando(true);
    setErro("");
    try {
      const endpoint =
        metodo === "pix" ? "confirmar-pix" : metodo === "cartao" ? "confirmar-cartao" : "confirmar-transferencia";
      await api(`/payments/${uuid}/${endpoint}`, {
        method: "POST",
        ...(form ? { form } : { body: {} }),
      });
      router.push(`/pagamento/${uuid}/sucesso`);
    } catch (err) {
      setErro((err as ApiError).message);
    } finally {
      setProcessando(false);
    }
  };

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!pagamento) return <Spinner />;

  if (pagamento.status !== "pendente") {
    return (
      <div className="mx-auto max-w-md text-center">
        <h1 className="text-2xl font-bold">Pagamento já processado</h1>
        <p className="mt-2"><Link href={`/pagamento/${uuid}/sucesso`} className="text-blue-700 underline">Ver resultado</Link></p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-xl">
      <h1 className="text-2xl font-bold">Pagamento</h1>
      <p className="mt-1 text-sm text-gray-600">
        R$ {Number(pagamento.valor).toFixed(2)} para {pagamento.instituicaoNome}
        {pagamento.campaignTitulo ? ` — ${pagamento.campaignTitulo}` : ""} via {pagamento.metodo}.
      </p>
      <p className="mt-1 text-xs text-gray-500">Confirmação manual no momento — integração com gateway em evolução. Nenhum valor real é cobrado neste ambiente.</p>

      {pagamento.metodo === "pix" && (
        <section aria-label="Pagamento via Pix" className="mt-4 rounded-xl border p-5 text-center">
          <h2 className="font-bold">Escaneie o QR Code</h2>
          {pagamento.qrcode ? (
            <img src={pagamento.qrcode} alt="QR Code Pix para pagamento" className="mx-auto mt-3 h-64 w-64" />
          ) : (
            <p className="mt-3 text-sm">QR indisponível.</p>
          )}
          <p className="mt-2 text-sm">Chave Pix: <strong>{pagamento.pixKey ?? "—"}</strong></p>
          <p className="mt-1 text-sm">Valor: <strong>R$ {Number(pagamento.valor).toFixed(2)}</strong></p>
          <PrimaryButton onClick={() => void confirmar("pix")} disabled={processando} className="mt-4">
            {processando ? "Confirmando..." : "Já paguei — confirmar"}
          </PrimaryButton>
        </section>
      )}

      {pagamento.metodo === "cartao" && (
        <form
          aria-label="Pagamento com cartão"
          className="mt-4 space-y-3 rounded-xl border p-5"
          onSubmit={(e) => { e.preventDefault(); void confirmar("cartao"); }}
        >
          <h2 className="font-bold">Cartão de crédito</h2>
          <Field label="Número do cartão" name="numero"><TextInput id="numero" required inputMode="numeric" value={cartao.numero} onChange={(e) => setCartao({ ...cartao, numero: e.target.value })} placeholder="0000 0000 0000 0000" /></Field>
          <Field label="Nome impresso" name="nome"><TextInput id="nome" required value={cartao.nome} onChange={(e) => setCartao({ ...cartao, nome: e.target.value })} /></Field>
          <div className="grid grid-cols-2 gap-3">
            <Field label="Validade" name="validade"><TextInput id="validade" required value={cartao.validade} onChange={(e) => setCartao({ ...cartao, validade: e.target.value })} placeholder="MM/AA" /></Field>
            <Field label="CVV" name="cvv"><TextInput id="cvv" required inputMode="numeric" maxLength={4} value={cartao.cvv} onChange={(e) => setCartao({ ...cartao, cvv: e.target.value })} /></Field>
          </div>
          <PrimaryButton type="submit" disabled={processando} className="w-full">
            {processando ? "Processando..." : `Pagar R$ ${Number(pagamento.valor).toFixed(2)}`}
          </PrimaryButton>
        </form>
      )}

      {pagamento.metodo === "transferencia" && (
        <form
          aria-label="Pagamento por transferência"
          className="mt-4 space-y-3 rounded-xl border p-5"
          onSubmit={(e) => { e.preventDefault(); const f = new FormData(); if (comprovante) f.set("comprovante", comprovante); void confirmar("transferencia", f); }}
        >
          <h2 className="font-bold">Transferência bancária</h2>
          <p className="text-sm text-gray-600">Transfira para a conta da instituição e anexe o comprovante.</p>
          <Field label="Comprovante (imagem)" name="comprovante">
            <input id="comprovante" type="file" accept="image/*" onChange={(e) => setComprovante(e.target.files?.[0] ?? null)} className="text-sm" />
          </Field>
          <PrimaryButton type="submit" disabled={processando} className="w-full">
            {processando ? "Enviando..." : "Confirmar transferência"}
          </PrimaryButton>
        </form>
      )}
    </div>
  );
}

export default function PagamentoPage({ params }: { params: Promise<{ uuid: string }> }) {
  const { uuid } = use(params);
  return (
    <RequireAuth tipos={["doador"]}>
      <PagamentoConteudo uuid={uuid} />
    </RequireAuth>
  );
}
