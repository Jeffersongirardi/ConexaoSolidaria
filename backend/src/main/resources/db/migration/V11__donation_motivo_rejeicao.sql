-- P0.7: motivo da rejeição de doação de item pela instituição.
ALTER TABLE donations ADD COLUMN motivo_rejeicao VARCHAR(300);
