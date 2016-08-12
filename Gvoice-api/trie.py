# -*- coding: utf-8 -*-
# Trie树实现，是存储词库的数据结构

import codecs
from util import strdecode


class _Node(object):
    """The node of the Trie"""
    def __init__(self):
        self.children = {}
        self._is_word = False
        self._attr = {}

    @property
    def is_word(self):
        return self._is_word

    @is_word.setter
    def is_word(self, value):
        if not isinstance(value, bool):
            raise ValueError("'is_word' must be bool!")
        self._is_word = value

    @property
    def attr(self):
        return self._attr

    @attr.setter
    def attr(self, value):
        if not isinstance(value, dict):
            raise ValueError("'attr' must be dict!")
        self._attr = value

    def reset(self):
        self.is_word = False
        self.attr = {}

    def delete(self, character):
        del(self.children[character])


class Trie(object):
    def __init__(self):
        """Initialize a new trie"""
        self._root = _Node()
        self._length = 0

    def clear(self):
        """Clear the trie"""
        self._root = _Node()
        self._length = 0

    def add_new_word(self, new_word, attributes):
        """Add a new word with given frequency"""
        p = self._root
        new_word = strdecode(new_word)
        for c in new_word:
            if c not in p.children:
                p.children[c] = _Node()
            p = p.children[c]
        p.attr = attributes
        p.is_word = True
        self._length += 1

    def delete_word(self, word):
        """Delete the given word. Return True if success, return false if doesn't find the word"""
        p = self._root
        word = strdecode(word)
        flag = None
        for c in word:
            if c not in p.children:
                return False
            if p.is_word or len(p.children) > 1:
                flag = p
                character = c
            p = p.children[c]
        if p.children:
            p.is_word = False
        elif flag:
            flag.delete(character)
        self._length -= 1
        return True

    def get_attr(self, word):
        """Get the frequency of the given word. Return None if word not found. Word should be utf-8 encoded!"""
        p = self._root
        word = strdecode(word)
        for c in word:
            if c not in p.children:
                return None
            p = p.children[c]
        return p.attr

    def has_word(self, word):
        """Return True if has word"""
        return self.__contains__(word)

    def __contains__(self, word):
        p = self._root
        word = strdecode(word)
        for c in word:
            if c not in p.children:
                return False
            p = p.children[c]
        return p.is_word

    def __len__(self):
        return self._length


if __name__ == "__main__":
    my_trie = Trie()
    print len(my_trie)
    my_trie.add_new_word("中华", {})
    print len(my_trie)
    my_trie.add_new_word("中华人民", {})
    print len(my_trie)
    my_trie.add_new_word("中华人民共和国", {})
    print len(my_trie)
    my_trie.add_new_word("中华人民之歌", {})
    my_trie.delete_word("中华人民")
    print len(my_trie)
    print my_trie.has_word("中华人民")
    print my_trie.has_word("中华人民共和国")
    print my_trie.has_word("中华")
    print my_trie.has_word("中华人民之歌")
    my_trie.delete_word("中华人民共和国")
    print len(my_trie)
    print my_trie.has_word("中华人民共和国")
    print my_trie.has_word("中华人民")
    print my_trie.has_word("中华")
    print my_trie.has_word("中华人民之歌")
    my_trie.delete_word("中华")
    print my_trie.has_word("中华")
    print my_trie.has_word("中华人民之歌")



