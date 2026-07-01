from flask import Blueprint, render_template, redirect, url_for, jsonify
from flask_login import login_required, current_user
from models import db, Notification

notif_bp = Blueprint('notificacoes', __name__, url_prefix='/notificacoes')


@notif_bp.route('/')
@login_required
def listar():
    notificacoes = Notification.query.filter_by(usuario_id=current_user.id)\
        .order_by(Notification.data_criacao.desc()).all()
    return render_template('notificacoes.html', notificacoes=notificacoes)


@notif_bp.route('/ler/<int:id>')
@login_required
def ler(id):
    notif = Notification.query.get_or_404(id)
    if notif.usuario_id != current_user.id:
        return jsonify({'erro': 'Acesso negado'}), 403
    notif.lida = True
    db.session.commit()
    if notif.link:
        return redirect(notif.link)
    return redirect(url_for('notificacoes.listar'))


@notif_bp.route('/ler-todas')
@login_required
def ler_todas():
    Notification.query.filter_by(usuario_id=current_user.id, lida=False)\
        .update({'lida': True})
    db.session.commit()
    return redirect(url_for('notificacoes.listar'))


@notif_bp.route('/nao-lidas')
@login_required
def nao_lidas_count():
    count = Notification.query.filter_by(usuario_id=current_user.id, lida=False).count()
    return jsonify({'count': count})
