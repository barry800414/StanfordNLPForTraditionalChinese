#!/usr/bin/env python3
import sys
import json
import re
import random
import nltk
from NLPToolRequests import *
import Punctuation 


# default new sentence separator
NEW_SEP = ','

# parse the news
def tagEngNews(news, sepSet, rmFirstSet, rmLaterSet, new_sep=NEW_SEP):
    news['content_pos'] = tagEngText(news['content'], sepSet, rmFirstSet, rmLaterSet, new_sep=new_sep)
    return news

# segment all the sentences, dealing with punctuations
# sep: the sentence separators of original contents(for regex)
# new_sep: the new sentence separator
# brackets: the brackets. the content in brackets will not be segemented
def tagEngText(text, sepSet, rmFirstSet, rmLaterSet, new_sep=NEW_SEP):
    result = ''
    #print('\033[1;33moriginal:\033[0m|' + text + '|')
    
    # cleaning the text first
    rmFirstRegex = Punctuation.set2RegexStr(rmFirstSet)
    cleanedText = cleanText(text, rmFirstRegex)
    if cleanedText == None or len(cleanedText) == 0:
        return None
    #print('CleanedSent:|' + cleanText + '|')
    sentList = splitSent(cleanedText, sepSet)
   
    # for each sentence
    for i, sent in enumerate(sentList):
        #print('|' + sent + '|')
        # tag the sentence, return a string with tags
        response = sendTagRequest(sent, seg=True)
        if response == None:
            print('Tagging error', file=sys.stderr)
            continue

        # remove original sentence separator
        response = removeSepStr(response, rmLaterSet)
        #print('\tTagged:|' + response + '|')
        if len(result) == 0:
            result = response
        else:
            result = result + new_sep + response
    #print('\033[0;32mTagging Result:\033[0m|' + result + '|\n')    
    return result

# removing urls and some punctuations
def cleanText(text, rmFirstRegex):
    cleanedText = removeUrls(text)
    cleanedText = re.sub(rmFirstRegex, " ", cleanedText)
    return cleanedText.strip()

def removeUrls(string):
    pattern = 'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+'
    return re.sub(pattern, "", string)

def removeSepStr(string, sepSet):
    outStr = ''
    entry = string.strip().split(' ')
    for e in entry:
        (w, t) = e.split('/')
        if w not in sepSet:
            if len(outStr) == 0:
                outStr = str(e)
            else:
                outStr = outStr + ' ' + e
    return outStr

# split english sentence by nltk tokenizer, and normalize the tokens
def splitSent(text, sepSet):
    tokens = nltk.word_tokenize(text)
    sentList = list()
    sent = ''
    for i, t in enumerate(tokens):
        t = normalizeToken(t)
        if t in sepSet and len(sent) != 0:
            sentList.append(sent)
            sent = ''
        else:
            if len(sent) == 0:
                sent = str(t)
            else:
                sent = sent + ' ' + t
    if len(sent) != 0:
        sentList.append(sent)
    return sentList
    
# converting "," to "" if the token is a number
def normalizeToken(token):
    if token.find(",") == -1 or token == ',':
        return token
    return token.replace(",", "")

    #try:
    #    print('original:|' + token + '|')
    #    n = token.replace(",", "")
    #    int(n)
    #    print('int:|' + n + '|')
    #    return n
    #except:
    #    print('new:|' + n + '|')
    #    return n

def mergeTokens(tokens):
    outStr = ''
    for i, t in enumerate(tokens):
        if i == 0:
            outStr = str(t)
        else:
            outStr = outStr + ' ' + t
    return outStr


if __name__ == '__main__':
    if len(sys.argv) != 4:
        print('Usage:', sys.argv[0], 'InSegNewsJson OutTaggedNewsJson PunctuationJson', file=sys.stderr)
        exit(-1)

    inNewsJsonFile = sys.argv[1]
    outNewsJsonFile = sys.argv[2]

    # read in news file
    with open(inNewsJsonFile, 'r') as f:
        newsDict = json.load(f)
    
    # read in punctuation file
    punctuationJsonFile = sys.argv[3]
    punct = Punctuation.readJsonFile(punctuationJsonFile)
    sepSet= set(punct['sep'].keys())
    rmFirstSet = set(punct['remove_first'].keys())
    rmLaterSet = set(punct['remove_later'].keys())

    cnt = 0
    newNewsDict = dict()
    removedNewsId = set()
    for newsId, news in sorted(newsDict.items(), key=lambda x:x[0]):
        tagEngNews(news, sepSet, rmFirstSet, rmLaterSet, new_sep=NEW_SEP)
        # after preprocessing, the document has no content, thus it will be removed
        if news['content_pos'] == None or len(news['content_pos']) == 0:
            removedNewsId.add(newsId)
            print(newsId, file=sys.stderr)
            print(news, file=sys.stderr)
        else:
            newNewsDict[newsId] = news
        
        cnt += 1
        if cnt % 10 == 0:
            print('Progress: (%d/%d)' % (cnt, len(newsDict)), file=sys.stderr)
            
    print('news are removed:', removedNewsId)
    with open(outNewsJsonFile, 'w') as f:
        json.dump(newNewsDict, f, ensure_ascii=False, indent = 2)


