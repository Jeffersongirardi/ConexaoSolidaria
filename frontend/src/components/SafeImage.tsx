"use client";

import { useState } from "react";

type Props = React.ImgHTMLAttributes<HTMLImageElement> & { src: string; alt: string };

export default function SafeImage({ src, alt, ...rest }: Props) {
  const [falhou, setFalhou] = useState(false);
  if (falhou) return null;
  return <img src={src} alt={alt} onError={() => setFalhou(true)} {...rest} />;
}
