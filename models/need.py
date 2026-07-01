from . import db


class Need(db.Model):
    __tablename__ = 'needs'

    id = db.Column(db.Integer, primary_key=True)
    instituicao_id = db.Column(db.Integer, db.ForeignKey('institution_profiles.id'), nullable=False)
    titulo = db.Column(db.String(200), nullable=False)
    descricao = db.Column(db.Text, nullable=False)
    categoria = db.Column(db.String(50), default='outro')
    quantidade_alvo = db.Column(db.String(50), nullable=False)
    urgencia = db.Column(db.String(20), default='media')
    aceita_financeiro = db.Column(db.Boolean, default=False)
    progresso = db.Column(db.Integer, default=0)
    ativo = db.Column(db.Boolean, default=True)
    data_criacao = db.Column(db.DateTime, default=db.func.now())
    data_encerramento = db.Column(db.DateTime)

    institution = db.relationship('InstitutionProfile', back_populates='needs')
    donations = db.relationship('Donation', backref='need', lazy='dynamic',
                                foreign_keys='Donation.necessidade_id')
    images = db.relationship('NeedImage', backref='need', lazy='dynamic',
                             foreign_keys='NeedImage.need_id',
                             order_by='NeedImage.ordem')

    def progresso_percentual(self):
        return min(100, self.progresso)
