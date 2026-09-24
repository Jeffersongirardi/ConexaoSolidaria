"use client";

import { useEffect, type ReactNode } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth";
import type { User } from "@/lib/types";
import { Spinner } from "./ui";

export default function RequireAuth({
  tipos,
  children,
}: {
  tipos?: User["tipo"][];
  children: ReactNode;
}) {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (loading) return;
    if (!user) {
      router.replace("/login");
      return;
    }
    if (tipos && !tipos.includes(user.tipo)) {
      router.replace("/");
    }
  }, [user, loading, tipos, router]);

  if (loading || !user) return <Spinner label="Verificando acesso..." />;
  if (tipos && !tipos.includes(user.tipo)) return <Spinner label="Verificando acesso..." />;
  return <>{children}</>;
}
