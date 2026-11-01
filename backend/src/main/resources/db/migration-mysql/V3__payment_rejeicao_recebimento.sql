-- P0.6: rejeição da instituição, cancelamento pelo admin e data real de recebimento.
ALTER TABLE payments ADD COLUMN motivo_rejeicao VARCHAR(300);
ALTER TABLE payments ADD COLUMN motivo_cancelamento VARCHAR(300);
ALTER TABLE payments ADD COLUMN data_recebimento DATETIME;
