from flask import Blueprint, render_template, redirect, url_for, request, flash, abort
from flask_login import login_required, current_user
from functools import wraps
from models import db, User, InstitutionProfile, Donation, Need, ContactMessage, BlogPost, Notification

admin_bp = Blueprint('admin', __name__, url_prefix='/admin')


def admin_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if not current_user.is_authenticated or current_user.tipo != 'admin':
            abort(403)
        return f(*args, **kwargs)
    return decorated


@admin_bp.route('/')
@login_required
@admin_required
def dashboard():
    stats = {
        'usuarios': User.query.count(),
        'instituicoes': InstitutionProfile.query.count(),
        'pendentes': InstitutionProfile.query.filter_by(aprovado=False).count(),
        'doacoes': Donation.query.count(),
        'necessidades': Need.query.count(),
        'mensagens': ContactMessage.query.filter_by(lido=False).count(),
    }
    return render_template('admin/dashboard.html', stats=stats)


@admin_bp.route('/instituicoes')
@login_required
@admin_required
def instituicoes():
    filtro = request.args.get('filtro', 'pendentes')
    query = InstitutionProfile.query
    if filtro == 'pendentes':
        query = query.filter_by(aprovado=False)
    elif filtro == 'aprovadas':
        query = query.filter_by(aprovado=True)
    instituicoes = query.order_by(InstitutionProfile.data_cadastro.desc()).all()
    return render_template('admin/instituicoes.html', instituicoes=instituicoes, filtro=filtro)


@admin_bp.route('/instituicoes/aprovar/<int:id>', methods=['POST'])
@login_required
@admin_required
def aprovar_instituicao(id):
    profile = InstitutionProfile.query.get_or_404(id)
    profile.aprovado = True
    profile.motivo_recusa = None
    notif = Notification(
        usuario_id=profile.user_id,
        tipo='cadastro_aprovado',
        mensagem='Seu cadastro foi aprovado! Você já pode cadastrar necessidades.',
        link=url_for('dashboard.instituicao')
    )
    db.session.add(notif)
    db.session.commit()
    flash('Instituição aprovada com sucesso!', 'success')
    return redirect(url_for('admin.instituicoes'))


@admin_bp.route('/instituicoes/recusar/<int:id>', methods=['POST'])
@login_required
@admin_required
def recusar_instituicao(id):
    profile = InstitutionProfile.query.get_or_404(id)
    motivo = request.form.get('motivo', '')
    profile.motivo_recusa = motivo
    notif = Notification(
        usuario_id=profile.user_id,
        tipo='cadastro_recusado',
        mensagem=f'Seu cadastro foi recusado. Motivo: {motivo or "Não informado"}',
        link=url_for('auth.cadastro_instituicao')
    )
    db.session.add(notif)
    db.session.commit()
    flash('Instituição recusada.', 'info')
    return redirect(url_for('admin.instituicoes'))


@admin_bp.route('/usuarios')
@login_required
@admin_required
def usuarios():
    usuarios = User.query.order_by(User.data_cadastro.desc()).all()
    return render_template('admin/usuarios.html', usuarios=usuarios)


@admin_bp.route('/usuarios/toggle/<int:id>')
@login_required
@admin_required
def toggle_usuario(id):
    user = User.query.get_or_404(id)
    if user.tipo == 'admin':
        flash('Não é possível desativar o admin.', 'error')
        return redirect(url_for('admin.usuarios'))
    user.ativo = not user.ativo
    db.session.commit()
    flash(f'Usuário {"ativado" if user.ativo else "desativado"}.', 'success')
    return redirect(url_for('admin.usuarios'))


@admin_bp.route('/mensagens')
@login_required
@admin_required
def mensagens():
    msgs = ContactMessage.query.order_by(ContactMessage.data_envio.desc()).all()
    return render_template('admin/mensagens.html', mensagens=msgs)


@admin_bp.route('/mensagens/ler/<int:id>')
@login_required
@admin_required
def ler_mensagem(id):
    msg = ContactMessage.query.get_or_404(id)
    msg.lido = True
    db.session.commit()
    return redirect(url_for('admin.mensagens'))


@admin_bp.route('/blog')
@login_required
@admin_required
def blog():
    posts = BlogPost.query.order_by(BlogPost.data_criacao.desc()).all()
    return render_template('admin/blog.html', posts=posts)


@admin_bp.route('/blog/criar', methods=['GET', 'POST'])
@login_required
@admin_required
def blog_criar():
    if request.method == 'POST':
        from datetime import datetime
        post = BlogPost(
            titulo=request.form.get('titulo'),
            conteudo=request.form.get('conteudo'),
            resumo=request.form.get('resumo'),
            autor_id=current_user.id,
            categoria=request.form.get('categoria', 'geral'),
            imagem_url=request.form.get('imagem_url'),
            publicado=bool(request.form.get('publicado')),
            data_publicacao=datetime.now() if bool(request.form.get('publicado')) else None
        )
        post.save()
        flash('Post criado com sucesso!', 'success')
        return redirect(url_for('admin.blog'))
    return render_template('admin/blog-form.html', post=None)


@admin_bp.route('/blog/editar/<int:id>', methods=['GET', 'POST'])
@login_required
@admin_required
def blog_editar(id):
    post = BlogPost.query.get_or_404(id)
    if request.method == 'POST':
        from datetime import datetime
        post.titulo = request.form.get('titulo')
        post.conteudo = request.form.get('conteudo')
        post.resumo = request.form.get('resumo')
        post.categoria = request.form.get('categoria', 'geral')
        post.imagem_url = request.form.get('imagem_url')
        post.publicado = bool(request.form.get('publicado'))
        if post.publicado and not post.data_publicacao:
            post.data_publicacao = datetime.now()
        db.session.commit()
        flash('Post atualizado!', 'success')
        return redirect(url_for('admin.blog'))
    return render_template('admin/blog-form.html', post=post)


@admin_bp.route('/blog/deletar/<int:id>', methods=['POST'])
@login_required
@admin_required
def blog_deletar(id):
    post = BlogPost.query.get_or_404(id)
    db.session.delete(post)
    db.session.commit()
    flash('Post removido.', 'info')
    return redirect(url_for('admin.blog'))
