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
        #result = result.strip()
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

def printSegNews(news, sentSep=NEW_SEP, outfile=sys.stdout):
    for sent in news['title_seg'].split(sentSep):
        print(sent, end=' ', file=outfile)
    for sent in news['content_seg'].split(sentSep):
        print(sent, end=' ', file=outfile)
    print('', file=outfile)

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print('Usage:', sys.argv[0], 'InNewsJsonFile OutNews [PunctuationJson]', file=sys.stderr)
        exit(-1)
    inNewsJsonFile = sys.argv[1]
    outNewsFile = sys.argv[2]

    # read in news file
    print('read in news file ...', file=sys.stderr)
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

    with open(outNewsFile, 'w') as f:
        cnt = 0
        print('start segmenting news ...', file=sys.stderr)
        for newsId, news in newsDict.items():
            segLabelNews(news, sep=sepRegexStr, new_sep=NEW_SEP, to_remove=removeRegexStr)
            printSegNews(news, outfile=f)
            cnt = cnt + 1
            if cnt % 10 == 0:
                print('Progress: (%d/%d)' % (cnt, len(newsDict)), file=sys.stderr)
