#StanfordNLPForTraditionalChinese
This project is to wrap the function of [jopencc](https://github.com/copperoxide/jopencc) (converter for simplified Chinese and traditional Chinese) and [Stanford NLP toolkit](http://nlp.stanford.edu/software/index.shtml), and to wrap them into server version. With it, you can easily use Stanford tools by sending http request.

Prerequisite
-------------------------------
<p>Before using this tool, please install the following packages first:<p>

1. [Stanford Chinese Segmenter](http://nlp.stanford.edu/software/segmenter.shtml) (the whole folder should be inside the project folder, and name it as stanford_segmenter)
2. [Stanford POS tagger](http://nlp.stanford.edu/software/tagger.shtml) (the whole folder should be inside the project folder, and name it as stanford_postagger)
3. [Stanford Parser](http://nlp.stanford.edu/software/lex-parser.shtml) (the whole folder should be inside the project folder, and name it as stanford_parser)
4. installing java(1.8+)
5. modify the Makefile first (the path to JAVA 1.8)


###For Chinese tools:
    make server port=xxxx


###For English tools:
-------------------------------
    make eng_server port=xxxx
