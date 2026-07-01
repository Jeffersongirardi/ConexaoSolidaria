from . import db


class InstitutionProfile(db.Model):
    __tablename__ = 'institution_profiles'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False, unique=True)
    cnpj = db.Column(db.String(18), unique=True, nullable=False)
    razao_social = db.Column(db.String(200), nullable=False)
    endereco = db.Column(db.String(300))
    descricao = db.Column(db.Text)
    aprovado = db.Column(db.Boolean, default=False)
    data_cadastro = db.Column(db.DateTime, default=db.func.now())

    needs = db.relationship('Need', back_populates='institution', lazy='dynamic',
                            foreign_keys='Need.instituicao_id')
