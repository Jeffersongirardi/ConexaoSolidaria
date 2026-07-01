from . import db
from .user import User


class Institution(User):
    __tablename__ = 'institutions'

    id = db.Column(db.Integer, db.ForeignKey('users.id'), primary_key=True)
    cnpj = db.Column(db.String(18), unique=True, nullable=False)
    razao_social = db.Column(db.String(200), nullable=False)
    endereco = db.Column(db.String(300))
    descricao = db.Column(db.Text)
    aprovado = db.Column(db.Boolean, default=False)

    __mapper_args__ = {
        'polymorphic_identity': 'instituicao',
    }

    needs = db.relationship('Need', backref='institution', lazy='dynamic')
