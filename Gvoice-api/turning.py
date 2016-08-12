#-*-coding:utf-8 -*-

import requests
import json
import util

API_URL = 'http://www.tuling123.com/openapi/api'
# set your turning api key
API_KEY = '597aed0c67bb48e8a804e8635fe8a114'


def post_query(info, userid, location):
    post_json = {'key': API_KEY, 'info': info, 'userid': userid, 'loc': location}
    r = requests.post(API_URL, json = post_json)
    result = json.loads(r.text)
    code = result['code']
    # text
    if code == 100000:
        print(result['text'])
        return util.formResult('', result['text'], '', '')
    # links
    elif code == 200000:
        print(result['text'], result['url'])
        return util.formResult('', result['text'], '', result['url'])
    # news
    elif code == 302000:
        answer_list = []
        print(result['text'])
        content = result['text']
        first_item = result['list'][0]
        for item in result['list']:
            print('%s - %s' % (item['article'], item['source']))
            print('%s' % item['detailurl'])
            result = util.formResult(item['article'], content, '', item['detailurl'])
            answer_list.append(result)
        return util.formResult(first_item['article'], content, '', first_item['detailurl'])
    # cookbook
    elif code == 308000:
        answer_list = []
        print(result['text'])
        content = result['text']
        first_item = result['list'][0]
        for item in result['list']:
            print('%s' % item['name'])
            print('%s' % item['detailurl'])
            result = util.formResult(item['name'], content, '', item['detailurl'])
            answer_list.append(result)
        return util.formResult(first_item['name'], content, '', first_item['detailurl'])
    # errors
    else:
        print(result['text'])
        return util.formResult('', result['text'], '', '')



if __name__ == '__main__':
    query = '天气'
    post_query(query)