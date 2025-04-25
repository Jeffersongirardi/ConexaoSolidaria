from flask import Flask, render_template, send_from_directory
import os

app = Flask(__name__)

# Define o diretório base do projeto
base_dir = os.path.abspath(os.path.dirname(__file__))

@app.route('/')
def index():
    return send_from_directory(base_dir, 'index.html')

@app.route('/css/<path:path>')
def send_css(path):
    return send_from_directory(os.path.join(base_dir, 'css'), path)

@app.route('/js/<path:path>')
def send_js(path):
    return send_from_directory(os.path.join(base_dir, 'js'), path)

@app.route('/pages/<path:path>')
def send_pages(path):
    return send_from_directory(os.path.join(base_dir, 'pages'), path)

if __name__ == '__main__':
    app.run(debug=True)