
Before the following commands, you have to open NLPToolServer at the same machine first.
Please go to $(PROJECT_FOLDER)/nlp_tool, then 
    make server

Segment news content
-------------------
    ./segNews.py news.json segNews.json

Tag news content (POS-tagging)
-------------------
    ./tagNews.py segNews.json taggedNews.json

Parse news content (Dependency parsing)
-------------------
    ./parseNews.py news.json parsedNews.json

Segment & tag & parse the statement
-------------------
    ./statPreprocess.py statement.json preStat.json



Tag Eng Debate corpus
-------------------
    python3 tagEngNews.py ../engCorpus/engNews.json ../engCorpus/taggedEngNews.json engPunctuation.json
