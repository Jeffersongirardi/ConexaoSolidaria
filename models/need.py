from . import db


class Need(db.Model):
    __tablename__ = 'needs'

    id = db.Column(db.Integer, primary_key=True)
    instituicao_id = db.Column(db.Integer, db.ForeignKey('institutions.id'), nullable=False)
    titulo = db.Column(db.String(200), nullable=False)
    descricao = db.Column(db.Text, nullable=False)
    quantidade_alvo = db.Column(db.String(50), nullable=False)
    urgencia = db.Column(db.String(20), default='media')
    progresso = db.Column(db.Integer, default=0)
    ativo = db.Column(db.Boolean, default=True)
    data_criacao = db.Column(db.DateTime, default=db.func.now())
    data_encerramento = db.Column(db.DateTime)

    donations = db.relationship('Donation', backref='need', lazy='dynamic')
