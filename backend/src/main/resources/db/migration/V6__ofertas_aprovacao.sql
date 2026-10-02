-- Ofertas visíveis só a instituições, após aprovação do admin.
ALTER TABLE ofertas ADD COLUMN aprovado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ofertas ADD COLUMN motivo_recusa TEXT;
-- Ofertas já existentes (ambiente local de teste) partem aprovadas.
UPDATE ofertas SET aprovado = TRUE;
