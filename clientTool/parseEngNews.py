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
def constParseEngNews(news, sepSet, rmFirstSet, rmLaterSet, new_sep=NEW_SEP):
    r = constParseEngText(news['content'], sepSet, rmFirstSet, rmLaterSet, new_sep=new_sep)
    if r == None:
        return False
    else:
        news['content_constituent'] 
        return True

def constParseEngText(text, sepSet, rmFirstSet, rmLaterSet, new_sep=NEW_SEP):
    result = list()
    #print('\033[1;33moriginal:\033[0m|' + text + '|')
    # remove some punctuation first
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
        response = sendConstParseRequest(sent, seg=True)
        if response == None:
            print('Parsing error', file=sys.stderr)
            continue
        (nodes, edges) = response
        #print(nodes)
        #print(edges)

        if len(nodes) != 0 and len(edges) != 0:
            result.append({'nodes': nodes, 'edges': edges})

    #print('\033[0;32mTagging Result:\033[0m|' + result + '|\n')
    return result


# parse the news
def depParseEngNews(news, sepSet, rmFirstSet, rmLaterSet, 
        new_sep=NEW_SEP, draw=False, fileFolder=None, fileName=''):
    r = depParseEngText(news['content'], sepSet, rmFirstSet, rmLaterSet, 
            new_sep=new_sep, draw=draw, fileFolder=fileFolder, fileName='content')
    if r == None:
        return False
    else:
        news['content_dep'] = r
        return True

def depParseEngText(text, sepSet, rmFirstSet, rmLaterSet, 
        new_sep=NEW_SEP, draw=False, fileFolder=None, fileName=''):
    result = list()
    #print('\033[1;33moriginal:\033[0m|' + text + '|')
    # remove some punctuation first
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
        response = sendDepParseRequest(sent, seg=True, draw=draw, 
                fileFolder=fileFolder, fileName=fileName+"_%04d_%s" %(
                    i,sent))
        if response == None:
            print('Parsing error', file=sys.stderr)
            continue
        #print(response)
        #print(edges)

        if len(response) != 0:
            result.append({'tdList': response})

    #print('\033[0;32mTagging Result:\033[0m|' + result + '|\n')
    return result



# parse the news
def parseEngNews(news, sepSet, rmFirstSet, rmLaterSet, 
        new_sep=NEW_SEP, draw=False, fileFolder=None, fileName=''):
    
    r = parseEngText(news['content'], sepSet, rmFirstSet, rmLaterSet, 
            new_sep=new_sep, draw=draw, fileFolder=fileFolder, 
            fileName='content')
    if r != None:
        (news['content_constituent'], news['content_dep']) = r
        return True
    else:
        return False

def parseEngText(text, sepSet, rmFirstSet, rmLaterSet, 
        new_sep=NEW_SEP, draw=False, fileFolder=None, fileName=''):
    constResult = list()
    depResult = list()
    
    #print('\033[1;33moriginal:\033[0m|' + text + '|')
    # remove some punctuation first
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
        response = sendParseRequest(sent, seg=True, draw=draw, 
                fileFolder=fileFolder, fileName=fileName+"_%04d_%s" %(
                    i,sent))
        if response == None:
            print('Parsing error', file=sys.stderr)
            continue
        
        (constR, depR) = response
        #print('ConstR:', constR)
        #print('depR:', depR)
        constResult.append({'nodes': constR[0], 'edges': constR[1]})
        depResult.append({'tdList': depR})

    #print('\033[0;32mTagging Result:\033[0m|' + result + '|\n')
    return (constResult, depResult)

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

def mergeTokens(tokens):
    outStr = ''
    for i, t in enumerate(tokens):
        if i == 0:
            outStr = str(t)
        else:
            outStr = outStr + ' ' + t
    return outStr





if __name__ == '__main__':
    if len(sys.argv) != 5:
        print('Usage:', sys.argv[0], 'InSegNewsJson OutTaggedNewsJson PunctuationJson Dep/Const/Dep_Const', file=sys.stderr)
        exit(-1)

    inNewsJsonFile = sys.argv[1]
    outNewsJsonFile = sys.argv[2]
    punctuationJsonFile = sys.argv[3]
    parseType = sys.argv[4]
    assert parseType == 'Dep' or parseType == 'Const' or parseType == 'Dep_Const'

    # read in news file
    with open(inNewsJsonFile, 'r') as f:
        newsDict = json.load(f)
    
    # read in punctuation file
    punct = Punctuation.readJsonFile(punctuationJsonFile)
    sepSet= set(punct['sep'].keys())
    rmFirstSet = set(punct['remove_first'].keys())
    rmLaterSet = set(punct['remove_later'].keys())

    cnt = 0
    newNewsDict = dict()
    removedNewsId = set()
    for newsId, news in sorted(newsDict.items(), key=lambda x:x[0])[0:1]:
        if parseType == 'Dep':
            r = depParseEngNews(news, sepSet, rmFirstSet, rmLaterSet, new_sep=NEW_SEP, draw=True, fileFolder=newsId)
            if r:
                newNewsDict[newsId] = news
            else:
                removedNewsId.add(newsId)
        elif parseType == 'Const':
            r = constParseEngNews(news, sepSet, rmFirstSet, rmLaterSet, new_sep=NEW_SEP)
            if r:
                newNewsDict[newsId] = news
            else:
                removedNewsId.add(newsId)
        elif parseType == 'Dep_Const':
            r = parseEngNews(news, sepSet, rmFirstSet, rmLaterSet, new_sep=NEW_SEP, draw=True, fileFolder=newsId)
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


