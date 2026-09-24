"use client";

import { use, useEffect, useState } from "react";
import RequireAuth from "@/components/RequireAuth";
import BlogForm from "@/components/BlogForm";
import { Alert, Spinner } from "@/components/ui";
import { api } from "@/lib/api";
import type { BlogPost } from "@/lib/types";

function EditarConteudo({ id }: { id: string }) {
  const [post, setPost] = useState<BlogPost | null>(null);
  const [erro, setErro] = useState("");
  const [salvo, setSalvo] = useState(false);

  useEffect(() => {
    api<BlogPost>(`/blog/by-id/${id}`)
      .then(setPost)
      .catch(() => setErro("Post não encontrado."));
  }, [id]);

  if (erro) return <Alert kind="error">{erro}</Alert>;
  if (!post) return <Spinner />;

  return (
    <div className="mx-auto max-w-3xl">
      <h1 className="text-2xl font-bold">Editar post</h1>
      {salvo && <div className="mt-2"><Alert kind="success">Post atualizado!</Alert></div>}
      <div className="mt-4"><BlogForm inicial={post} onSalvo={(p) => { setPost(p); setSalvo(true); }} /></div>
    </div>
  );
}

export default function EditarPostPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  return (
    <RequireAuth tipos={["admin"]}>
      <EditarConteudo id={id} />
    </RequireAuth>
  );
}
