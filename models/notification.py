from . import db


class Notification(db.Model):
    __tablename__ = 'notifications'

    id = db.Column(db.Integer, primary_key=True)
    usuario_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    tipo = db.Column(db.String(30), nullable=False)
    mensagem = db.Column(db.String(300), nullable=False)
    link = db.Column(db.String(200))
    lida = db.Column(db.Boolean, default=False)
    data_criacao = db.Column(db.DateTime, default=db.func.now())
