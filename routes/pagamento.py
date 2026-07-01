import os
from io import BytesIO
from flask import Blueprint, render_template, request, redirect, url_for, flash, jsonify, current_app
from flask_login import login_required, current_user
from models import db, Payment, Donation, Need, Notification
from uuid import uuid4

try:
    import qrcode
    QRCODE_AVAILABLE = True
except ImportError:
    qrcode = None
    QRCODE_AVAILABLE = False

pagamento_bp = Blueprint('pagamento', __name__, url_prefix='/pagamento')


def notificar(usuario_id, tipo, mensagem, link=None):
    notif = Notification(usuario_id=usuario_id, tipo=tipo, mensagem=mensagem, link=link)
    db.session.add(notif)


def gerar_qrcode_pix(pix_key, valor):
    if not QRCODE_AVAILABLE:
        return None
    try:
        img = qrcode.make(f'pix://{pix_key}?amount={valor}')
        buf = BytesIO()
        img.save(buf, format='PNG')
        import base64
        return f'data:image/png;base64,{base64.b64encode(buf.getvalue()).decode()}'
    except Exception:
        return None


@pagamento_bp.route('/doar-pix/<uuid>', methods=['GET', 'POST'])
@login_required
def doar_pix(uuid):
    payment = Payment.query.filter_by(uuid=uuid).first_or_404()
    if payment.doador_id != current_user.id:
        return jsonify({'erro': 'Acesso negado'}), 403
    if payment.status != 'pendente':
        flash('Este pagamento já foi processado.', 'error')
        return redirect(url_for('dashboard.doador'))

    pix_key = payment.instituicao.pix_key or 'chave@exemplo.org'
    pix_qrcode = gerar_qrcode_pix(pix_key, payment.valor)

    return render_template('pagamento-pix.html', payment=payment, pix_key=pix_key, pix_qrcode=pix_qrcode)


@pagamento_bp.route('/doar-cartao/<uuid>', methods=['GET', 'POST'])
@login_required
def doar_cartao(uuid):
    payment = Payment.query.filter_by(uuid=uuid).first_or_404()
    if payment.doador_id != current_user.id:
        return jsonify({'erro': 'Acesso negado'}), 403
    if payment.status != 'pendente':
        flash('Este pagamento já foi processado.', 'error')
        return redirect(url_for('dashboard.doador'))

    if request.method == 'POST':
        payment.status = 'processando'
        payment.transacao_id = f'CARD-{uuid4().hex[:12].upper()}'
        db.session.commit()

        payment.status = 'confirmado'
        payment.data_confirmacao = db.func.now()
        need = payment.necessidade
        if need:
            need.progresso = min(100, need.progresso + 5)
        notificar(payment.instituicao_id, 'nova_doacao',
                  f'{current_user.nome} contribuiu com R$ {payment.valor} via cartão.',
                  url_for('dashboard.instituicao'))
        db.session.commit()
        flash('Pagamento com cartão confirmado!', 'success')
        return redirect(url_for('dashboard.doador'))

    return render_template('pagamento-cartao.html', payment=payment)


@pagamento_bp.route('/doar-transferencia/<uuid>', methods=['GET', 'POST'])
@login_required
def doar_transferencia(uuid):
    payment = Payment.query.filter_by(uuid=uuid).first_or_404()
    if payment.doador_id != current_user.id:
        return jsonify({'erro': 'Acesso negado'}), 403
    if payment.status != 'pendente':
        flash('Este pagamento já foi processado.', 'error')
        return redirect(url_for('dashboard.doador'))

    if request.method == 'POST':
        comprovante = request.files.get('comprovante')
        if comprovante and comprovante.filename:
            from routes.upload import save_upload
            path = save_upload(comprovante, 'comprovantes')
            if path:
                payment.comprovante_url = url_for('static', filename=path)
        payment.status = 'processando'
        payment.transacao_id = f'TRANSF-{uuid4().hex[:12].upper()}'
        db.session.commit()

        need = payment.necessidade
        notificar(payment.instituicao_id, 'nova_doacao',
                  f'{current_user.nome} enviou transferência de R$ {payment.valor}.',
                  url_for('dashboard.instituicao'))
        db.session.commit()
        flash('Comprovante enviado! Pagamento será processado.', 'success')
        return redirect(url_for('dashboard.doador'))

    return render_template('pagamento-transferencia.html', payment=payment)


@pagamento_bp.route('/confirmar/<uuid>')
@login_required
def confirmar_pagamento(uuid):
    payment = Payment.query.filter_by(uuid=uuid).first_or_404()
    if payment.status != 'processando':
        flash('Pagamento não está em processamento.', 'error')
        return redirect(url_for('dashboard.doador'))
    payment.status = 'confirmado'
    payment.data_confirmacao = db.func.now()
    need = payment.necessidade
    if need:
        need.progresso = min(100, need.progresso + 5)
    db.session.commit()
    flash('Pagamento confirmado!', 'success')
    return redirect(url_for('dashboard.doador'))


@pagamento_bp.route('/sucesso/<uuid>')
@login_required
def pagamento_sucesso(uuid):
    payment = Payment.query.filter_by(uuid=uuid).first_or_404()
    return render_template('pagamento-sucesso.html', payment=payment)
