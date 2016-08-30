
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.trees.international.pennchinese.ChineseTreebankLanguagePack;

import com.chaoticity.dependensee.Node;
import jopencc.ZhtZhsConvertor;

/*
	The parser which contains zht<->zht converter, 
	stanford segmenter and stanford parser.
    This parser can be used to output constituent
    parsing and dependency parsing.
	Date: 2014/12/15
    Last Update: 2015/02/28
*/
public class FullPCFGParser extends PCFGParser{
	public static void main(String[] args){
		//initialize the converter
        System.err.println(" ===== Initializing Convertor =====");
        ZhtZhsConvertor convertor = new ZhtZhsConvertor("./jopencc");

        //initialize the segmenter
        System.err.println(" ===== Initializing Segmentor =====");
        Segmenter seg = new Segmenter("./stanford_segmenter", convertor);
        
        //intialize the parser
        System.err.println(" ===== Initializing PCFGParser =====");
        FullPCFGParser fpp = new FullPCFGParser(Lang.ZHS, seg, convertor);

        String untokenizedSent = "今天天氣很好，我早餐吃蛋餅";
        Tree parse = fpp.parseUntokenizedSent(untokenizedSent, Lang.ZHT, Lang.ZHT);
        System.out.println("Constituent parsing:");
        parse.pennPrint();
        System.out.println(TreePrinter.treeToString(parse));
        
        System.out.println("Dependency parsing:");
        List<TypedDependency> tdl = fpp.toTypedDependency(parse);
        //FIXME: Although the words in this tree has been converted to ZHT,
        //it seems that when we convert tree to typed dependencies, the
        //words in TDs remains ZHS
        tdl = fpp.wordToZht(tdl);
        System.out.println(DepPrinter.TDsToString(tdl));

    }
	
	private Segmenter segmenter;
	private ZhtZhsConvertor convertor;
    public String[] tokenizedSentBuffer = null;

	public FullPCFGParser(int lang, Segmenter segmenter){
		super(lang);
		this.segmenter = segmenter;
	}

	public FullPCFGParser(int lang, Segmenter segmenter, ZhtZhsConvertor convertor){
		super(lang);
		this.segmenter = segmenter;
		this.convertor = convertor;
	}

	//sent: untokenized sentence
	public Tree parseUntokenizedSent(String untokenizedSent, int inLang, int outLang){
		// tokenize the sentence & converting the language
		tokenizedSentBuffer = segmenter.segmentStr(untokenizedSent, inLang, Lang.ZHS);

		// parse
		Tree result = parseTokenizedSent(tokenizedSentBuffer);

		// convert the language if necessary
		if(inLang != Lang.ENG && outLang == Lang.ZHT){
			result = wordToZht(result);
            tokenizedSentBuffer = wordToZht(tokenizedSentBuffer);
		}
		return result;
	}

    public List<TypedDependency> depParseUntokenizedSent(String untokenizedSent, 
            int inLang, int outLang, String imgPath){
        // tokenize the sentence & converting the language
        tokenizedSentBuffer = segmenter.segmentStr(untokenizedSent, inLang, Lang.ZHS);

		// constituent parsing
		Tree parse = parseTokenizedSent(tokenizedSentBuffer);
    
        // convert to typed dependencies
        List<TypedDependency> tdl = toTypedDependency(parse);

		// convert the language if necessary
		if(inLang != Lang.ENG && outLang == Lang.ZHT){
		    tdl = wordToZht(tdl); 
            tokenizedSentBuffer = wordToZht(tokenizedSentBuffer);
            //TODO: the conversion from zhs to zht is inperfect
        }

        //drawing the dependency tree
        if(imgPath != null){
            drawDepTree(parse, tdl, imgPath, 3);
        }

		return tdl;
    }
    
	public Tree parseTokenizedSent(String tokenizedSent, String sep, int inLang, int outLang){
		// tokenize the sentence & converting the language
		String[] sent = null;
        if(inLang == Lang.ZHT){
            String zhsSent = convertor.convertToZhs(tokenizedSent);
            sent = zhsSent.split(sep);
        }    
        else{
            sent = tokenizedSent.split(sep);
        }
		// parse
		Tree result = parseTokenizedSent(sent);

		// convert the language if necessary
		if(inLang != Lang.ENG && outLang == Lang.ZHT){
			result = wordToZht(result);
			return result;
		}
		return result;
	}

    //return constituent parsing and dependency parsing
    public Object[] CDParseTokenizedSent(String tokenizedSent, String sep, 
            int inLang, int outLang, String imgPath){
        // tokenize the sentence & converting the language
        String[] sent = null;
        if(inLang == Lang.ZHT){
            String zhsSent = convertor.convertToZhs(tokenizedSent);
            sent = zhsSent.split(sep);
        }    
        else{
            sent = tokenizedSent.split(sep);
        }
		// constituent parsing
		Tree parse = parseTokenizedSent(sent);
    
        // convert the language if necessary
		if(inLang != Lang.ENG && outLang == Lang.ZHT){
			parse = wordToZht(parse);
		}

        // convert to typed dependencies
        List<TypedDependency> tdl = toTypedDependency(parse);

        //drawing the dependency tree
        if(imgPath != null){
            drawDepTree(parse, tdl, imgPath, 3);
        }

        Object[] object = new Object[2];
        object[0] = parse;
        object[1] = tdl;
		return object;
    }

    //return constituent parsing and dependency parsing
    public Object[] CDParseUntokenizedSent(String untokenizedSent, int inLang, int outLang, String imgPath){
        // tokenize the sentence & converting the language
        tokenizedSentBuffer = segmenter.segmentStr(untokenizedSent, inLang, Lang.ZHS);

		// constituent parsing
		Tree parse = parseTokenizedSent(tokenizedSentBuffer);
    
        // convert to typed dependencies
        List<TypedDependency> tdl = toTypedDependency(parse);

        // convert the language if necessary
		if(inLang != Lang.ENG && outLang == Lang.ZHT){
            tokenizedSentBuffer = wordToZht(tokenizedSentBuffer);
			parse = wordToZht(parse);
            tdl = wordToZht(tdl);
		}

        //drawing the dependency tree
        if(imgPath != null){
            drawDepTree(parse, tdl, imgPath, 3);
        }

        Object[] object = new Object[2];
        object[0] = parse;
        object[1] = tdl;
		return object;
    }


    public List<TypedDependency> depParseTokenizedSent(String tokenizedSent, String sep, 
            int inLang, int outLang, String imgPath){
        // tokenize the sentence & converting the language
        String[] sent = null;
        if(inLang == Lang.ZHT){
            String zhsSent = convertor.convertToZhs(tokenizedSent);
            sent = zhsSent.split(sep);
        }    
        else{
            sent = tokenizedSent.split(sep);
        }
		// constituent parsing
		Tree parse = parseTokenizedSent(sent);
    
        // convert to typed dependencies
        List<TypedDependency> tdl = toTypedDependency(parse);

		// convert the language if necessary
		if(inLang != Lang.ENG && outLang == Lang.ZHT){
		    tdl = wordToZht(tdl); 
            //TODO: the conversion from zhs to zht is inperfect
        }

        //drawing the dependency tree
        if(imgPath != null){
            drawDepTree(parse, tdl, imgPath, 3);
        }

		return tdl;
    }



    public String getTokenizedSentBuffer(){
        if(tokenizedSentBuffer == null){
            return null;
        }
        else{
            return mergeStr(tokenizedSentBuffer);
        }
    }
	//convert the words in a typed dependency to ZHT  
    public TypedDependency wordToZht(TypedDependency td){
        IndexedWord g = td.gov();
        IndexedWord d = td.dep();

        g.setWord(convertor.convertToZht(g.word()));
        d.setWord(convertor.convertToZht(d.word()));

        return td;
    }

	//convert the words in a list of type dependency to ZHT 
    public List<TypedDependency> wordToZht(List<TypedDependency> tdl){
        for(int i = 0; i < tdl.size(); i++){
            tdl.set(i, wordToZht(tdl.get(i)));
        }
        return tdl;
    }

	//convert the list of string to ZHT
    public String[] wordToZht(String[] text){
        for(int i = 0; i < text.length; i++){
            text[i] = convertor.convertToZht(text[i]);
        }
        return text;
    }
	
	//convert the words in a tree to ZHT
	public Tree wordToZht(Tree tree){
		//recursively traverse the tree to convert words to ZHT;
		__leafWordToZht(tree);
		return tree;
	}
	
	private void __leafWordToZht(Tree tree){
        if(tree.numChildren() == 0){ //leave node (word node)
			//convert words to ZHT
            Label label = tree.label();
            label.setValue(convertor.convertToZht(label.toString()));
        }
        else{ 
			for(Tree c: tree.children()){
				__leafWordToZht(c);
			}
        }
    }
	
	//convert the words in list of nodes
	/*
	public ArrayList<Node> wordToZht(ArrayList<Node> nodes){
		for(Node n : nodes){
			n.word = convertor.convertToZht(n.word);
		}
		return nodes;
	}*/

	
	
}
