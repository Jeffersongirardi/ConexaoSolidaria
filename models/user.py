from flask_login import UserMixin
from . import db
from werkzeug.security import generate_password_hash, check_password_hash


class User(UserMixin, db.Model):
    __tablename__ = 'users'

    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(120), unique=True, nullable=False, index=True)
    password_hash = db.Column(db.String(256), nullable=False)
    nome = db.Column(db.String(100), nullable=False)
    cpf = db.Column(db.String(14))
    data_nascimento = db.Column(db.Date)
    telefone = db.Column(db.String(20))
    whatsapp = db.Column(db.String(20))
    cep = db.Column(db.String(9))
    cidade = db.Column(db.String(50))
    estado = db.Column(db.String(2))
    tipo = db.Column(db.String(20), nullable=False)
    avatar_url = db.Column(db.String(500))
    ativo = db.Column(db.Boolean, default=True)
    data_cadastro = db.Column(db.DateTime, default=db.func.now())

    institution_profile = db.relationship('InstitutionProfile', uselist=False, backref='user')
    notifications = db.relationship('Notification', backref='user', lazy='dynamic',
                                    foreign_keys='Notification.usuario_id',
                                    order_by='Notification.data_criacao.desc()')

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)
