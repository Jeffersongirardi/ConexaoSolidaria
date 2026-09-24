-- Índices para queries frequentes (prod + dev)
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
