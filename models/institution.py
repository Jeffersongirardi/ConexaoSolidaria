from . import db


class InstitutionProfile(db.Model):
    __tablename__ = 'institution_profiles'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False, unique=True)
    cnpj = db.Column(db.String(18), unique=True, nullable=False)
    razao_social = db.Column(db.String(200), nullable=False)
    nome_fantasia = db.Column(db.String(200))
    endereco = db.Column(db.String(300))
    website = db.Column(db.String(200))
    foto_url = db.Column(db.String(500))
    descricao = db.Column(db.Text)
    categoria_atuacao = db.Column(db.String(100))
    whatsapp = db.Column(db.String(20))
    pix_key = db.Column(db.String(100))
    pix_titular = db.Column(db.String(100))
    aprovado = db.Column(db.Boolean, default=False)
    data_cadastro = db.Column(db.DateTime, default=db.func.now())
    motivo_recusa = db.Column(db.Text)

    needs = db.relationship('Need', back_populates='institution', lazy='dynamic',
                            foreign_keys='Need.instituicao_id')
