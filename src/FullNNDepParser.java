
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.TypedDependency;


import java.io.StringReader;
import java.util.List;
import java.util.Collection;

import jopencc.ZhtZhsConvertor;


/**
 * Demonstrates how to first use the tagger, then use the NN dependency
 * parser. Note that the parser will not work on untagged text.
 *
 * @author Jon Gauthier
 */
public class FullNNDepParser {
    public static void main(String[] args) {
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

        //Initialize the dependency parser
        FullNNDepParser fdp = new FullNNDepParser(Lang.ZHS, tagger, 
            seg, convertor);

        System.out.println("Dependency Format: reln gov_index gov_word gov_tag dep_index dep_word dep_tag");

        //String text = "I can almost always tell when movies use fake dinosaurs.";
        String untokenizedSent = "這是一個測試用的句子";
        List<TypedDependency> tdList = fdp.parseUntokenizedSent(untokenizedSent, Lang.ZHT, Lang.ZHT);
        System.out.println(untokenizedSent);
        System.out.println(DepPrinter.TDsToString(tdList));
    }

    public DependencyParser parser = null; 
    public FullPOSTagger tagger = null;
    public Segmenter segmenter = null;
    public ZhtZhsConvertor convertor = null;
    public String[] tokenizedSentBuffer = null;
    public int lang = 0;

    private String modelPath = DependencyParser.DEFAULT_MODEL;

    public FullNNDepParser(int lang){
        this.lang = lang;
        if(lang == Lang.ENG){
            modelPath = "edu/stanford/nlp/models/parser/nndep/PTB_CoNLL_params.txt.gz";
        }
        else if(lang == Lang.ZHS){
            modelPath = "edu/stanford/nlp/models/parser/nndep/CTB_CoNLL_params.txt.gz"; 
        }
        parser = DependencyParser.loadFromModelFile(modelPath);
    }

    public FullNNDepParser(int lang, Segmenter segmenter){
        this(lang);
        this.segmenter = segmenter;
    }

    public FullNNDepParser(int lang, Segmenter segmenter, 
        ZhtZhsConvertor convertor){
        this(lang);
        this.segmenter = segmenter;
        this.convertor = convertor;
    }

    public FullNNDepParser(int lang, FullPOSTagger tagger, 
        Segmenter segmenter, ZhtZhsConvertor convertor){
        this(lang);
        this.tagger = tagger;
        this.segmenter = segmenter;
        this.convertor = convertor;
    }

    //get the dependency parsed results from "tokenized" and "ZHS" sentence
    public List<TypedDependency> parseTokenizedSent(String sent, int inLang, int outLang){
        //TODO: may be inconsistent to tagger
        tokenizedSentBuffer = sent.split(" ");
        List<TaggedWord> tagged = tagger.tagTokenizedSent(sent, inLang, outLang);
        GrammaticalStructure gs = parser.predict(tagged);
        // Print typed dependencies
        //System.err.println(gs);
        //System.err.println(gs.allTypedDependencies());
        //System.err.println(gs.typedDependencies());
        return gs.typedDependenciesCCprocessed();
    }

    public List<TypedDependency> parseUntokenizedSent(String untokenizedSent, int inLang, int outLang){
        if(inLang == Lang.ENG){
            System.err.println("Untokenized English sentences parsing is not supported now");
            return null;    
        }

        // tokenize the sentence & converting the language
        tokenizedSentBuffer = segmenter.segmentStr(untokenizedSent, inLang, Lang.ZHS);

        // dependency parsing
        List<TypedDependency> tdList = parseTokenizedSent(mergeStr(tokenizedSentBuffer), Lang.ZHS, Lang.ZHS);

        // convert the language if necessary
        if(outLang == Lang.ZHT){
            //TODO
            return tdList;
        }
        return tdList;
    }

    public String getTokenizedSentBuffer(){
        return mergeStr(tokenizedSentBuffer);
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

}
