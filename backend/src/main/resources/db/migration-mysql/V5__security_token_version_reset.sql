-- S5/S4: versão do token (invalida sessões) e data de criação do reset.
ALTER TABLE users ADD COLUMN token_version INT NOT NULL DEFAULT 0;
ALTER TABLE password_reset_tokens ADD COLUMN created_at DATETIME;
