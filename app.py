from flask import Flask, render_template
from models import db, login_manager, Need
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
        return {'need_count': Need.query.filter_by(ativo=True).count()}

    @app.errorhandler(404)
    def not_found(e):
        return render_template('404.html'), 404

    with app.app_context():
        db.create_all()

    return app


if __name__ == '__main__':
    app = create_app()
    app.run(debug=True)
