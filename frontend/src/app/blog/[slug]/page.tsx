"use client";

import { use, useEffect, useState } from "react";
import { Alert, Spinner, formatarData } from "@/components/ui";
import { api } from "@/lib/api";
import type { BlogPost } from "@/lib/types";

export default function BlogPostPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = use(params);
  const [post, setPost] = useState<BlogPost | null>(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    api<BlogPost>(`/blog/${slug}`, { token: null })
      .then(setPost)
      .catch(() => setErro("Post não encontrado."));
  }, [slug]);

  if (erro) return <div className="mt-4"><Alert kind="error">{erro}</Alert></div>;
  if (!post) return <Spinner />;

  return (
    <article className="mx-auto max-w-3xl">
      <p className="text-sm text-gray-500">{post.categoria} · {formatarData(post.dataPublicacao)}{post.autorNome ? ` · por ${post.autorNome}` : ""}</p>
      <h1 className="mt-2 text-3xl font-bold">{post.titulo}</h1>
      {post.resumo && <p className="mt-2 text-lg text-gray-600">{post.resumo}</p>}
      <div className="mt-6 whitespace-pre-line leading-relaxed">{post.conteudo}</div>
    </article>
  );
}
