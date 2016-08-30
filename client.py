import requests
import json
import sys


class Client():
	def __init__(self, api_url_prefix="http://localhost", port=8011):
		self.api_url = '%s:%d' % (api_url_prefix, port)
		print(self.api_url)
	
	# segment zht string to zht string, whitespace will be the separator
	def sendSegmentRequest(self, sentence):
		api_url = '%s/segmenter' % (self.api_url)
		payload = {
			's': sentence        
		}
		r = requests.get(api_url, params = payload)
		if r.status_code == 200:
			return r.text
		else:
			return None

	# typedDependency format:  reln gov_index gov_word gov_tag dep_index dep_word dep_tag
	def sendDepParseRequest(self, sentence, seg=False, draw=False, fileFolder=None, 
			fileName=None, returnTokenizedSent=False):
		api_url = '%s/pcfg_dep' % (self.api_url)
		
		if seg == False:
			payload = { 's': sentence }
		else:
			payload = { 'seg_s': sentence }

		if draw == True and fileFolder != None and fileName != None:
			payload['f_folder'] = fileFolder
			payload['f_name'] = fileName
			payload['draw'] = True
			
		r = requests.get(api_url, params = payload)
		if r.status_code == 200:
			lines = r.text.strip().split('\n')
			if not seg:
				tokenizedSent = lines[0]
				typedDependencies = lines[1:]
			else:
				typedDependencies = lines

			if returnTokenizedSent and not seg:
				return (tokenizedSent, typedDependencies)
			else:
				return typedDependencies
		else:
			return None

	#constituent parsing by stanford pcfg parser
	def sendConstParseRequest(self, sentence, seg=False, returnTokenizedSent=False):
		api_url = '%s/pcfg' % (self.api_url)
		
		if seg == False:
			payload = { 's': sentence }
		else:
			payload = { 'seg_s': sentence }
			
		r = requests.get(api_url, params = payload)
		if r.status_code == 200:
			lines = r.text.strip().split('\n')
			if not seg: 
				tokenizedSent = lines[0]
				(nodeLines, edgeLines) = lines2Const(lines[1:])
			else:
				(nodeLines, edgeLines) = lines2Const(lines[1:])

			if returnTokenizedSent and not seg:
				return (tokenizedSent, nodeLines, edgeLines)
			else:
				return (nodeLines, edgeLines)
		else:
			return None

	def sendParseRequest(sentence, seg=False, draw=False, fileFolder=None, 
			fileName=None, returnTokenizedSent=False):
		api_url = 'http://localhost:8000/pcfg_all'
		
		if seg == False:
			payload = { 's': sentence }
		else:
			payload = { 'seg_s': sentence }

		if draw and fileFolder != None and fileName != None:
			payload['f_folder'] = fileFolder
			payload['f_name'] = fileName
			payload['draw'] = True
			
		r = requests.get(api_url, params = payload)
		if r.status_code == 200:
			lines = r.text.strip().split('\n')
			entry = lines[0].split(' ')
			constNum = int(entry[0])
			depNum = int(entry[1])
			#print('constNum:', constNum, 'depNum:', depNum)
			if not seg:
				assert len(lines) == constNum + depNum + 2
				tokenizedSent = lines[1]
				(nodeLines, edgeLines) = lines2Const(lines[2:2+constNum])
				typedDependencies = lines[2+constNum:2+constNum+depNum]
			else:
				assert len(lines) == constNum + depNum + 1
				(nodeLines, edgeLines) = lines2Const(lines[1:1+constNum])
				typedDependencies = lines[1+constNum:1+constNum+depNum]
					
			if returnTokenizedSent and not seg:
				return (tokenizedSent, (nodeLines, edgeLines), typedDependencies)
			else:
				return ((nodeLines, edgeLines), typedDependencies)
		else:
			return None


	def sendTagRequest(self, sentence, seg=False):
		api_url = '%s/pos' % (api_url)
		if seg == False:
			payload = { 's': sentence }
		else:
			payload = { 'seg_s': sentence }

		r = requests.get(api_url, params = payload)
		if r.status_code == 200:
			return r.text
		else:
			return None

		
def lines2Const(lines):
    entry = lines[0].strip().split(' ')
    nodesNum = int(entry[0])
    edgesNum = int(entry[1])
    assert (nodesNum + edgesNum + 1) == len(lines)
    nodeLines = lines[1:1+nodesNum]
    edgeLines = lines[1+nodesNum:]
    return (nodeLines, edgeLines)

	
client = Client("http://140.112.31.187", 8011)
	
print(client.sendConstParseRequest("軟體工程師"))


#s = "I hate this product, so I don't want to buy it."
#s = "我反對核四"
#print(sendDepParseRequest(s, seg=False, draw=True, fileName=s, fileFolder='/home/r02922010/codes/AgreementPrediction/method/nlp'))

		
#print(sendTagRequest("He has cars", seg=True))
#print(sendTagRequest("It 's my fault , ! ; ? not your business .", seg=True))
#print(sendTagRequest("我是一個人"))

#print(sendSegmentRequest("測試 我是一個句子"))

#print(sendDepParseRequest("He has cars", seg=True))
#print(sendDepParseRequest("It 's my fault , not your business .", seg=True))
#print(sendDepParseRequest("我是一個人", draw = True, fileFolder='test'))
#print(sendDepParseRequest("這是一個測試用的句子"))
#print(sendDepParseRequest("台灣應廢除死刑"))	

#s = "I hate this product"
#print(sendDepParseRequest(s, seg=True, draw=True, fileName=s, fileFolder='./'))
#s = "This is a beautiful, useful and cheap product."
#print(sendDepParseRequest(s, seg=True, draw=True, fileName=s, fileFolder='./'))
#s = "This is a beautiful ring in the box."
#print(sendDepParseRequest(s, seg=True, draw=True, fileName=s, fileFolder='./'))

