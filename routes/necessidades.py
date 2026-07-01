from flask import Blueprint, render_template, request, abort
from models import Need, InstitutionProfile

n_bp = Blueprint('necessidades', __name__, url_prefix='/necessidades')


@n_bp.route('/')
def listar():
    query = Need.query.filter_by(ativo=True)
    categoria = request.args.get('categoria')
    urgencia = request.args.get('urgencia')
    busca = request.args.get('busca')

    if categoria and categoria != 'todas':
        query = query.filter_by(categoria=categoria)
    if urgencia and urgencia != 'todas':
        query = query.filter_by(urgencia=urgencia)
    if busca:
        query = query.filter(
            Need.titulo.ilike(f'%{busca}%') |
            Need.descricao.ilike(f'%{busca}%')
        )

    page = request.args.get('page', 1, type=int)
    pagination = query.order_by(Need.data_criacao.desc()).paginate(
        page=page, per_page=12, error_out=False)

    categorias = ['alimento', 'roupa', 'higiene', 'material_escolar', 'outro']
    return render_template('necessidades-listar.html',
                           pagination=pagination,
                           categorias=categorias,
                           filtros={'categoria': categoria, 'urgencia': urgencia, 'busca': busca})


@n_bp.route('/<int:id>')
def detalhe(id):
    need = Need.query.get_or_404(id)
    institution = need.institution
    images = need.images.all()
    return render_template('necessidades-detalhe.html', need=need, institution=institution, images=images)
