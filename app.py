import os
from flask import Flask
from config import Config
from models import db, login_manager
from routes import register_blueprints


def create_app():
    app = Flask(__name__, template_folder='templates', static_folder='.', static_url_path='')
    app.config.from_object(Config)

    db.init_app(app)
    login_manager.init_app(app)

    register_blueprints(app)

    with app.app_context():
        import models
        db.create_all()

    @app.context_processor
    def inject_stats():
        from models import Donation, Institution
        return {
            'stats': {
                'doacoes': Donation.query.filter_by(status='recebido').count(),
                'instituicoes': Institution.query.filter_by(aprovado=True).count(),
            }
        }

    return app


app = create_app()

if __name__ == '__main__':
    app.run(debug=True)
