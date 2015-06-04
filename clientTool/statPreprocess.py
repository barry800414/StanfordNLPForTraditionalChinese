#!/usr/bin/env python3
import sys
import json
import re
import random
from NLPToolRequests import *
from parseNews import parseText
from segNews import segText
from tagNews import tagText

# default sentence separator
#SEP = '[;\t\n。；　「」﹝﹞【】《》〈〉（）〔〕『 』\(\)\[\]!?？！]'

SEP = '[,;\t\n，。；　「」﹝﹞【】《》〈〉（）〔〕『 』\(\)\[\]!?？！]'

# default new sentence separator
NEW_SEP = ','

# default to-removed punctuation
TO_REMOVE = '[\uF0D8\u0095/=&�+:：／\|‧]'
#TO_REMOVE = '[\uF0D8\u0095/=&�+、:：／\|‧]'

# default brackets for fixing them (not to segment)
BRACKETS = [ ('[', ']'), ('(', ')'), ('{', '}'), 
             ('〈', '〉'), ('《', '》'), ('【', '】'),
             ('﹝', '﹞'), ('「','」'), ('『', '』'), 
             ('（','）'), ('〔','〕')]

allRelationSet = set()

def preStat(statDict, sep=SEP, new_sep=NEW_SEP, to_remove=TO_REMOVE):
    for statId, statObj in statDict.items():
        stat = statObj['original']
        statObj['seg'] = segText(stat, sep, new_sep, to_remove)
        statObj['pos'] = tagText(stat, sep, new_sep, to_remove)
        statObj['dep'] = depParseText(stat, sep=sep, new_sep=new_sep, to_remove=to_remove)
        statObj['const'] = constParseText(stat, sep, new_sep, to_remove)
    return statDict

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print('Usage:', sys.argv[0], 'InStatJson OutStatJson [PunctuationJson]', file=sys.stderr)
        exit(-1)
    inStatJsonFile = sys.argv[1]
    outStatJsonFile = sys.argv[2]
    
    # read in statement fuke
    with open(inStatJsonFile, 'r') as f:
        statDict = json.load(f)
 
    # read in punctuation file
    if len(sys.argv) == 4:
        punctuationJsonFile = sys.argv[3]
        punct = Punctuation.readJsonFile(punctuationJsonFile)
        sepRegexStr = Punctuation.toRegexStr(punct['sep'])
        removeRegexStr = Punctuation.toRegexStr(punct['remove'])
    else:
        sepRegexStr = SEP
        removeRegexStr = TO_REMOVE

    statDict = preStat(statDict, sep=sepRegexStr, 
            new_sep=NEW_SEP, to_remove=removeRegexStr)

    with open(outStatJsonFile, 'w') as f:
        json.dump(statDict, f, ensure_ascii=False, indent = 2)


