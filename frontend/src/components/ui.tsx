import type { InputHTMLAttributes, ReactNode, SelectHTMLAttributes, TextareaHTMLAttributes } from "react";

export function Spinner({ label = "Carregando..." }: { label?: string }) {
  return (
    <p role="status" aria-live="polite" className="py-10 text-center text-gray-600">
      <span aria-hidden="true" className="mr-2 inline-block h-4 w-4 animate-spin rounded-full border-2 border-gray-300 border-t-blue-600 align-middle" />
      {label}
    </p>
  );
}

export function Alert({ kind, children }: { kind: "error" | "success" | "info"; children: ReactNode }) {
  const styles =
    kind === "error"
      ? "border-red-300 bg-red-50 text-red-800"
      : kind === "success"
        ? "border-green-300 bg-green-50 text-green-800"
        : "border-blue-300 bg-blue-50 text-blue-800";
  return (
    <div role={kind === "error" ? "alert" : "status"} className={`rounded-lg border p-3 text-sm ${styles}`}>
      {children}
    </div>
  );
}

export function EmptyState({ children }: { children: ReactNode }) {
  return <p className="rounded-lg border border-dashed border-gray-300 p-6 text-center text-sm text-gray-600">{children}</p>;
}

interface FieldProps {
  label: string;
  name: string;
  error?: string;
  children: ReactNode;
}

export function Field({ label, name, error, children }: FieldProps) {
  return (
    <div>
      <label htmlFor={name} className="mb-1 block text-sm font-medium text-gray-700">
        {label}
      </label>
      {children}
      {error && (
        <p id={`${name}-erro`} role="alert" className="mt-1 text-xs text-red-700">
          {error}
        </p>
      )}
    </div>
  );
}

const inputCls =
  "w-full rounded-lg border border-gray-300 px-3 py-2 text-sm shadow-sm focus:border-blue-600";

export function TextInput(props: InputHTMLAttributes<HTMLInputElement>) {
  return <input {...props} aria-invalid={props["aria-invalid"]} className={`${inputCls} ${props.className ?? ""}`} />;
}

export function TextArea(props: TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return <textarea {...props} className={`${inputCls} ${props.className ?? ""}`} />;
}

export function Select(props: SelectHTMLAttributes<HTMLSelectElement>) {
  return <select {...props} className={`${inputCls} ${props.className ?? ""}`} />;
}

export function PrimaryButton(props: React.ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      {...props}
      className={`rounded-lg bg-blue-600 px-4 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-60 ${props.className ?? ""}`}
    />
  );
}

export function SecondaryButton(props: React.ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      {...props}
      className={`rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-60 ${props.className ?? ""}`}
    />
  );
}

export function DangerButton(props: React.ButtonHTMLAttributes<HTMLButtonElement>) {
  return (
    <button
      {...props}
      className={`rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-60 ${props.className ?? ""}`}
    />
  );
}

export function Pagination({
  page,
  totalPages,
  onChange,
}: {
  page: number;
  totalPages: number;
  onChange: (p: number) => void;
}) {
  if (totalPages <= 1) return null;
  return (
    <nav aria-label="Paginação" className="mt-6 flex items-center justify-center gap-2">
      <SecondaryButton disabled={page <= 0} onClick={() => onChange(page - 1)} aria-label="Página anterior">
        ← Anterior
      </SecondaryButton>
      <span aria-current="page" className="text-sm text-gray-700">
        Página {page + 1} de {totalPages}
      </span>
      <SecondaryButton disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)} aria-label="Próxima página">
        Próxima →
      </SecondaryButton>
    </nav>
  );
}

const urgenciaCores: Record<string, string> = {
  alta: "bg-red-100 text-red-800",
  media: "bg-yellow-100 text-yellow-800",
  baixa: "bg-green-100 text-green-800",
};

export function UrgenciaBadge({ urgencia }: { urgencia: string }) {
  return (
    <span className={`inline-block rounded-full px-2 py-0.5 text-xs font-semibold ${urgenciaCores[urgencia] ?? "bg-gray-100 text-gray-800"}`}>
      {urgencia === "alta" ? "🔴 Alta" : urgencia === "media" ? "🟡 Média" : urgencia === "baixa" ? "🟢 Baixa" : urgencia}
    </span>
  );
}

const categoriaIcones: Record<string, string> = {
  alimento: "🍞",
  roupa: "👕",
  higiene: "🧼",
  material_escolar: "📚",
  outro: "📦",
};

export function categoriaIcone(categoria: string): string {
  return categoriaIcones[categoria] ?? "📦";
}

export function formatarData(iso?: string | null): string {
  if (!iso) return "—";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return iso;
  return d.toLocaleString("pt-BR", { day: "2-digit", month: "2-digit", year: "numeric", hour: "2-digit", minute: "2-digit" });
}
