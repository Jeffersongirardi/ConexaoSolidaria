"use client";

import { useEffect, useRef } from "react";

export default function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel = "Confirmar",
  cancelLabel = "Cancelar",
  onConfirm,
  onCancel,
}: {
  open: boolean;
  title: string;
  description?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  const ref = useRef<HTMLDialogElement>(null);
  useEffect(() => {
    if (open) ref.current?.showModal();
    else ref.current?.close();
  }, [open]);
  return (
    <dialog ref={ref} className="rounded-xl p-0 backdrop:bg-black/40" onClose={onCancel} aria-labelledby="confirm-title">
      <div className="min-w-80 max-w-md p-6">
        <h2 id="confirm-title" className="text-lg font-bold">{title}</h2>
        {description && <p className="mt-2 text-sm text-gray-600">{description}</p>}
        <div className="mt-6 flex justify-end gap-2">
          <button onClick={onCancel} className="rounded-lg border border-[var(--border)] px-4 py-2 text-sm hover:bg-gray-50">{cancelLabel}</button>
          <button onClick={onConfirm} className="rounded-lg bg-[var(--primary)] px-4 py-2 text-sm font-semibold text-white hover:brightness-95">{confirmLabel}</button>
        </div>
      </div>
    </dialog>
  );
}
