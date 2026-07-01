from . import db
import uuid
from datetime import datetime


class Payment(db.Model):
    __tablename__ = 'payments'

    id = db.Column(db.Integer, primary_key=True)
    uuid = db.Column(db.String(36), unique=True, nullable=False, default=lambda: str(uuid.uuid4()))
    doador_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    instituicao_id = db.Column(db.Integer, db.ForeignKey('institution_profiles.id'), nullable=False)
    necessidade_id = db.Column(db.Integer, db.ForeignKey('needs.id'), nullable=True)
    valor = db.Column(db.Numeric(10, 2), nullable=False)
    metodo = db.Column(db.String(20), nullable=False)
    status = db.Column(db.String(20), default='pendente')
    comprovante_url = db.Column(db.String(500))
    transacao_id = db.Column(db.String(100))
    data_criacao = db.Column(db.DateTime, default=db.func.now())
    data_confirmacao = db.Column(db.DateTime)

    doador = db.relationship('User', backref='pagamentos', foreign_keys=[doador_id])
    instituicao = db.relationship('InstitutionProfile', backref='pagamentos', foreign_keys=[instituicao_id])
    necessidade = db.relationship('Need', backref='pagamentos', foreign_keys=[necessidade_id])

    STATUS_CHOICES = ('pendente', 'processando', 'confirmado', 'recusado', 'cancelado')
    METODO_CHOICES = ('pix', 'cartao_credito', 'boleto', 'transferencia')
