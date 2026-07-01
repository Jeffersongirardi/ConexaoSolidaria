import re
from . import db


def slugify(text):
    text = text.lower().strip()
    text = re.sub(r'[^\w\s-]', '', text)
    text = re.sub(r'[-\s]+', '-', text)
    return text[:80]


class BlogPost(db.Model):
    __tablename__ = 'blog_posts'

    id = db.Column(db.Integer, primary_key=True)
    titulo = db.Column(db.String(200), nullable=False)
    slug = db.Column(db.String(80), unique=True, nullable=False, index=True)
    conteudo = db.Column(db.Text, nullable=False)
    resumo = db.Column(db.String(300))
    autor_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    categoria = db.Column(db.String(50), default='geral')
    imagem_url = db.Column(db.String(500))
    publicado = db.Column(db.Boolean, default=False)
    data_criacao = db.Column(db.DateTime, default=db.func.now())
    data_publicacao = db.Column(db.DateTime)

    autor = db.relationship('User', backref='posts')

    def save(self, commit=True):
        if not self.slug:
            base = slugify(self.titulo)
            slug = base
            counter = 1
            while BlogPost.query.filter_by(slug=slug).first():
                slug = f'{base}-{counter}'
                counter += 1
            self.slug = slug
        db.session.add(self)
        if commit:
            db.session.commit()
