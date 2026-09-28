-- Logística de entrega: instruções por campanha + coleta domiciliar por doação.
ALTER TABLE campaigns ADD COLUMN instrucoes_entrega TEXT;
ALTER TABLE campaigns ADD COLUMN endereco_entrega VARCHAR(300);
ALTER TABLE donations ADD COLUMN precisa_coleta BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE donations ADD COLUMN endereco_coleta VARCHAR(300);
