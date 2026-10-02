"use client";

import { useCallback, useRef, useState } from "react";
import ConfirmDialog from "./ConfirmDialog";

type Opts = { title: string; description?: string; confirmLabel?: string };

/** Substitui confirm() nativo por modal acessível. Uso: if (!(await confirmar({...}))) return; */
export function useConfirm() {
  const [opts, setOpts] = useState<Opts | null>(null);
  const resolver = useRef<((v: boolean) => void) | null>(null);

  const confirmar = useCallback(
    (o: Opts) =>
      new Promise<boolean>((resolve) => {
        resolver.current = resolve;
        setOpts(o);
      }),
    []
  );

  const fechar = (v: boolean) => {
    setOpts(null);
    resolver.current?.(v);
  };

  const dialog = (
    <ConfirmDialog
      open={opts !== null}
      title={opts?.title ?? ""}
      description={opts?.description}
      confirmLabel={opts?.confirmLabel}
      onConfirm={() => fechar(true)}
      onCancel={() => fechar(false)}
    />
  );

  return { confirmar, dialog };
}
