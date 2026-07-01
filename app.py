import os
from flask import Flask, render_template
from models import db, login_manager
from config import Config


def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    db.init_app(app)
    login_manager.init_app(app)

    from routes.main import main_bp
    from routes.auth import auth_bp
    from routes.dashboard import dashboard_bp

    app.register_blueprint(main_bp)
    app.register_blueprint(auth_bp)
    app.register_blueprint(dashboard_bp)

    @app.context_processor
    def inject_globals():
        from models import Need
        return {'need_count': Need.query.filter_by(ativo=True).count()}

    @app.errorhandler(404)
    def not_found(e):
        return render_template('404.html'), 404

    with app.app_context():
        migrate_schema()

    return app


def migrate_schema():
    from sqlalchemy import inspect
    from models import User, InstitutionProfile, Need, Donation

    inspector = inspect(db.engine)
    tables = set(inspector.get_table_names())

    old_tables = {'institution'}
    drop_order = ['donations', 'needs', 'institution']
    to_drop = [t for t in drop_order if t in tables]

    if to_drop:
        dialect = db.engine.url.get_dialect().name
        if dialect == 'sqlite':
            db.session.execute(db.text('PRAGMA foreign_keys = OFF'))
            db.session.commit()
        for t in to_drop:
            db.metadata.tables[t].drop(db.engine, checkfirst=True)
        db.create_all()
        if dialect == 'sqlite':
            db.session.execute(db.text('PRAGMA foreign_keys = ON'))
            db.session.commit()
    else:
        db.create_all()


app = create_app()

if __name__ == '__main__':
    app.run(debug=True)
