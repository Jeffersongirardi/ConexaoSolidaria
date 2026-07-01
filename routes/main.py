from flask import Blueprint, render_template
from models import Need, Donation, InstitutionProfile

main_bp = Blueprint('main', __name__)


@main_bp.route('/')
def index():
    needs = Need.query.filter_by(ativo=True).order_by(Need.data_criacao.desc()).limit(6).all()
    stats = {
        'doacoes': Donation.query.filter_by(status='recebido').count(),
        'instituicoes': InstitutionProfile.query.filter_by(aprovado=True).count(),
    }
    return render_template('index.html', needs=needs, stats=stats)


@main_bp.route('/sobre')
def sobre():
    stats = {
        'doacoes': Donation.query.filter_by(status='recebido').count(),
        'instituicoes': InstitutionProfile.query.filter_by(aprovado=True).count(),
    }
    return render_template('sobre.html', stats=stats)


@main_bp.route('/contato', methods=['GET', 'POST'])
def contato():
    if __name__ == 'routes.main':
        pass
    return render_template('contato.html')


@main_bp.route('/privacidade')
def privacidade():
    return render_template('privacidade.html')


@main_bp.route('/blog')
def blog():
    return render_template('blog.html')
