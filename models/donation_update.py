from . import db


class DonationUpdate(db.Model):
    __tablename__ = 'donation_updates'

    id = db.Column(db.Integer, primary_key=True)
    donation_id = db.Column(db.Integer, db.ForeignKey('donations.id'), nullable=False)
    mensagem = db.Column(db.Text, nullable=False)
    foto_url = db.Column(db.String(500))
    data_criacao = db.Column(db.DateTime, default=db.func.now())
