from . import db


class NeedImage(db.Model):
    __tablename__ = 'need_images'

    id = db.Column(db.Integer, primary_key=True)
    need_id = db.Column(db.Integer, db.ForeignKey('needs.id'), nullable=False)
    filename = db.Column(db.String(200), nullable=False)
    legenda = db.Column(db.String(200))
    ordem = db.Column(db.Integer, default=0)
    data_upload = db.Column(db.DateTime, default=db.func.now())
