from __future__ import division
print(division)

import sys
print(sys.path)
import zipfile
import os
import random
print(os)
print(os.uname())

def testread(name) :
    text_file = open(name, "rt")
    result = text_file.readline()
    print(result)
    text_file.close()
    return result

def add(arg1,arg2) :
    return arg1+arg2


def testArray(array) :
     print(array)
     return array