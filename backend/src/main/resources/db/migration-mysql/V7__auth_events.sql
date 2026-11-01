-- S3: auditoria de eventos de autenticação (sem tokens/senhas).
CREATE TABLE auth_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    email VARCHAR(255),
    evento VARCHAR(50) NOT NULL,
    ip VARCHAR(45),
    user_agent VARCHAR(255),
    criado_em DATETIME
);
