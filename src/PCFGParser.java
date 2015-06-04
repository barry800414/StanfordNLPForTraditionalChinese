import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.*;

import com.chaoticity.dependensee.*;
/*
	The lexicalized parser for parsing Simplfied Chinese and English 
    sentences only. Besides, it does not support untokenized sentence.
*/
public class PCFGParser{
	// Demo 
    public static void main(String[] args){
        //initialize the parser
    	PCFGParser p = new PCFGParser(Lang.ZHS);

    	//Example 1: Parse tokenized sentences
    	String[] tokenizedSent = {"今天", "天气", "很", "好"};
    	Tree result = p.parseTokenizedSent(tokenizedSent);
        result.pennPrint();
        ArrayList<Label> posTags = new ArrayList<Label>();
        PCFGParser.treeTraversalGetPOSTags(result,posTags);

        String str = TreePrinter.treeToString(result);
        System.out.println(posTags);
        System.out.println(str);
    	//Example 2: Parse tokenized sentences 
    	//(original sentence which is separated by sep)
    	//String sepSent = "今天 天气 很 好";
    	//result = p.parseTokenizedSent(sepSent, " ");
    }


	public LexicalizedParser lp = null;
	private int lang = -1;
    public TreebankLanguagePack tlp = null;    
    public GrammaticalStructureFactory gsf = null;

	public PCFGParser(int lang){
		this.lang = lang;
		if(lang == Lang.ENG){
			lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		}
		else if(lang == Lang.ZHS){
			lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/chinesePCFG.ser.gz");
		}
        tlp = lp.treebankLanguagePack(); // TreebankLanguagePack for Chinese
        gsf = tlp.grammaticalStructureFactory();
        DepDrawer.setTreebankLanguagePack(tlp); // for drawing dependency trees
	}
	
	public Tree parseTokenizedSent(String[] sent){
	    List<CoreLabel> rawWords = Sentence.toCoreLabelList(sent);
	    Tree parse = lp.apply(rawWords);
	    return parse;
	}
	
	public Tree parseTokenizedSent(String sent, String sep){
		String[] tokenizedSent = sent.split(sep);
		return parseTokenizedSent(tokenizedSent);
	}
	
    public List<TypedDependency> depParseTokenizedSent(String[] tokenizedSent, String imgPath){
		// constituent parsing
		Tree parse = parseTokenizedSent(tokenizedSent);
    
        // convert to typed dependencies
        List<TypedDependency> tdl = toTypedDependency(parse);

        //drawing the dependency tree
        if(imgPath != null){
            drawDepTree(parse, tdl, imgPath, 3);
        }
		return tdl;
    }


    public List<TypedDependency> depParseTokenizedSent(String sent, String sep, String imgPath){
		String[] tokenizedSent = sent.split(sep);
        return depParseTokenizedSent(tokenizedSent, imgPath); 
    }

    //return constituent parsing and dependency parsing
    public Object[] CDParseTokenizedSent(String sent, String sep, String imgPath){
        // constituent parsing
		Tree parse = parseTokenizedSent(sent, sep);
    
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



    // convert to dependency parsing 
    public List<TypedDependency> toTypedDependency(Tree parse){
        GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
        List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
        return tdl;
    }
    
    public String mergeStr(String[] str){
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

    public void drawDepTree(Tree parse, List<TypedDependency> tdl, String filePrefix, int scale){
        //drawing image
        try {
            DepDrawer.writeImage(parse, tdl, filePrefix + ".png" , scale);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

	public static Label[] getPOSTags(Tree parse){
		ArrayList<Label> posTags = new ArrayList<Label>();
		treeTraversalGetPOSTags(parse, posTags);
		Label[] tags = new Label[posTags.size()];
		posTags.toArray(tags);
		return tags;
	}
	
	private static boolean treeTraversalGetPOSTags(Tree t, ArrayList<Label> posTags){
		if(t.numChildren() == 0){
			return true;
		}
		else{
			boolean childIsLeave;
			for(Tree c: t.children()){
				childIsLeave = treeTraversalGetPOSTags(c, posTags);
				if(childIsLeave){
					//t.pennPrint();
					posTags.add(t.label());
					return false;
				}
			}
			return false;
		}
	}
}
