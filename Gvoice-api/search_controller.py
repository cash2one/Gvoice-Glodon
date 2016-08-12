#-*-coding:utf-8 -*-

import os
import crawler
import token_util
import turning

# initialize tokenizer
my_tokenizer = token_util.TokenUtil(os.path.join(os.path.dirname(__file__), "dict.txt.big"))
my_tokenizer.init_classification(os.path.join(os.path.dirname(__file__), 'construction_dict.txt'))
my_tokenizer.init_logic_translator(os.path.join(os.path.dirname(__file__), 'Logic_words.txt'))


# query based on tokenize result
def query(query_string, userid, location):
    answer = my_tokenizer.get_keyword(query_string)
    if answer['Type'] == 'customized':
        return crawler.search_baike(answer['Token'][0])
    else:
        logic_answer = my_tokenizer.logic_translate(query_string)
        if logic_answer['Type'] == 'logic':
            return crawler.search_baidu(logic_answer['Content'])
        else:
            return turning.post_query(query_string, userid, location)
