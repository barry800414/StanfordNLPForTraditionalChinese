#!/usr/bin/env python3
import sys
import json
import re
from NLPToolRequests import *
import Punctuation

# default sentence separator
#SEP = '[;\t\n。；　「」﹝﹞【】《》〈〉（）〔〕『 』\(\)\[\]!?？！]'

SEP = '[,;\t\n，。；　「」﹝﹞【】《》〈〉（）〔〕『 』\(\)\[\]!?？！]'

# default new sentence separator
NEW_SEP = ','

# default to-removed punctuation
TO_REMOVE = '[\uF06E\uF0D8\u0095/=&�+:：／\|‧]'
#TO_REMOVE = '[\uF0D8\u0095/=&�+、:：／\|‧]'

# default brackets for fixing them (not to segment)
BRACKETS = [ ('[', ']'), ('(', ')'), ('{', '}'), 
             ('〈', '〉'), ('《', '》'), ('【', '】'),
             ('﹝', '﹞'), ('「','」'), ('『', '』'), 
             ('（','）'), ('〔','〕')]

allRelationSet = set()

# parse the news
def depParseNews(news, draw=False, fileFolder=None, sep=SEP, 
        new_sep=NEW_SEP, to_remove=TO_REMOVE):
    #news['title_dep'] = depParseText(news['title'], draw=draw, 
    #        fileFolder=fileFolder, fileName='title', sep=sep,
    #        new_sep=new_sep, to_remove=to_remove)
    news['content_dep'] = depParseText(news['content'], draw=draw, 
            fileFolder=fileFolder, fileName='content', sep=sep,
            new_sep=new_sep, to_remove=to_remove)
    return True

# segment all the sentences, dealing with punctuations
# sep: the sentence separators of original contents(for regex)
# new_sep: the new sentence separator
def depParseText(text, draw=False, fileFolder=None, fileName='', 
        sep=SEP, new_sep=NEW_SEP, to_remove=TO_REMOVE, brackets=BRACKETS):
    result = list() 
    sentArray = re.split(sep, text)
    
    # for each sentence
    for i, sent in enumerate(sentArray):
        cleanSent = re.sub(to_remove, " ", sent)
        cleanSent = cleanSent.strip()
        if len(cleanSent) > 0: #if empty string, skipped
            tmp = dict()
            tmp['sent'] = cleanSent
            # for debugging
            '''
            print(cleanSent, end=' ')
            for c in cleanSent:
                print(hex(ord(c)), end=' ')
            print('')
            '''
            # parse the sentence, return an array of typed dependencies
            (tmp['seg_sent'], tmp['tdList']) = sendDepParseRequest(cleanSent, 
                    draw=draw, fileFolder=fileFolder, 
                    fileName=fileName+"_%04d_%s" %(i,cleanSent),
                    returnTokenizedSent=True)

            # for debugging
            for td in tmp['tdList']:
                #print(td)
                allRelationSet.add(td.split(" ")[0])
            result.append(tmp)
    return result


# segment all the sentences, dealing with punctuations
# sep: the sentence separators of original contents(for regex)
# new_sep: the new sentence separator
def constParseText(text, sep=SEP, new_sep=NEW_SEP, to_remove=TO_REMOVE, 
        brackets=BRACKETS):
    result = list() 
    sentArray = re.split(sep, text)
    
    # for each sentence
    for i, sent in enumerate(sentArray):
        cleanSent = re.sub(to_remove, " ", sent)
        cleanSent = cleanSent.strip()
        if len(cleanSent) > 0: #if empty string, skipped
            tmp = dict()
            tmp['sent'] = cleanSent
            # for debugging
            '''
            print(cleanSent, end=' ')
            for c in cleanSent:
                print(hex(ord(c)), end=' ')
            print('')
            '''
            # parse the sentence, return an array of typed dependencies
            (tmp['seg_sent'], nodes, edges) = sendConstParseRequest(cleanSent, 
                    returnTokenizedSent=True)
            tmp['nodes'] = nodes
            tmp['edges'] = edges
            result.append(tmp)
    return result

# parse the news
def constParseNews(news, sep=SEP, new_sep=NEW_SEP, to_remove=TO_REMOVE) :
    #news['title_constituent'] = constParseText(news['title'], sep=sep,
    #        new_sep=new_sep, to_remove=to_remove)
    news['content_constituent'] = constParseText(news['content'], sep=sep,
            new_sep=new_sep, to_remove=to_remove)
    return True

# segment all the sentences, dealing with punctuations
# sep: the sentence separators of original contents(for regex)
# new_sep: the new sentence separator
def parseText(text, draw=False, fileFolder=None, fileName='', 
        sep=SEP, new_sep=NEW_SEP, to_remove=TO_REMOVE):
    constResult = list()
    depResult = list()
    sentArray = re.split(sep, text)
    
    # for each sentence
    for i, sent in enumerate(sentArray):
        cleanSent = re.sub(to_remove, " ", sent)
        cleanSent = cleanSent.strip()
        if len(cleanSent) == 0: #if empty string, skipped
            continue
        
        # for debugging
        '''
        print(cleanSent, end=' ')
        for c in cleanSent:
            print(hex(ord(c)), end=' ')
        print('')
        '''
        response = sendParseRequest(cleanSent, seg=False, draw=draw, 
            fileFolder=fileFolder, fileName=fileName+"_%04d_%s" %(
                i,sent), returnTokenizedSent=True)
    
        if response == None:
            print('Parsing error', file=sys.stderr)
            continue
        
        (tokenizedSent, constR, depR) = response
        #print('tokenizedSent:', tokenizedSent)
        #print('ConstR:', constR)
        #print('depR:', depR)
        constResult.append({'nodes': constR[0], 'edges': constR[1], 'seg': tokenizedSent, 'sent': cleanSent})
        depResult.append({'tdList': depR, 'seg':tokenizedSent, 'sent': cleanSent})
        
    return (constResult, depResult)

# parse the news
def parseNews(text, draw=False, fileFolder=None, fileName='', 
        sep=SEP, new_sep=NEW_SEP, to_remove=TO_REMOVE):
    fileName = 'content'
    r = parseText(news['content'], draw, fileFolder, fileName, sep, new_sep, to_remove)
    if r != None:
        (news['content_constituent'], news['content_dep']) = r
        return True
    else:
        return False


if __name__ == '__main__':
    if len(sys.argv) < 4:
        print('Usage:', sys.argv[0], 'InNewsJson OutNewsJson Dep/Const/Dep_Const PunctuationJson', file=sys.stderr)
        exit(-1)
    inNewsJsonFile = sys.argv[1]
    outNewsJsonFile = sys.argv[2]
    parseType = sys.argv[3]
    assert parseType in ['Dep', 'Const', 'Dep_Const']

    with open(inNewsJsonFile, 'r') as f:
        newsDict = json.load(f)
    
    # read in punctuation file
    if len(sys.argv) == 5:
        punctuationJsonFile = sys.argv[4]
        punct = Punctuation.readJsonFile(punctuationJsonFile)
        sepRegexStr = Punctuation.toRegexStr(punct['sep'])
        removeRegexStr = Punctuation.toRegexStr(punct['remove'])
    else:
        sepRegexStr = SEP
        removeRegexStr = TO_REMOVE

    cnt = 0
    newNewsDict = dict()
    removedNewsId = set()
    for newsId, news in sorted(newsDict.items()):
        if parseType == 'Dep':
            r = depParseNews(news, draw=True, fileFolder=newsId, 
                    sep=sepRegexStr, new_sep=NEW_SEP, 
                    to_remove=removeRegexStr)
            if r:
                newNewsDict[newsId] = news
            else:
                removedNewsId.add(newsId)

        elif parseType == 'Const':
            r = constParseNews(news, sep=sepRegexStr, new_sep=NEW_SEP, 
                    to_remove=removeRegexStr)
            if r:
                newNewsDict[newsId] = news
            else:
                removedNewsId.add(newsId)
        elif parseType == 'Dep_Const':
            r = parseNews(news, sep=sepRegexStr, new_sep=NEW_SEP, 
                    to_remove=removeRegexStr)
            if r:
                newNewsDict[newsId] = news
            else:
                removedNewsId.add(newsId)

        cnt += 1
        if cnt % 10 == 0:
            print('Progress: (%d/%d)' % (cnt, len(newsDict)), file=sys.stderr)

    print('news are removed:', removedNewsId)
    with open(outNewsJsonFile, 'w') as f:
        json.dump(newNewsDict, f, ensure_ascii=False, indent = 2)

