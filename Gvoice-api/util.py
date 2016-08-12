#-*-coding:utf-8 -*-
import os
import sys

text_type = unicode


def strdecode(sentence):
    """This function would decode Unicode, utf-8 or gbk to unicode"""
    if not isinstance(sentence, text_type):
        try:
            sentence = sentence.decode('utf-8')
        except UnicodeDecodeError:
            sentence = sentence.decode('gbk', 'ignore')
    return sentence

def strencode(sentence, type):
    """This function would encode Unicode, utf-8 or gbk to given encoding type"""
    return strdecode(sentence).encode(type)

def formResult(title, content, pictureUrl, url):
    return {
        'title': title,
        'content': content,
        'pictureUrl': pictureUrl,
        'url': url
    }