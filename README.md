#StanfordNLP For Traditional Chinese
This project is to combine [jopencc](https://github.com/copperoxide/jopencc) (a converter to convert between simplified Chinese and traditional Chinese) and [Stanford NLP toolkit](http://nlp.stanford.edu/software/index.shtml), and to provide a server version to avoid repeated loading model file. With this tool, you can easily use Stanford tools to deal with traditional chinese document by sending http request.

Prerequisite
-------------------------------
<p>Before using this tool, please install the following packages first:<p>

1. [Stanford Chinese Segmenter](http://nlp.stanford.edu/software/segmenter.shtml) (path: {PROJECT_FOLDER}/stanford_segmenter)
2. [Stanford POS tagger](http://nlp.stanford.edu/software/tagger.shtml) (path: {PROJECT_FOLDER}/stanford_postagger)
3. [Stanford Parser](http://nlp.stanford.edu/software/lex-parser.shtml) (path: {PROJECT_FOLDER}/stanford_parser)
4. installing java(1.8+)
5. modify path to Java 1.8 in Makefile


Launch server
---------------------------------
* Run server for Chinese:
    <code>make server port=xxxx</code>

* Run server for English:
    <code>make eng_server port=xxxx</code>

Usage
---------------------------------
### Word Segementer <br>
* API url: <code>http://localhost:{$PORT}/segementer</code>

### POS Tagger <br>
* API url: <code>http://localhost:{$PORT}/pos</code>

### Dependency Parser <br>
* API url: <code>http://localhost:{$PORT}/pcfg_dep</code>

### Constituent Parser
* API url: <code>http://localhost:{$PORT}/pcfg</code>

