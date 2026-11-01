-- E4: controle de lembretes enviados (idempotência).
CREATE TABLE email_lembretes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(40) NOT NULL,
    referencia_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    enviado_em DATETIME,
    CONSTRAINT uq_email_lembrete UNIQUE (tipo, referencia_id, user_id)
);
