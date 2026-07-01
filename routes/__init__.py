from .auth import auth_bp
from .main import main_bp
from .dashboard import dashboard_bp
from .necessidades import n_bp
from .admin import admin_bp
from .perfil import perfil_bp
from .notificacoes import notif_bp
from .blog import blog_bp


def register_blueprints(app):
    app.register_blueprint(auth_bp)
    app.register_blueprint(main_bp)
    app.register_blueprint(dashboard_bp)
    app.register_blueprint(n_bp)
    app.register_blueprint(admin_bp)
    app.register_blueprint(perfil_bp)
    app.register_blueprint(notif_bp)
    app.register_blueprint(blog_bp)
