#-*-coding:utf-8 -*-

import commands

def get_text():
    cmd = './iat'
    (stat, output) = commands.getstatusoutput(cmd)
    if (stat == 0):
        return output
    else:
        print "Failed, output: %s" %output

if __name__ == '__main__':
    get_text()
