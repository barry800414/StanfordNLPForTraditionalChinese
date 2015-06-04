 
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.ling.Sentence;
import java.util.List;

/*
 * GrammaticalStructure contains List<TypedDependencies>
 * TypedDependencies t: 
 *      t.reln(): relation, 
 *      t.gov(): govern word(IndexedWord)
 *      t.dep(): dependent word(IndexedWord)
 * IndexedWord w: 
 *      w.word(): word
 *      w.tag(): POS tagger of the word
 *      w.index(): the index of the word in string
 */


public class DepPrinter{
    //format: reln gov_index gov_word gov_tag dep_index dep_word dep_tag
    public static String TDToString(TypedDependency td){
        if(td == null){
            return null;
        }
        IndexedWord g = td.gov();
        IndexedWord d = td.dep();
        String str = String.format("%s %d %s %s %d %s %s", 
                td.reln(), g.index(), g.word(), g.tag(),
                d.index(), d.word(), d.tag());
        return str;
    }

    public static String TDsToString(List<TypedDependency> tdList){
        if(tdList == null){
            return null;
        }
        String str = "";
        for(TypedDependency td: tdList){
            str = str + DepPrinter.TDToString(td) + "\n";
        }
        return str;
    }
    
    public static int getExpLineNum(List<TypedDependency> tdList){
        return tdList.size();
    }

}

