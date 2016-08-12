#!/usr/bin/python
#-*-coding:utf-8 -*-

from flask import Flask, jsonify
from flask import abort
from flask import make_response
from flask import request
from flask import send_from_directory
from werkzeug import secure_filename
import sys, os
import iat
import tts
import id
import search_controller

# add environment path
os.putenv('LD_LIBRARY_PATH', "/home/autotest/gvoice/git_repo/Linux-Speech/libs/x64/")

app = Flask(__name__)

AUDIO_FOLDER = 'wav'
ALLOWED_EXTENSIONS = set(['wav'])

app.config['AUDIO_FOLDER'] = os.path.join(os.path.dirname(__file__), AUDIO_FOLDER)


@app.errorhandler(404)
def not_found(error):
    return make_response(jsonify({'error': 'Something WRONG!'}), 404)

    
# get user id
@app.route('/gvoice/api/v1.0/id', methods=['GET'])
def get_id():
    return jsonify({'userid': id.generate(32)}), 200

    
# query from post
@app.route('/gvoice/api/v1.0/query', methods=['POST'])
def create_query():
    if not request.json or not 'query' in request.json or not 'userid' in request.json:
        abort(404)
    
    query_string = request.json['query']
    userid = request.json['userid']
    location = request.json['location']
    result = query(query_string, userid, location)

    return jsonify({'result': result}), 200

    
# generate audio from text
@app.route('/gvoice/api/v1.0/audio', methods=['POST'])
def create_audio():
    if not request.json or not 'text' in request.json:
        abort(404)
    text = request.json['text'].encode('utf8')
    create_dir(app.config['AUDIO_FOLDER'])
    result = tts.get_audio(text)
    filename = 'tts.wav'
    
    return jsonify({'filename': filename}), 200

    
# download audio to client
@app.route('/gvoice/api/v1.0/audio', methods=['GET'])
def download_audio():
    filename = 'tts.wav'
    dir = app.config['AUDIO_FOLDER']
    if os.path.isfile(os.path.join(dir, filename)):
        return send_from_directory(dir, filename, as_attachment=True), 200
    else:
        abort(404)

        
# recognize user audio
@app.route('/gvoice/api/v1.0/upload', methods=['POST'])
def get_recognize_result():
    file = request.files['file']
    if file and allowed_file(file.filename):
        filename = secure_filename('upload.wav')
        create_dir(app.config['AUDIO_FOLDER'])
        file.save(os.path.join(app.config['AUDIO_FOLDER'], filename))
    else:
        return abort(404)
    
    text = iat.get_text()

    return jsonify({'result': text}), 200

    
def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS

           
def create_dir(path):
    if not os.path.exists(path):
        os.mkdir(path)

def query(query_string, userid, location):
    return search_controller.query(query_string, userid, location)


if __name__ == '__main__':
    app.run(host = '192.168.132.51')
