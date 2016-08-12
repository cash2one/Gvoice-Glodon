# -*- coding: utf-8 -*-
# 这是分词器，一般情况一个应用只应有一个，生命周期与应用相同

from thesaurus import Thesaurus
import jieba
import jieba.analyse
import codecs
from util import strdecode
from util import strencode

class TokenUtil(object):
    """Tokenizer would initialize the thesaurus and scan all utf-8 encoded vocabulary on the disk."""
    def __init__(self, general_thesaurus_path):
        """Read the general thesaurus"""
        jieba.initialize(general_thesaurus_path)

    def init_classification(self, customized_thesaurus_path):
        self._customized_thesaurus = Thesaurus(customized_thesaurus_path, ["Frequency", "Property"])
        jieba.load_userdict(customized_thesaurus_path)

    def get_keyword(self, content):
        seg_list = jieba.cut_for_search(content)
        customized_words = []
        for atoken in seg_list:
            if atoken in self._customized_thesaurus:
                customized_words.append(atoken)
        if len(customized_words) > 0:
            return {'Type': 'customized',
                    'Token': customized_words}
        else:
            return {'Type': 'general',
                    'Token': content}

    def init_logic_translator(self, logic_thesaurus_path):
        """Initialize logic translator"""
        self._logic_thesaurus = Thesaurus(logic_thesaurus_path, ["Logic"])
        jieba.load_userdict(logic_thesaurus_path)

    def logic_translate(self, content):
        """Translate the content to logic expression for Baidu Search Engine"""
        content = strdecode(content)
        result_list = []
        token_list = jieba.tokenize(content)
        has_logic = False
        for token in token_list:
            if token[0] in self._logic_thesaurus:
                has_logic = True
                result_list.append({"Type": self._logic_thesaurus.get_attr(token[0])["Logic"],
                                    "Content": token[0]})
            else:
                result_list.append({"Type": "Common",
                                    "Content": token[0]})
        if not has_logic:
            return {"Type": "general",
                    "Content": content}
        translate_finish = False
        or_list = []
        not_list = []
        and_list = []
        while(not translate_finish and len(result_list) > 0):
            for index in range(len(result_list)):
                if result_list[index]["Type"] == "NOT":
                    # 如果是NOT逻辑词
                    if index < len(result_list) - 1 and result_list[index + 1]["Type"] == "Common":
                        # 如果可以合并语句，则进行合并
                        not_list.append("-(" + result_list[index + 1]["Content"] + ")")
                        del result_list[index + 1]
                        del result_list[index]
                        break
                    else:
                        # 若不能合并语句，则将逻辑词视为普通词语
                        result_list[index]["Type"] = "Common"
                if result_list[index]["Type"] == "AND":
                    # 如果是AND逻辑词
                    if 0 < index < len(result_list) - 1 and result_list[index + 1]["Type"] == "Common" and\
                        result_list[index - 1]["Type"] == "Common":
                        and_list.append("(" + result_list[index - 1]["Content"] + " " +
                                        result_list[index + 1]["Content"] + ")")
                        del result_list[index + 1]
                        del result_list[index]
                        del result_list[index - 1]
                        break
                    else:
                        # 若不能合并语句，则将逻辑词视为普通词语
                        result_list[index]["Type"] = "Common"
                if result_list[index]["Type"] == "OR":
                    # 如果是OR连接词
                    if 0 < index < len(result_list) - 1 and result_list[index + 1]["Type"] == "Common" and\
                        result_list[index - 1]["Type"] == "Common":
                        or_list.append("(" + result_list[index - 1]["Content"] + " | " +
                                        result_list[index + 1]["Content"] + ")")
                        del result_list[index + 1]
                        del result_list[index]
                        del result_list[index - 1]
                        break
                    else:
                        # 若不能合并语句，则将逻辑词视为普通词语
                        result_list[index]["Type"] = "Common"
                if index >= len(result_list) - 1:
                    # 所有逻辑词处理完成
                    translate_finish = True
        result_content = " ".join([item["Content"] for item in result_list]) + " " + " ".join(and_list) + " " +\
                         " ".join(or_list) + " " + " ".join(not_list)
        if(result_content == ""):
            return {"Type": "general",
                    "Content": content}
        return {"Type": "logic",
                "Content": result_content}


if __name__ == "__main__":
    my_tokenizer = TokenUtil("dict.txt.big")
    my_tokenizer.init_classification('construction_dict.txt')
    print len(my_tokenizer._customized_thesaurus)
    content = "普通墙与异形墙分别是什么"
    answer = my_tokenizer.get_keyword(content)
    print answer['Type']
    for atoken in answer['Token']:
        print strdecode(atoken)
    content = "太阳是什么"
    answer = my_tokenizer.get_keyword(content)
    print answer['Type']
    print answer['Token']
    my_tokenizer.init_logic_translator("Logic_words.txt")
    content = "不要辣椒和大蒜或者洋葱的川菜"
    result = my_tokenizer.logic_translate(content)
    print result["Type"]
    print result["Content"]
    content = "太阳是什么"
    result = my_tokenizer.logic_translate(content)
    print result["Type"]
    print result["Content"]
    content = "太阳或许是假的"
    result = my_tokenizer.logic_translate(content)
    print result["Type"]
    print result["Content"]
    seg = jieba.cut_for_search(content)
    print "/".join(seg)
    content = "我和你"
    result = my_tokenizer.logic_translate(content)
    print result["Type"]
    print result["Content"]
    seg = jieba.cut_for_search(content)
    print "/".join(seg)
    content = "普通墙与异形墙分别是什么"
    answer = my_tokenizer.get_keyword(content)
    print answer['Type']
    for atoken in answer['Token']:
        print strdecode(atoken)

