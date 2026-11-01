-- S2: verificação de e-mail (contas existentes ficam verificadas para não bloquear).
ALTER TABLE users ADD COLUMN email_verificado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN email_verificado_em DATETIME;
UPDATE users SET email_verificado = TRUE;

CREATE TABLE email_verification_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    created_at DATETIME,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_email_verif_user FOREIGN KEY (user_id) REFERENCES users (id)
);
