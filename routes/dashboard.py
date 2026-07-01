from flask import Blueprint, render_template, redirect, url_for, request, flash, jsonify
from flask_login import login_required, current_user
from models import db, Need, Donation, Institution

dashboard_bp = Blueprint('dashboard', __name__, url_prefix='/dashboard')


@dashboard_bp.route('/doador')
@login_required
def doador():
    if current_user.tipo != 'doador':
        return redirect(url_for('main.index'))
    doacoes = Donation.query.filter_by(doador_id=current_user.id)\
        .order_by(Donation.data_intencao.desc()).all()
    return render_template('dashboard-doador.html', doacoes=doacoes)


@dashboard_bp.route('/instituicao')
@login_required
def instituicao():
    if current_user.tipo != 'instituicao':
        return redirect(url_for('main.index'))
    inst = Institution.query.get(current_user.id)
    if not inst.aprovado:
        return render_template('dashboard-instituicao.html', pendente=True)
    needs = Need.query.filter_by(instituicao_id=inst.id).order_by(Need.data_criacao.desc()).all()
    return render_template('dashboard-instituicao.html', needs=needs)


@dashboard_bp.route('/need/criar', methods=['POST'])
@login_required
def criar_need():
    if current_user.tipo != 'instituicao':
        return jsonify({'erro': 'Acesso negado'}), 403
    need = Need(
        instituicao_id=current_user.id,
        titulo=request.form.get('titulo'),
        descricao=request.form.get('descricao'),
        quantidade_alvo=request.form.get('quantidade'),
        urgencia=request.form.get('urgencia', 'media')
    )
    db.session.add(need)
    db.session.commit()
    flash('Necessidade criada com sucesso!', 'success')
    return redirect(url_for('dashboard.instituicao'))


@dashboard_bp.route('/doar/<int:need_id>', methods=['POST'])
@login_required
def doar(need_id):
    if current_user.tipo != 'doador':
        return jsonify({'erro': 'Acesso negado'}), 403
    need = Need.query.get_or_404(need_id)
    donation = Donation(
        doador_id=current_user.id,
        necessidade_id=need.id,
        item=request.form.get('item'),
        quantidade=request.form.get('quantidade')
    )
    db.session.add(donation)
    db.session.commit()
    flash('Intenção de doação registrada!', 'success')
    return redirect(url_for('dashboard.doador'))


@dashboard_bp.route('/confirmar/<int:donation_id>')
@login_required
def confirmar_recebimento(donation_id):
    donation = Donation.query.get_or_404(donation_id)
    need = donation.need
    if need.instituicao_id != current_user.id:
        return jsonify({'erro': 'Acesso negado'}), 403
    donation.status = 'recebido'
    donation.data_recebimento = db.func.now()
    need.progresso = min(100, need.progresso + 10)
    db.session.commit()
    flash('Doação confirmada como recebida!', 'success')
    return redirect(url_for('dashboard.instituicao'))
