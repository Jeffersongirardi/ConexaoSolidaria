-- Baseline MySQL (prod, Railway) — schema FINAL consolidado de V1–V8 do H2.
-- Banco NOVO, sem dados legados: não portar o histórico (usa sintaxe
-- Postgres/H2 inexistente no MySQL: IDENTITY, RENAME COLUMN/CONSTRAINT, IF NOT EXISTS).
-- Novas evoluções entram como V2__... nesta mesma pasta, com sintaxe válida em H2 E MySQL.

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(255),
    data_nascimento DATE,
    telefone VARCHAR(255),
    whatsapp VARCHAR(255),
    cep VARCHAR(255),
    cidade VARCHAR(255),
    estado VARCHAR(2),
    tipo VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    ativo BOOLEAN NOT NULL,
    data_cadastro DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE institution_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    cnpj VARCHAR(18) NOT NULL UNIQUE,
    razao_social VARCHAR(255) NOT NULL,
    nome_fantasia VARCHAR(255),
    endereco VARCHAR(300),
    website VARCHAR(255),
    foto_url VARCHAR(500),
    descricao TEXT,
    categoria_atuacao VARCHAR(255),
    whatsapp VARCHAR(20),
    pix_key VARCHAR(100),
    pix_titular VARCHAR(100),
    aprovado BOOLEAN NOT NULL,
    data_cadastro DATETIME,
    motivo_recusa TEXT,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE campaigns (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    instituicao_id BIGINT NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    categoria VARCHAR(255),
    quantidade_alvo VARCHAR(255) NOT NULL,
    urgencia VARCHAR(255),
    aceita_financeiro BOOLEAN,
    progresso INTEGER,
    ativo BOOLEAN NOT NULL,
    data_criacao DATETIME,
    data_encerramento DATETIME,
    instrucoes_entrega TEXT,
    endereco_entrega VARCHAR(300),
    CONSTRAINT fk_campaign_institution FOREIGN KEY (instituicao_id) REFERENCES institution_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE campaign_images (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    campaign_id BIGINT NOT NULL,
    filename VARCHAR(200) NOT NULL,
    legenda VARCHAR(200),
    ordem INTEGER,
    data_upload DATETIME,
    CONSTRAINT fk_image_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE donations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    doador_id BIGINT NOT NULL,
    campaign_id BIGINT NOT NULL,
    tipo VARCHAR(255) NOT NULL,
    item VARCHAR(255) NOT NULL,
    quantidade VARCHAR(255) NOT NULL,
    categoria VARCHAR(255),
    observacao TEXT,
    status VARCHAR(255) NOT NULL,
    data_intencao DATETIME,
    data_recebimento DATETIME,
    precisa_coleta BOOLEAN NOT NULL DEFAULT FALSE,
    endereco_coleta VARCHAR(300),
    CONSTRAINT fk_donation_doador FOREIGN KEY (doador_id) REFERENCES users (id),
    CONSTRAINT fk_donation_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE donation_updates (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    donation_id BIGINT NOT NULL,
    mensagem TEXT NOT NULL,
    foto_url VARCHAR(500),
    data_criacao DATETIME,
    CONSTRAINT fk_update_donation FOREIGN KEY (donation_id) REFERENCES donations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payments (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    doador_id BIGINT NOT NULL,
    instituicao_id BIGINT NOT NULL,
    campaign_id BIGINT,
    valor DECIMAL(10, 2) NOT NULL,
    metodo VARCHAR(20) NOT NULL,
    status VARCHAR(255) NOT NULL,
    comprovante_url VARCHAR(500),
    transacao_id VARCHAR(100),
    copia_e_cola VARCHAR(500),
    data_criacao DATETIME,
    data_confirmacao DATETIME,
    CONSTRAINT fk_payment_doador FOREIGN KEY (doador_id) REFERENCES users (id),
    CONSTRAINT fk_payment_instituicao FOREIGN KEY (instituicao_id) REFERENCES institution_profiles (id),
    CONSTRAINT fk_payment_campaign FOREIGN KEY (campaign_id) REFERENCES campaigns (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    mensagem VARCHAR(300) NOT NULL,
    link VARCHAR(200),
    lida BOOLEAN NOT NULL,
    data_criacao DATETIME,
    CONSTRAINT fk_notification_user FOREIGN KEY (usuario_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE password_reset_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(100) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    used BOOLEAN NOT NULL,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE contact_messages (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL,
    assunto VARCHAR(200),
    mensagem TEXT NOT NULL,
    lido BOOLEAN NOT NULL,
    data_envio DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE blog_posts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    slug VARCHAR(80) NOT NULL UNIQUE,
    conteudo TEXT NOT NULL,
    resumo VARCHAR(300),
    autor_id BIGINT NOT NULL,
    categoria VARCHAR(255),
    imagem_url VARCHAR(500),
    publicado BOOLEAN NOT NULL,
    data_criacao DATETIME,
    data_publicacao DATETIME,
    CONSTRAINT fk_post_autor FOREIGN KEY (autor_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ofertas (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    doador_id BIGINT NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT NOT NULL,
    categoria VARCHAR(255),
    estado_item VARCHAR(50),
    cidade VARCHAR(255),
    precisa_coleta BOOLEAN NOT NULL DEFAULT FALSE,
    endereco_coleta VARCHAR(300),
    disponivel_ate DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    instituicao_id BIGINT,
    reservada_em DATETIME,
    prazo_coleta DATE,
    data_entrega DATETIME,
    data_criacao DATETIME,
    aprovado BOOLEAN NOT NULL DEFAULT FALSE,
    motivo_recusa TEXT,
    CONSTRAINT fk_oferta_doador FOREIGN KEY (doador_id) REFERENCES users (id),
    CONSTRAINT fk_oferta_instituicao FOREIGN KEY (instituicao_id) REFERENCES institution_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE oferta_images (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    oferta_id BIGINT NOT NULL,
    filename VARCHAR(200) NOT NULL,
    legenda VARCHAR(200),
    ordem INTEGER,
    data_upload DATETIME,
    CONSTRAINT fk_oferta_image FOREIGN KEY (oferta_id) REFERENCES ofertas (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_campaign_ativo_data ON campaigns (ativo, data_criacao DESC);
CREATE INDEX idx_campaign_instituicao ON campaigns (instituicao_id);
CREATE INDEX idx_campaign_ativo_categoria ON campaigns (ativo, categoria);
CREATE INDEX idx_donation_doador ON donations (doador_id, data_intencao DESC);
CREATE INDEX idx_donation_campaign ON donations (campaign_id);
CREATE INDEX idx_donation_status ON donations (status);
CREATE INDEX idx_payment_doador ON payments (doador_id);
CREATE INDEX idx_payment_instituicao ON payments (instituicao_id);
CREATE INDEX idx_payment_status ON payments (status);
CREATE INDEX idx_notification_user_lida ON notifications (usuario_id, lida);
CREATE INDEX idx_institution_aprovado ON institution_profiles (aprovado);
CREATE INDEX idx_blog_publicado_categoria ON blog_posts (publicado, categoria);
CREATE INDEX idx_contact_lido ON contact_messages (lido);
CREATE INDEX idx_password_token_expiry ON password_reset_tokens (expiry_date);
CREATE INDEX idx_ofertas_status ON ofertas (status);
CREATE INDEX idx_ofertas_aprovado ON ofertas (aprovado);
CREATE INDEX idx_oferta_images_oferta ON oferta_images (oferta_id);
