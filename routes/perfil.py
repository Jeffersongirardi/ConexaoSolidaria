from flask import Blueprint, render_template, redirect, url_for, request, flash, current_app
from flask_login import login_required, current_user
from models import db
import os
from werkzeug.utils import secure_filename
from uuid import uuid4

perfil_bp = Blueprint('perfil', __name__, url_prefix='/perfil')


@perfil_bp.route('/', methods=['GET', 'POST'])
@login_required
def editar():
    if request.method == 'POST':
        current_user.nome = request.form.get('nome', current_user.nome)
        current_user.telefone = request.form.get('telefone', current_user.telefone)
        current_user.whatsapp = request.form.get('whatsapp', current_user.whatsapp)
        current_user.cep = request.form.get('cep', current_user.cep)
        current_user.cidade = request.form.get('cidade', current_user.cidade)
        current_user.estado = request.form.get('estado', current_user.estado)

        if current_user.tipo == 'instituicao' and current_user.institution_profile:
            p = current_user.institution_profile
            p.razao_social = request.form.get('razao_social', p.razao_social)
            p.nome_fantasia = request.form.get('nome_fantasia', p.nome_fantasia)
            p.endereco = request.form.get('endereco', p.endereco)
            p.descricao = request.form.get('descricao', p.descricao)
            p.website = request.form.get('website', p.website)
            p.categoria_atuacao = request.form.get('categoria_atuacao', p.categoria_atuacao)
            p.whatsapp = request.form.get('whatsapp_inst', p.whatsapp)
            p.pix_key = request.form.get('pix_key', p.pix_key)
            p.pix_titular = request.form.get('pix_titular', p.pix_titular)

        if 'avatar' in request.files and request.files['avatar'].filename:
            file = request.files['avatar']
            ext = file.filename.rsplit('.', 1)[-1].lower() if '.' in file.filename else ''
            if ext in {'png', 'jpg', 'jpeg', 'gif', 'webp'}:
                name = f'avatar-{uuid4().hex}.{ext}'
                path = os.path.join(current_app.root_path, 'static', 'uploads', 'avatars')
                os.makedirs(path, exist_ok=True)
                file.save(os.path.join(path, name))
                current_user.avatar_url = url_for('static', filename=f'uploads/avatars/{name}')

        db.session.commit()
        flash('Perfil atualizado!', 'success')
        return redirect(url_for('perfil.editar'))
    return render_template('perfil.html')


@perfil_bp.route('/alterar-senha', methods=['GET', 'POST'])
@login_required
def alterar_senha():
    if request.method == 'POST':
        senha_atual = request.form.get('senha_atual')
        nova_senha = request.form.get('nova_senha')
        confirmar = request.form.get('confirmar_senha')
        if not current_user.check_password(senha_atual):
            flash('Senha atual incorreta.', 'error')
        elif nova_senha != confirmar:
            flash('Nova senha e confirmação não conferem.', 'error')
        elif len(nova_senha) < 6:
            flash('Senha deve ter no mínimo 6 caracteres.', 'error')
        else:
            current_user.set_password(nova_senha)
            db.session.commit()
            flash('Senha alterada com sucesso!', 'success')
            return redirect(url_for('perfil.editar'))
    return render_template('alterar-senha.html')
