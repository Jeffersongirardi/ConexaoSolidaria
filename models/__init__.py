from flask_sqlalchemy import SQLAlchemy
from flask_login import LoginManager

db = SQLAlchemy()
login_manager = LoginManager()
login_manager.login_view = 'auth.login_doador'

from .user import User
from .institution import InstitutionProfile
from .donation import Donation
from .need import Need
from .need_image import NeedImage
from .payment import Payment
from .donation_update import DonationUpdate
from .notification import Notification
from .contact_message import ContactMessage
from .blog_post import BlogPost


@login_manager.user_loader
def load_user(user_id):
    return User.query.get(int(user_id))
