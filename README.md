#StanfordNLPForTraditionalChinese
This project is to wrap the function of jopencc (converter for simplified Chinese and traditional Chinese) and Stanford NLP toolkit, and to wrap them into server version. With it, you can easily use Stanford tools by sending http request.

Prerequisite
-------------------------------
<p>Before using this tool, please install the following packages first:<p>

1. stanford segmenter (the whole folder should be inside the project folder, and name it as stanford_segmenter)
2. stanford postagger (the whole folder should be inside the project folder, and name it as stanford_postagger)
3. stanford parser (the whole folder should be inside the project folder, and name it as stanford_parser)
4. installing java(1.8+)
5. modify the Makefile first (the path to JAVA 1.8)


###For Chinese tools:
    make server port=xxxx


###For English tools:
-------------------------------
    maker eng_server port=xxxx
