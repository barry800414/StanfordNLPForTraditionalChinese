PACKAGE=./stanford_segmenter/*:./stanford_parser/*:./stanford_postagger/*:./jopencc/
JAVA_PATH=

#convertor (ZHT <-> ZHS)
convertor_compile:
	$(JAVA_PATH)javac -cp "./jopencc/src" -d ./jopencc ./jopencc/src/jopencc/ZhtZhsConvertor.java 
	
convertor: convertor_compile
	$(JAVA_PATH)java -cp "./jopencc" jopencc.ZhtZhsConvertor ./jopencc

#segmenter
segmenter_compile: 
	$(JAVA_PATH)javac -cp "$(PACKAGE):./bin:./src" -d ./bin ./src/Segmenter.java

segmenter: segmenter_compile
	$(JAVA_PATH)java -cp "$(PACKAGE):./bin" Segmenter 

# POS tagger
tagger_compile: 
	$(JAVA_PATH)javac -cp "$(PACKAGE):./bin:./src" -d ./bin ./src/FullPOSTagger.java

tagger: tagger_compile
	$(JAVA_PATH)java -cp "$(PACKAGE):./bin:./src" FullPOSTagger

# full neural network dependency parser
nndep_parser_compile: 
	$(JAVA_PATH)javac -cp "$(PACKAGE):./bin:./src" -d ./bin ./src/FullNNDepParser.java

nndep_parser: nndep_parser_compile
	$(JAVA_PATH)java -cp "$(PACKAGE):./bin:./src" FullNNDepParser

# full PCFG parser (for constituent & dependency parsing) 
parser_compile: convertor_compile
	$(JAVA_PATH)javac -cp "$(PACKAGE):./bin:./src:./lib" -d ./bin ./src/FullPCFGParser.java

parser: parser_compile
	$(JAVA_PATH)java -cp "$(PACKAGE):./bin:./lib" FullPCFGParser

#server_compile: segmenter_compile tagger_compile nndep_parser_compile parser_compile
server_compile: 
	$(JAVA_PATH)javac -cp "$(PACKAGE):./bin:./src:./lib" -d ./bin ./src/NLPToolServer.java

server: server_compile
	$(JAVA_PATH)java -cp "$(PACKAGE):./bin:./lib" NLPToolServer $(port)

eng_server_compile: 
	$(JAVA_PATH)javac -cp "$(PACKAGE):./bin:./src:./lib" -d ./bin ./src/EngNLPToolServer.java

eng_server: eng_server_compile
	$(JAVA_PATH)java -cp "$(PACKAGE):./bin:./lib" EngNLPToolServer

