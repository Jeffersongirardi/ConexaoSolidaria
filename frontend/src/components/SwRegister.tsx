"use client";

import { useEffect } from "react";

export default function SwRegister() {
  useEffect(() => {
    if ("serviceWorker" in navigator && process.env.NODE_ENV === "production") {
      navigator.serviceWorker.register("/sw.js").catch(() => {
        /* offline indisponível: app segue funcionando online */
      });
    }
  }, []);
  return null;
}
