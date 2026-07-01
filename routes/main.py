from flask import Blueprint, render_template, request, flash, redirect, url_for
from models import Need, Donation, InstitutionProfile, ContactMessage, db

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
    if request.method == 'POST':
        msg = ContactMessage(
            nome=request.form.get('nome'),
            email=request.form.get('email'),
            assunto=request.form.get('assunto'),
            mensagem=request.form.get('mensagem'),
        )
        db.session.add(msg)
        db.session.commit()
        flash('Mensagem enviada com sucesso! Entraremos em contato em breve.', 'success')
        return redirect(url_for('main.contato'))
    return render_template('contato.html')


@main_bp.route('/privacidade')
def privacidade():
    return render_template('privacidade.html')
