CREATE INDEX IF NOT EXISTS idx_ofertas_status ON ofertas (status);
CREATE INDEX IF NOT EXISTS idx_ofertas_aprovado ON ofertas (aprovado);
CREATE INDEX IF NOT EXISTS idx_oferta_images_oferta ON oferta_images (oferta_id);
