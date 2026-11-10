"use client";

import { use, useEffect, useState } from "react";
import Voltar from "@/components/Voltar";
import SafeImage from "@/components/SafeImage";
import { Alert, Spinner, formatarDataSimples } from "@/components/ui";
import { api } from "@/lib/api";
import { categoriaBlogLabel, imagemBlog, tempoLeitura } from "@/lib/blog";
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

  const imagem = imagemBlog(post.imagemUrl);

  return (
    <article className="mx-auto max-w-3xl">
      <Voltar fallback="/blog" />
      <p className="mt-3 flex flex-wrap items-center gap-x-2 gap-y-1 text-sm text-[var(--text-soft)]">
        <span className="rounded-full bg-[var(--accent-soft)] px-2 py-0.5 font-semibold text-[var(--primary)]">
          {categoriaBlogLabel(post.categoria)}
        </span>
        <span>{formatarDataSimples(post.dataPublicacao)}</span>
        <span aria-hidden="true">·</span>
        <span>{tempoLeitura(post.conteudo)} min de leitura</span>
        {post.autorNome && (
          <>
            <span aria-hidden="true">·</span>
            <span>por {post.autorNome}</span>
          </>
        )}
      </p>

      <h1 className="mt-3 text-3xl font-bold leading-tight">{post.titulo}</h1>
      {post.resumo && <p className="mt-3 text-lg text-[var(--text-soft)]">{post.resumo}</p>}

      {imagem && (
        <SafeImage
          src={imagem}
          alt={`Imagem do artigo ${post.titulo}`}
          loading="eager"
          className="mt-6 aspect-[16/9] w-full rounded-2xl object-cover"
        />
      )}

      <div className="mt-6 whitespace-pre-line leading-relaxed">{post.conteudo}</div>
    </article>
  );
}
