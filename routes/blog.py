from flask import Blueprint, render_template, abort, request
from models import db, BlogPost

blog_bp = Blueprint('blog_publico', __name__, url_prefix='/blog')


@blog_bp.route('/')
def listar():
    page = request.args.get('page', 1, type=int)
    categoria = request.args.get('categoria')
    query = BlogPost.query.filter_by(publicado=True)
    if categoria:
        query = query.filter_by(categoria=categoria)
    pagination = query.order_by(BlogPost.data_publicacao.desc())\
        .paginate(page=page, per_page=6, error_out=False)
    categorias_query = db.session.query(BlogPost.categoria).filter(BlogPost.publicado == True).distinct().all()
    return render_template('blog-publico.html', pagination=pagination, categorias=[c[0] for c in categorias_query])


@blog_bp.route('/<slug>')
def detalhe(slug):
    post = BlogPost.query.filter_by(slug=slug, publicado=True).first_or_404()
    return render_template('blog-post.html', post=post)
