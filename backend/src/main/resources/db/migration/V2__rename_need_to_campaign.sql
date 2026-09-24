-- Evolução arquitetural (Extensão III): domínio "necessidade" passa a "campanha".
-- Renomeia tabelas e colunas preservando todos os dados existentes.

ALTER TABLE needs RENAME TO campaigns;
ALTER TABLE need_images RENAME TO campaign_images;

ALTER TABLE campaign_images RENAME COLUMN need_id TO campaign_id;
ALTER TABLE donations RENAME COLUMN necessidade_id TO campaign_id;
ALTER TABLE payments RENAME COLUMN necessidade_id TO campaign_id;

ALTER TABLE campaign_images RENAME CONSTRAINT fk_image_need TO fk_image_campaign;
ALTER TABLE donations RENAME CONSTRAINT fk_donation_need TO fk_donation_campaign;
ALTER TABLE payments RENAME CONSTRAINT fk_payment_need TO fk_payment_campaign;
ALTER TABLE campaigns RENAME CONSTRAINT fk_need_institution TO fk_campaign_institution;
