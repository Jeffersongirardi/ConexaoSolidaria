import os
from flask import Flask, render_template
from flask_login import current_user

app = None


def create_app():
    global app
    app = Flask(__name__)
    app.config.from_object('config.Config')

    from models import db, login_manager
    db.init_app(app)
    login_manager.init_app(app)

    from routes.main import main_bp
    from routes.auth import auth_bp
    from routes.dashboard import dashboard_bp
    from routes.necessidades import n_bp
    from routes.admin import admin_bp
    from routes.perfil import perfil_bp
    from routes.notificacoes import notif_bp
    from routes.blog import blog_bp
    from routes.pagamento import pagamento_bp
    from routes.upload import upload_bp

    app.register_blueprint(main_bp)
    app.register_blueprint(auth_bp)
    app.register_blueprint(dashboard_bp)
    app.register_blueprint(n_bp)
    app.register_blueprint(admin_bp)
    app.register_blueprint(perfil_bp)
    app.register_blueprint(notif_bp)
    app.register_blueprint(blog_bp)
    app.register_blueprint(pagamento_bp)
    app.register_blueprint(upload_bp)

    @app.context_processor
    def inject_globals():
        from models import Need, Notification
        ctx = {'need_count': Need.query.filter_by(ativo=True).count()}
        if current_user.is_authenticated:
            ctx['notificacoes_nao_lidas'] = Notification.query.filter_by(
                usuario_id=current_user.id, lida=False).count()
        else:
            ctx['notificacoes_nao_lidas'] = 0
        return ctx

    @app.errorhandler(404)
    def not_found(e):
        return render_template('404.html'), 404

    @app.errorhandler(403)
    def forbidden(e):
        return render_template('403.html'), 403

    with app.app_context():
        migrate_schema()
        seed_admin()

    return app


def migrate_schema():
    from sqlalchemy import inspect
    from models import db

    inspector = inspect(db.engine)
    tables = set(inspector.get_table_names())
    needs_drop = False

    if 'users' in tables:
        cols = {c['name'] for c in inspector.get_columns('users')}
        if 'cpf' not in cols:
            needs_drop = True

    if not needs_drop and 'institution' in tables:
        needs_drop = True

    if needs_drop:
        drop_order = ['donation_updates', 'donations', 'payments', 'need_images', 'needs',
                      'notifications', 'contact_messages', 'blog_posts',
                      'institution_profiles', 'institution', 'users']
        dialect = db.engine.url.get_dialect().name
        if dialect == 'sqlite':
            db.session.execute(db.text('PRAGMA foreign_keys = OFF'))
            db.session.commit()
        for t in drop_order:
            if t in tables:
                try:
                    db.metadata.tables[t].drop(db.engine, checkfirst=True)
                except KeyError:
                    db.session.execute(db.text(f'DROP TABLE IF EXISTS "{t}"'))
                    db.session.commit()
        db.create_all()
        if dialect == 'sqlite':
            db.session.execute(db.text('PRAGMA foreign_keys = ON'))
            db.session.commit()
    else:
        db.create_all()


def seed_admin():
    from models import User, db
    if not User.query.filter_by(tipo='admin').first():
        admin = User(
            nome='Administrador',
            email='admin@conexoessolidarias.org',
            tipo='admin',
            ativo=True
        )
        admin.set_password('admin123')
        db.session.add(admin)
        db.session.commit()


app = create_app()

if __name__ == '__main__':
    app.run(debug=True)
