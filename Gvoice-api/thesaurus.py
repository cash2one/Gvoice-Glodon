# -*- coding: utf-8 -*-
# 词库

from trie import Trie
import codecs
from util import strdecode


class Thesaurus(object):
    """When initialize the Thesaurus, it will scan the word_bank.txt and establish the dictionary trie."""
    def __init__(self, word_bank_path, tags):
        if not isinstance(tags, list):
            raise ValueError("'default_setting' must be dict!")
        self._trie = Trie()
        for line in open(word_bank_path, 'r'):
            item = strdecode(line).strip().split(' ')
            attr = {}
            for index in range(len(item) - 1):
                attr[tags[index]] = item[index + 1]
            self._trie.add_new_word(item[0], attr)

    def __len__(self):
        return self._trie.__len__()

    def __contains__(self, word):
        return self._trie.__contains__(word)

    def clear(self):
        self._trie = Trie()

    def has_word(self, word):
        """Return whether the thesaurus contains the word"""
        return self._trie.has_word(word)

    def get_attr(self, word):
        """Return the frequency of the word"""
        return self._trie.get_attr(word)


if __name__ == "__main__":
    my_thesaurus = Thesaurus("construction_dict.txt", ["Frequency", "Property"])
    word = "参数化墙"
    print word
    print word, my_thesaurus.get_attr(word)
    print my_thesaurus.get_attr(word)["Frequency"]
    print my_thesaurus.get_attr(word)["Property"]
    print my_thesaurus.has_word(word)
    print my_thesaurus.has_word("广联达")
    print len(my_thesaurus)