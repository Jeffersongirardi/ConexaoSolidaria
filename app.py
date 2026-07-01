from flask import Flask, render_template, send_from_directory, redirect
import os

app = Flask(__name__, static_folder='.')

@app.route('/')
def index():
    return send_from_directory('.', 'index.html')

@app.route('/index.html')
def index_html():
    # Redireciona /index.html para /
    return redirect('/')

@app.route('/css/<path:path>')
def send_css(path):
    return send_from_directory('css', path)

@app.route('/js/<path:path>')
def send_js(path):
    return send_from_directory('js', path)

@app.route('/pages/<path:path>')
def send_pages(path):
    return send_from_directory('pages', path)

if __name__ == '__main__':
    app.run(debug=True)