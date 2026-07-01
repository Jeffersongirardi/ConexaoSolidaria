from datetime import datetime
from flask import Blueprint, render_template, redirect, url_for, request, flash
from flask_login import login_user, logout_user, login_required, current_user
from models import db, User, InstitutionProfile

auth_bp = Blueprint('auth', __name__, url_prefix='/auth')


@auth_bp.route('/login-doador', methods=['GET', 'POST'])
def login_doador():
    if current_user.is_authenticated:
        return redirect(url_for('dashboard.doador'))
    if request.method == 'POST':
        email = request.form.get('email')
        senha = request.form.get('senha')
        user = User.query.filter_by(email=email, tipo='doador').first()
        if user and user.check_password(senha):
            login_user(user)
            next_page = request.args.get('next')
            return redirect(next_page or url_for('dashboard.doador'))
        flash('E-mail ou senha inválidos.', 'error')
    return render_template('login-doador.html')


@auth_bp.route('/login-instituicao', methods=['GET', 'POST'])
def login_instituicao():
    if current_user.is_authenticated:
        return redirect(url_for('dashboard.instituicao'))
    if request.method == 'POST':
        email = request.form.get('email')
        senha = request.form.get('senha')
        user = User.query.filter_by(email=email, tipo='instituicao').first()
        if user and user.check_password(senha):
            login_user(user)
            return redirect(url_for('dashboard.instituicao'))
        flash('E-mail ou senha inválidos.', 'error')
    return render_template('login-instituicao.html')


@auth_bp.route('/cadastro-doador', methods=['GET', 'POST'])
def cadastro_doador():
    if request.method == 'POST':
        nome = request.form.get('nome')
        email = request.form.get('email')
        senha = request.form.get('senha')
        telefone = request.form.get('telefone')
        cpf = request.form.get('cpf')
        data_nascimento = request.form.get('data_nascimento')
        whatsapp = request.form.get('whatsapp')
        cep = request.form.get('cep')
        cidade = request.form.get('cidade')
        estado = request.form.get('estado')
        if User.query.filter_by(email=email).first():
            flash('E-mail já cadastrado.', 'error')
            return render_template('cadastro-doador.html')
        user = User(
            nome=nome, email=email, telefone=telefone, tipo='doador',
            cpf=cpf, whatsapp=whatsapp, cep=cep, cidade=cidade, estado=estado
        )
        if data_nascimento:
            user.data_nascimento = datetime.strptime(data_nascimento, '%Y-%m-%d').date()
        user.set_password(senha)
        db.session.add(user)
        db.session.commit()
        flash('Conta criada com sucesso! Faça login.', 'success')
        return redirect(url_for('auth.login_doador'))
    return render_template('cadastro-doador.html')


@auth_bp.route('/cadastro-instituicao', methods=['GET', 'POST'])
def cadastro_instituicao():
    if request.method == 'POST':
        nome = request.form.get('nome')
        email = request.form.get('email')
        senha = request.form.get('senha')
        cnpj = request.form.get('cnpj')
        razao_social = request.form.get('razao_social')
        nome_fantasia = request.form.get('nome_fantasia')
        telefone = request.form.get('telefone')
        whatsapp = request.form.get('whatsapp')
        endereco = request.form.get('endereco')
        descricao = request.form.get('descricao')
        categoria_atuacao = request.form.get('categoria_atuacao')
        pix_key = request.form.get('pix_key')
        pix_titular = request.form.get('pix_titular')
        if User.query.filter_by(email=email).first():
            flash('E-mail já cadastrado.', 'error')
            return render_template('cadastro-instituicao.html')
        user = User(nome=nome, email=email, telefone=telefone, whatsapp=whatsapp, tipo='instituicao')
        user.set_password(senha)
        db.session.add(user)
        db.session.flush()
        profile = InstitutionProfile(
            user_id=user.id, cnpj=cnpj, razao_social=razao_social,
            nome_fantasia=nome_fantasia, endereco=endereco, descricao=descricao,
            categoria_atuacao=categoria_atuacao, whatsapp=whatsapp,
            pix_key=pix_key, pix_titular=pix_titular
        )
        db.session.add(profile)
        db.session.commit()
        flash('Cadastro realizado! Aguarde aprovação.', 'success')
        return redirect(url_for('auth.login_instituicao'))
    return render_template('cadastro-instituicao.html')


@auth_bp.route('/login-admin', methods=['GET', 'POST'])
def login_admin():
    if current_user.is_authenticated:
        return redirect(url_for('admin.dashboard'))
    if request.method == 'POST':
        email = request.form.get('email')
        senha = request.form.get('senha')
        user = User.query.filter_by(email=email, tipo='admin').first()
        if user and user.check_password(senha):
            login_user(user)
            return redirect(url_for('admin.dashboard'))
        flash('Credenciais inválidas.', 'error')
    return render_template('login-admin.html')


@auth_bp.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('main.index'))
