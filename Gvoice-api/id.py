#-*-coding:utf-8 -*-

import random
import string

# generate N length [0-9a-zA-Z] string
def generate(N):
    return ''.join(random.choice(string.digits + string.ascii_letters) for _ in range(N))

if __name__ == '__main__':
    print generate(32)