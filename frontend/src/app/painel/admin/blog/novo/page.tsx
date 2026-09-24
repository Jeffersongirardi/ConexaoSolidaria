"use client";

import { useRouter } from "next/navigation";
import RequireAuth from "@/components/RequireAuth";
import BlogForm from "@/components/BlogForm";

export default function NovoPostPage() {
  const router = useRouter();
  return (
    <RequireAuth tipos={["admin"]}>
      <div className="mx-auto max-w-3xl">
        <h1 className="text-2xl font-bold">Novo post</h1>
        <div className="mt-4"><BlogForm onSalvo={(p) => router.push(`/blog/${p.slug}`)} /></div>
      </div>
    </RequireAuth>
  );
}
