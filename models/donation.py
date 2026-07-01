from . import db


class Donation(db.Model):
    __tablename__ = 'donations'

    id = db.Column(db.Integer, primary_key=True)
    doador_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    necessidade_id = db.Column(db.Integer, db.ForeignKey('needs.id'), nullable=False)
    tipo = db.Column(db.String(20), default='fisico')
    item = db.Column(db.String(200), nullable=False)
    quantidade = db.Column(db.String(50), nullable=False)
    categoria = db.Column(db.String(50), default='outro')
    observacao = db.Column(db.Text)
    status = db.Column(db.String(20), default='pendente')
    data_intencao = db.Column(db.DateTime, default=db.func.now())
    data_recebimento = db.Column(db.DateTime)

    doador = db.relationship('User', backref='doacoes', foreign_keys=[doador_id])
    updates = db.relationship('DonationUpdate', backref='donation', lazy='dynamic',
                              foreign_keys='DonationUpdate.donation_id',
                              order_by='DonationUpdate.data_criacao.desc()')
