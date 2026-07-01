from flask import Blueprint, render_template, redirect, url_for, request, flash, jsonify, current_app
from flask_login import login_required, current_user
from models import db, Need, Donation, InstitutionProfile, Notification, NeedImage, DonationUpdate
import os
from werkzeug.utils import secure_filename
from uuid import uuid4

dashboard_bp = Blueprint('dashboard', __name__, url_prefix='/dashboard')

ALLOWED_EXT = {'png', 'jpg', 'jpeg', 'gif', 'webp'}


def notificar(usuario_id, tipo, mensagem, link=None):
    notif = Notification(usuario_id=usuario_id, tipo=tipo, mensagem=mensagem, link=link)
    db.session.add(notif)


def save_upload(file, subdir='needs'):
    ext = file.filename.rsplit('.', 1)[-1].lower() if '.' in file.filename else ''
    if ext not in ALLOWED_EXT:
        return None
    name = f'{uuid4().hex}.{ext}'
    path = os.path.join(current_app.root_path, 'static', 'uploads', subdir)
    os.makedirs(path, exist_ok=True)
    file.save(os.path.join(path, name))
    return url_for('static', filename=f'uploads/{subdir}/{name}')


# ─── Doador ───
@dashboard_bp.route('/doador')
@login_required
def doador():
    if current_user.tipo != 'doador':
        return redirect(url_for('main.index'))
    doacoes = Donation.query.filter_by(doador_id=current_user.id)\
        .order_by(Donation.data_intencao.desc()).all()
    pagamentos = Payment.query.filter_by(doador_id=current_user.id)\
        .order_by(Payment.data_criacao.desc()).all() if has_payment_model() else []
    stats = {
        'total': len(doacoes),
        'recebidas': sum(1 for d in doacoes if d.status == 'recebido'),
        'pendentes': sum(1 for d in doacoes if d.status == 'pendente'),
    }
    return render_template('dashboard-doador.html', doacoes=doacoes, pagamentos=pagamentos, stats=stats)


# ─── Instituicao ───
@dashboard_bp.route('/instituicao')
@login_required
def instituicao():
    if current_user.tipo != 'instituicao':
        return redirect(url_for('main.index'))
    profile = current_user.institution_profile
    if not profile or not profile.aprovado:
        return render_template('dashboard-instituicao.html', pendente=True, profile=profile)

    needs = Need.query.filter_by(instituicao_id=profile.id)\
        .order_by(Need.data_criacao.desc()).all()
    need_ids = [n.id for n in needs]
    doacoes = Donation.query.filter(Donation.necessidade_id.in_(need_ids))\
        .order_by(Donation.data_intencao.desc()).all() if need_ids else []
    pagamentos = Payment.query.filter_by(instituicao_id=profile.id)\
        .order_by(Payment.data_criacao.desc()).all() if has_payment_model() else []

    stats = {
        'total_doacoes': len(doacoes) + len(pagamentos),
        'recebidas': sum(1 for d in doacoes if d.status == 'recebido') + sum(1 for p in pagamentos if p.status == 'confirmado'),
        'pendentes': sum(1 for d in doacoes if d.status == 'pendente') + sum(1 for p in pagamentos if p.status == 'pendente'),
        'necessidades_ativas': sum(1 for n in needs if n.ativo),
    }
    return render_template('dashboard-instituicao.html', needs=needs, doacoes=doacoes, pagamentos=pagamentos, stats=stats)


def has_payment_model():
    try:
        from models import Payment
        return True
    except ImportError:
        return False


# ─── Criar Necessidade ───
@dashboard_bp.route('/need/criar', methods=['POST'])
@login_required
def criar_need():
    if current_user.tipo != 'instituicao':
        return jsonify({'erro': 'Acesso negado'}), 403
    profile = current_user.institution_profile
    need = Need(
        instituicao_id=profile.id,
        titulo=request.form.get('titulo'),
        descricao=request.form.get('descricao'),
        categoria=request.form.get('categoria', 'outro'),
        quantidade_alvo=request.form.get('quantidade'),
        urgencia=request.form.get('urgencia', 'media'),
        aceita_financeiro=bool(request.form.get('aceita_financeiro'))
    )
    db.session.add(need)
    db.session.flush()

    for file in request.files.getlist('imagens'):
        if file and file.filename:
            url = save_upload(file)
            if url:
                img = NeedImage(need_id=need.id, filename=url)
                db.session.add(img)

    db.session.commit()
    flash('Necessidade criada com sucesso!', 'success')
    return redirect(url_for('dashboard.instituicao'))


# ─── Editar Necessidade ───
@dashboard_bp.route('/need/<int:id>/editar', methods=['GET', 'POST'])
@login_required
def editar_need(id):
    need = Need.query.get_or_404(id)
    profile = current_user.institution_profile
    if need.instituicao_id != profile.id:
        return jsonify({'erro': 'Acesso negado'}), 403

    if request.method == 'POST':
        need.titulo = request.form.get('titulo')
        need.descricao = request.form.get('descricao')
        need.categoria = request.form.get('categoria', 'outro')
        need.quantidade_alvo = request.form.get('quantidade')
        need.urgencia = request.form.get('urgencia', 'media')
        need.aceita_financeiro = bool(request.form.get('aceita_financeiro'))

        for file in request.files.getlist('imagens'):
            if file and file.filename:
                url = save_upload(file)
                if url:
                    max_ordem = db.session.query(db.func.max(NeedImage.ordem)).filter_by(need_id=need.id).scalar() or 0
                    img = NeedImage(need_id=need.id, filename=url, ordem=max_ordem + 1)
                    db.session.add(img)

        db.session.commit()
        flash('Necessidade atualizada!', 'success')
        return redirect(url_for('dashboard.instituicao'))

    return render_template('need-editar.html', need=need)


@dashboard_bp.route('/need/<int:id>/toggle')
@login_required
def toggle_need(id):
    need = Need.query.get_or_404(id)
    profile = current_user.institution_profile
    if need.instituicao_id != profile.id:
        return jsonify({'erro': 'Acesso negado'}), 403
    need.ativo = not need.ativo
    if not need.ativo:
        need.data_encerramento = db.func.now()
    else:
        need.data_encerramento = None
    db.session.commit()
    flash(f'Necessidade {"desativada" if not need.ativo else "reativada"}.', 'success')
    return redirect(url_for('dashboard.instituicao'))


@dashboard_bp.route('/need/imagem/<int:img_id>/remover', methods=['POST'])
@login_required
def remover_imagem(img_id):
    img = NeedImage.query.get_or_404(img_id)
    need = img.need
    if need.instituicao_id != current_user.institution_profile.id:
        return jsonify({'erro': 'Acesso negado'}), 403
    db.session.delete(img)
    db.session.commit()
    flash('Imagem removida.', 'info')
    return redirect(url_for('dashboard.editar_need', id=need.id))


# ─── Doar Item Físico ───
@dashboard_bp.route('/doar/<int:need_id>', methods=['POST'])
@login_required
def doar(need_id):
    if current_user.tipo != 'doador':
        return jsonify({'erro': 'Acesso negado'}), 403
    need = Need.query.get_or_404(need_id)
    if not need.ativo:
        flash('Esta necessidade não está mais ativa.', 'error')
        return redirect(url_for('necessidades.detalhe', id=need_id))

    donation = Donation(
        doador_id=current_user.id,
        necessidade_id=need.id,
        tipo='fisico',
        item=request.form.get('item'),
        quantidade=request.form.get('quantidade'),
        categoria=request.form.get('categoria', 'outro'),
        observacao=request.form.get('observacao', '')
    )
    db.session.add(donation)
    db.session.flush()

    notificar(
        need.institution.user_id,
        'nova_doacao',
        f'{current_user.nome} quer doar {donation.quantidade} de {donation.item} para "{need.titulo}"',
        url_for('dashboard.instituicao')
    )
    db.session.commit()
    flash('Intenção de doação registrada!', 'success')
    return redirect(url_for('dashboard.doador'))


# ─── Doação Financeira (fake gateway) ───
@dashboard_bp.route('/doar-financeiro/<int:need_id>', methods=['POST'])
@login_required
def doar_financeiro(need_id):
    from models import Payment, InstitutionProfile
    if current_user.tipo != 'doador':
        return jsonify({'erro': 'Acesso negado'}), 403
    need = Need.query.get_or_404(need_id)
    if not need.ativo:
        flash('Necessidade inativa.', 'error')
        return redirect(url_for('necessidades.detalhe', id=need_id))

    valor = request.form.get('valor', type=float)
    metodo = request.form.get('metodo', 'pix')
    if not valor or valor <= 0:
        flash('Valor inválido.', 'error')
        return redirect(url_for('necessidades.detalhe', id=need_id))

    payment = Payment(
        doador_id=current_user.id,
        instituicao_id=need.instituicao_id,
        necessidade_id=need.id,
        valor=valor,
        metodo=metodo,
        status='pendente'
    )
    db.session.add(payment)
    db.session.flush()

    return redirect(url_for('dashboard.pagamento_fake', payment_uuid=payment.uuid))


@dashboard_bp.route('/pagamento/<payment_uuid>')
@login_required
def pagamento_fake(payment_uuid):
    from models import Payment
    payment = Payment.query.filter_by(uuid=payment_uuid).first_or_404()
    if payment.doador_id != current_user.id:
        return jsonify({'erro': 'Acesso negado'}), 403
    return render_template('pagamento-fake.html', payment=payment)


@dashboard_bp.route('/pagamento/<payment_uuid>/confirmar', methods=['POST'])
@login_required
def confirmar_pagamento_fake(payment_uuid):
    from models import Payment
    payment = Payment.query.filter_by(uuid=payment_uuid).first_or_404()
    if payment.doador_id != current_user.id:
        return jsonify({'erro': 'Acesso negado'}), 403
    if payment.status != 'pendente':
        flash('Pagamento já processado.', 'error')
        return redirect(url_for('dashboard.doador'))

    payment.status = 'processando'
    payment.transacao_id = f'FAKE-{uuid4().hex[:12].upper()}'
    db.session.commit()

    import time
    time.sleep(1)

    payment.status = 'confirmado'
    payment.data_confirmacao = db.func.now()
    db.session.flush()

    need = payment.necessidade
    if need:
        need.progresso = min(100, need.progresso + 5)

    notificar(
        payment.instituicao_id,
        'nova_doacao',
        f'{current_user.nome} contribuiu com R$ {payment.valor} via {payment.metodo} para "{need.titulo if need else "doação"}".',
        url_for('dashboard.instituicao')
    )
    db.session.commit()
    flash('Pagamento confirmado com sucesso!', 'success')
    return redirect(url_for('dashboard.doador'))


# ─── Confirmar Recebimento Físico ───
@dashboard_bp.route('/confirmar/<int:donation_id>')
@login_required
def confirmar_recebimento(donation_id):
    donation = Donation.query.get_or_404(donation_id)
    need = donation.need
    profile = current_user.institution_profile
    if need.instituicao_id != profile.id:
        return jsonify({'erro': 'Acesso negado'}), 403
    donation.status = 'recebido'
    donation.data_recebimento = db.func.now()
    need.progresso = min(100, need.progresso + 10)
    db.session.flush()

    notificar(
        donation.doador_id,
        'doacao_confirmada',
        f'Sua doação de {donation.quantidade} de {donation.item} foi confirmada como recebida por {profile.razao_social}!',
        url_for('dashboard.doador')
    )
    db.session.commit()
    flash('Doação confirmada como recebida!', 'success')
    return redirect(url_for('dashboard.instituicao'))


@dashboard_bp.route('/cancelar/<int:donation_id>')
@login_required
def cancelar_doacao(donation_id):
    donation = Donation.query.get_or_404(donation_id)
    if donation.doador_id != current_user.id:
        return jsonify({'erro': 'Acesso negado'}), 403
    if donation.status != 'pendente':
        flash('Só é possível cancelar doações pendentes.', 'error')
        return redirect(url_for('dashboard.doador'))
    donation.status = 'cancelado'
    db.session.commit()
    flash('Doação cancelada.', 'info')
    return redirect(url_for('dashboard.doador'))


# ─── Destino / Atualização da Doação ───
@dashboard_bp.route('/doacao/<int:donation_id>/atualizar', methods=['POST'])
@login_required
def atualizar_doacao(donation_id):
    donation = Donation.query.get_or_404(donation_id)
    need = donation.need
    profile = current_user.institution_profile
    if need.instituicao_id != profile.id:
        return jsonify({'erro': 'Acesso negado'}), 403

    mensagem = request.form.get('mensagem')
    update = DonationUpdate(donation_id=donation.id, mensagem=mensagem)

    if 'foto' in request.files:
        url = save_upload(request.files['foto'], 'updates')
        if url:
            update.foto_url = url

    db.session.add(update)
    db.session.commit()
    flash('Atualização publicada!', 'success')
    return redirect(url_for('dashboard.instituicao'))
