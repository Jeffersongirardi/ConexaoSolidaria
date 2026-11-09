"use client";
import { useState, type InputHTMLAttributes } from "react";

export default function PasswordInput(props: InputHTMLAttributes<HTMLInputElement>) {
  const [visible, setVisible] = useState(false);
  return <div className="relative">
    <input {...props} type={visible ? "text" : "password"} className={`${props.className ?? ""} pr-24`} />
    <button type="button" disabled={props.disabled} aria-controls={props.id}
      aria-label={visible ? "Ocultar senha" : "Mostrar senha"} aria-pressed={visible}
      onClick={() => setVisible(!visible)}
      className="absolute inset-y-0 right-0 rounded-r-lg px-3 text-sm font-medium focus-visible:outline-2">
      {visible ? "Ocultar" : "Mostrar"}
    </button>
  </div>;
}
