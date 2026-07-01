from . import db


class ContactMessage(db.Model):
    __tablename__ = 'contact_messages'

    id = db.Column(db.Integer, primary_key=True)
    nome = db.Column(db.String(100), nullable=False)
    email = db.Column(db.String(120), nullable=False)
    assunto = db.Column(db.String(200))
    mensagem = db.Column(db.Text, nullable=False)
    lido = db.Column(db.Boolean, default=False)
    data_envio = db.Column(db.DateTime, default=db.func.now())
