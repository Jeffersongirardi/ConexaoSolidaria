import os
from flask import Blueprint, current_app
from werkzeug.utils import secure_filename
from uuid import uuid4

upload_bp = Blueprint('upload', __name__)

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'webp'}
MAX_SIZE = 5 * 1024 * 1024  # 5MB


def save_upload(file, subdir='needs'):
    if not file or not file.filename:
        return None
    ext = file.filename.rsplit('.', 1)[-1].lower() if '.' in file.filename else ''
    if ext not in ALLOWED_EXTENSIONS:
        return None
    if file.content_length and file.content_length > MAX_SIZE:
        return None
    name = f'{uuid4().hex}.{ext}'
    path = os.path.join(current_app.root_path, 'static', 'uploads', subdir)
    os.makedirs(path, exist_ok=True)
    file.save(os.path.join(path, name))
    return f'uploads/{subdir}/{name}'
