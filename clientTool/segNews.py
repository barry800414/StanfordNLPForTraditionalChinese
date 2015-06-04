#!/usr/bin/env python3
import sys
import json
import re

from NLPToolRequests import *
import Punctuation

# default sentence separator
SEP = '[,;\t\n，。；　「」﹝﹞【】《》〈〉（）〔〕『 』\(\)\[\]!?？！\u2019]'

# default new sentence separator
NEW_SEP = ','

# default to-removed punctuation
TO_REMOVE = '[\uF0D8\u0095/=&�+、:：／\|‧]'

# default brackets for fixing them (not to segment)
BRACKETS = [ ('[', ']'), ('(', ')'), ('{', '}'), 
             ('〈', '〉'), ('《', '》'), ('【', '】'),
             ('﹝', '﹞'), ('「','」'), ('『', '』'), 
             ('（','）'), ('〔','〕')]

# segment the news(title & content) & statement
def segLabelNews(newsDict, sep=SEP, new_sep=NEW_SEP, to_remove=TO_REMOVE):
    newsDict['title_seg'] = segText(newsDict['title'], sep, new_sep, to_remove)
    newsDict['content_seg'] = segText(newsDict['content'], sep, new_sep, to_remove)
    return newsDict

# segment all the sentences, dealing with punctuations
# sep: the sentence separators of original contents(for regex)
# new_sep: the new sentence separator
# brackets: the brackets. the content in brackets will not be segemented
def segText(content, sep=SEP, new_sep=NEW_SEP, 
        to_remove=TO_REMOVE, brackets=BRACKETS):
    segText = ''

    # deal with brackets TODO
    '''
    fixedPairs = list()
    for b in brackets:
        regexStr = "%s(.*?)%s" % (b[0], b[1])
        matchObj = re.finditer(regexStr, content)
        for m in matchObj:
            fixedPairs.append((m.start(), m.end()))
    
    if isOverlapping(fixedPairs):
        print('Overlapping!')
        return None
    '''
    sArray = re.split(sep, content)
    for s in sArray:
        s2 = s.strip()
        if len(s2) == 0: #if empty string, skip it
            continue
        
        #print('|%s|' % (s2))
        # segment the string by Stanford NLP segmenter
        result = sendSegmentRequest(s2)
        # remove punctuation
        result = re.sub(to_remove, '', result) 
        # normalizing spaces (N -> 1 space char)
        result = re.sub('[ ]+', ' ', result)
        # remove delimeter chars in front/end of string
        result = result.strip()
        #print('|%s|' % result)
        if len(result) == 0:
            continue
        if len(segText) != 0:
            segText += NEW_SEP + result
        else:
            segText += result
    return segText

# brute force way
def isOverlapping(intervals):
    for i in range(0, len(intervals)):
        s = intervals[i][0]
        e = intervals[i][1]
        for j in range(i+1, len(intervals)):
            s2 = intervals[j][0]
            e2 = intervals[j][1]
            if (s >= s2 and s <= e2) or (e >= s2 and e <= e2):
                return True
    return False

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print('Usage:', sys.argv[0], 'InNewsJson OutNewsJson [PunctuationJson]', file=sys.stderr)
        exit(-1)
    inNewsJsonFile = sys.argv[1]
    outNewsJsonFile = sys.argv[2]

    # read in news file
    with open(inNewsJsonFile, 'r') as f:
        newsDict = json.load(f)
    
    # read in punctuation file
    if len(sys.argv) == 4:
        punctuationJsonFile = sys.argv[3]
        punct = Punctuation.readJsonFile(punctuationJsonFile)
        sepRegexStr = Punctuation.toRegexStr(punct['sep'])
        removeRegexStr = Punctuation.toRegexStr(punct['remove'])
    else:
        sepRegexStr = SEP
        removeRegexStr = TO_REMOVE

    cnt = 0
    for newsId, news in sorted(newsDict.items()):
        segLabelNews(news, sep=sepRegexStr, new_sep=NEW_SEP, to_remove=removeRegexStr)
        cnt += 1
        if (cnt+1) % 10 == 0:
            print('%cProgress: (%d/%d)' % (13, cnt+1, len(newsDict)), end='', file=sys.stderr)

    with open(outNewsJsonFile, 'w') as f:
        json.dump(newsDict, f, ensure_ascii=False, indent = 2)


