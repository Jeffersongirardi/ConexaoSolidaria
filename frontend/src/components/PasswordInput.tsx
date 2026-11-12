"use client";
import { useState, type InputHTMLAttributes } from "react";
import { Eye, EyeOff } from "lucide-react";

export default function PasswordInput(props: InputHTMLAttributes<HTMLInputElement>) {
  const [visible, setVisible] = useState(false);
  return <div className="relative">
    <input {...props} type={visible ? "text" : "password"} className={`${props.className ?? ""} pr-12`} />
    <button type="button" disabled={props.disabled} aria-controls={props.id}
      aria-label={visible ? "Ocultar senha" : "Mostrar senha"} aria-pressed={visible}
      onClick={() => setVisible(!visible)}
      className="absolute inset-y-0 right-0 flex w-11 cursor-pointer items-center justify-center rounded-r-lg bg-transparent text-[var(--text-soft)] transition-colors hover:text-[var(--primary)] disabled:cursor-not-allowed disabled:opacity-50">
      {visible ? <EyeOff aria-hidden="true" size={20} /> : <Eye aria-hidden="true" size={20} />}
    </button>
  </div>;
}
