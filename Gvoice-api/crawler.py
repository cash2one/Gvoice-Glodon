#-*-coding:utf-8 -*-

import requests
import bs4
import re
import util
import sys

reload(sys)
sys.setdefaultencoding('utf-8')

def get_response_content(url, params):
    headers = {'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36'}
    
    response = requests.get(url, params=params, headers=headers)
    
    return response

def search_baike(query):
    url = 'http://baike.baidu.com/search'
    params = {
        'word': query,
        'pn': 0,
        'rn': 0,
        'enc': 'utf-8'
    }
    response = get_response_content(url, params)
    response.encoding='utf-8'

    return parse_baike(response.text)

def search_baidu(query):
    url = 'http://www.baidu.com/s'
    params = {
        'wd': query,
        'ie': 'utf-8',
        'cl': 3,
        'tn': 'baidulocal'
    }
    response = get_response_content(url, params)
    response.encoding='utf-8'
    
    return parse_baidu(response.text)


def parse_baike(html_content):
    soup = bs4.BeautifulSoup(html_content, "lxml")
    
    # get search result number
    result_num = soup.find('div', class_ = "result-count")
    if not result_num:
        return ""
        
    # get search results
    results = soup.body.dl
    
    # get first result
    url = results.dd.a.get('href')
    title = results.dd.a.get_text()
    content = results.dd.p.get_text()

    # get pictures url
    url_content = requests.get(url)
    url_content.encoding = 'utf-8'
    url_soup = bs4.BeautifulSoup(url_content.text, "lxml")
    pictureUrl_list = url_soup.find_all('img', src = re.compile(r'^http?://.+\.(jpg|png)'))
    pictureUrl = ''

    # get first pic url
    if pictureUrl_list:
        pictureUrl = pictureUrl_list[0].get('src')    

    return util.formResult(title, content, pictureUrl, url)

def parse_baidu(html_content):
    soup = bs4.BeautifulSoup(html_content, "lxml")
    # get search results
    results = soup.ol.table
    
    # get first result
    title = results.a.get_text()
    url = results.a.get('href')
    content = re.split(re.compile(r'...[a-z0-9\.]+\.(com|cn|net|org)\/.+'), results.find('font', size = "-1").get_text())[0]
    
    # get pictures url
    url_content = requests.get(url)
    url_content.encoding = 'utf-8'
    url_soup = bs4.BeautifulSoup(url_content.text, "lxml")
    pictureUrl_list = url_soup.find_all('img', src = re.compile(r'^http?://.+\.(jpg|png)'))
    pictureUrl = ''

    # get first pic url
    if pictureUrl_list:
        pictureUrl = pictureUrl_list[0].get('src')
   
    return util.formResult(title, content, pictureUrl, url)
    

if __name__ == '__main__':
    search_baidu('周杰伦')
