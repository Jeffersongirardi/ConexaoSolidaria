from flask import Blueprint, render_template, redirect, url_for, request, flash
from flask_login import login_user, logout_user, login_required, current_user
from models import db, User, Institution

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
            return redirect(url_for('dashboard.doador'))
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
        if User.query.filter_by(email=email).first():
            flash('E-mail já cadastrado.', 'error')
            return render_template('cadastro-doador.html')
        user = User(nome=nome, email=email, telefone=telefone, tipo='doador')
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
        telefone = request.form.get('telefone')
        endereco = request.form.get('endereco')
        descricao = request.form.get('descricao')
        if User.query.filter_by(email=email).first():
            flash('E-mail já cadastrado.', 'error')
            return render_template('cadastro-instituicao.html')
        inst = Institution(
            nome=nome, email=email, telefone=telefone,
            tipo='instituicao', cnpj=cnpj, razao_social=razao_social,
            endereco=endereco, descricao=descricao
        )
        inst.set_password(senha)
        db.session.add(inst)
        db.session.commit()
        flash('Cadastro realizado! Aguarde aprovação.', 'success')
        return redirect(url_for('auth.login_instituicao'))
    return render_template('cadastro-instituicao.html')


@auth_bp.route('/logout')
@login_required
def logout():
    logout_user()
    return redirect(url_for('main.index'))
