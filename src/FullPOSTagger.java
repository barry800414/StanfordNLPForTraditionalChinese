
import java.io.StringReader;
import java.util.List;

import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.process.DocumentPreprocessor;

import jopencc.ZhtZhsConvertor;

public class FullPOSTagger {

    public static void main(String[] args) throws Exception {
        //initialize the converter
        System.err.println(" ===== Initializing Convertor =====");
        ZhtZhsConvertor convertor = new ZhtZhsConvertor("./jopencc");

        //initialize the segmenter
        System.err.println(" ===== Initializing Segmentor =====");
        Segmenter seg = new Segmenter("./stanford_segmenter", convertor);

        //initialize the pos-tagger 
        System.err.println(" ===== Initializing pos-tagger =====");
        FullPOSTagger tagger = new FullPOSTagger("./stanford_postagger", Lang.ZHS, 
            seg, convertor);

        String untokenizedSent = "這是一個測試用的句子";
        List<TaggedWord> tagged = tagger.tagUntokenizedSent(untokenizedSent, 
            Lang.ZHT, Lang.ZHT);

        System.out.println(tagged);
        for(TaggedWord tw: tagged){
            //value: word,   tag: POS tagger
            System.out.println(tw.value() + ':' + tw.tag());
        }
    }

    public int lang;
    public MaxentTagger tagger = null;
    private Segmenter segmenter = null;
    private ZhtZhsConvertor convertor = null;

    public FullPOSTagger(String modelDir, int lang){
        this.lang = lang;
        String modelPath = null;
        if(lang == Lang.ENG){
            modelPath = modelDir + "/models/english-left3words-distsim.tagger";
        }
        else if(lang == Lang.ZHS){
            modelPath = modelDir + "/models/chinese-distsim.tagger";
        }
        tagger = new MaxentTagger(modelPath);
    }

    public FullPOSTagger(String modelDir, int lang, Segmenter segmenter){
        this(modelDir, lang);
        this.segmenter = segmenter;
    }

    public FullPOSTagger(String modelDir, int lang, Segmenter segmenter, 
        ZhtZhsConvertor convertor){
        this(modelDir, lang);
        this.segmenter = segmenter;
        this.convertor = convertor;
    }


    //get the dependency parsed results from "tokenized" and "ZHS" sentence
    public List<TaggedWord> tagTokenizedSent(String tokenizedSent, int inLang, int outLang){ //TODO
        String sent;
        if(inLang == Lang.ZHT){
            sent = convertor.convertToZhs(tokenizedSent);
        }
        else{
            sent = tokenizedSent;
        }
        DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(sent));
        List<TaggedWord> tagged = null;
        for (List<HasWord> sentence : tokenizer){
            tagged = tagger.tagSentence(sentence);
            break;
            //TODO, assume only one sentence
        }

        if(inLang != Lang.ENG && outLang == Lang.ZHT){
            wordToZht(tagged);   
        }
        return tagged;
    }

    public List<TaggedWord> tagUntokenizedSent(String untokenizedSent, int inLang, int outLang){
        // tokenize the sentence & converting the language
        String[] sent = null;
        sent = segmenter.segmentStr(untokenizedSent, inLang, Lang.ZHS);

        // dependency parsing
        List<TaggedWord> result = tagTokenizedSent(mergeStr(sent), inLang, outLang);

        // convert the language if necessary
        if(outLang == Lang.ZHT){
            //TODO
            //setValue()
            return result;
        }
        return result;
    }

    private String mergeStr(String[] str){
        String del = " ";
        if(str.length > 0){
            String result = "";
            for(int i = 0; i < str.length -1 ; i++){
                result = result + str[i] + del;
            }
            result = result + str[str.length - 1];
            return result;
        }
        else{
            return null;
        }
    }

    private List<TaggedWord> wordToZht(List<TaggedWord> zhsTaggedWord){
        for(TaggedWord tw: zhsTaggedWord){
            tw.setWord(convertor.convertToZht(tw.word()));
        }
        return zhsTaggedWord;
    }

}
