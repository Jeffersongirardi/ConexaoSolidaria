-- Aceite explícito do compromisso de coleta em até 7 dias (P0.4).
ALTER TABLE ofertas ADD COLUMN aceite_coleta BOOLEAN NOT NULL DEFAULT FALSE;
