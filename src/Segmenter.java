
import java.util.Properties;
import java.util.List;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreLabel;

import jopencc.ZhtZhsConvertor;

/**
 * General chinese segmenter interface
 * Date: 2014/12/15
 */
public class Segmenter{
    
    // Demo 
    public static void main(String[] args){
        //initialize the converter
        ZhtZhsConvertor convertor = new ZhtZhsConvertor("./jopencc");

        //initialize the segmenter
        Segmenter seg = new Segmenter("./stanford_segmenter", convertor);

        String str = "今天天氣很好,我中餐吃麵" ;
        String[] buf = seg.segmentStrZht(str);
        for(String word: buf){
            System.out.println(word);
        }
    }

    private Properties props;
    private CRFClassifier<CoreLabel> segmenter;
    private ZhtZhsConvertor convertor;

    /**
     *  segPath: the path of the needed in stanford segmenter
     *  jopenccPath: the path of jopencc package
     *  The segmenter will be ABLE to deal with traditional chinese words
     *  The default model is CRF Segmenter
     *  TODO: More model 
     * */
    public Segmenter(String segPath, ZhtZhsConvertor convertor) {
        this(segPath);
        this.convertor = convertor;
    }

    /**
     *  segPath: the path of the needed in stanford segmenter
     *  The segmenter will NOT BE ABLE to deal with traditional chinese words
     *  The default model is CRF Segmenter
     *  TODO: More model 
     * */
    public Segmenter(String segPath) {
        props = new Properties();
        props.setProperty("sighanCorporaDict", segPath + "/data");
        props.setProperty("serDictionary", segPath + "/data/dict-chris6.ser.gz");
        props.setProperty("inputEncoding", "UTF-8");
        props.setProperty("sighanPostProcessing", "true");
        segmenter = new CRFClassifier<CoreLabel>(props);
        try{
            segmenter.loadClassifier(segPath + "/data/ctb.gz", props);
        }
        catch(Exception e){
            System.err.println("Load dataset Error");
            e.printStackTrace();
        }
        this.convertor = null;
    }


    //segment the string (simplified chinese to simplified chinese)
    public String[] segmentStrZhs(String str){
        return segmentStr(str, Lang.ZHS, Lang.ZHS);
    }

    //segment the string (traditional chinese to traditional chinese)
    public String[] segmentStrZht(String str){
        return segmentStr(str, Lang.ZHT, Lang.ZHT);
    }

    //segment the string 
    public String[] segmentStr(String str, int inLang, int outLang){
        String strBuf = null;
        // convert to zhs or not 
        if(inLang == Lang.ZHT){ //convert to zht
            if(this.convertor == null){
                System.err.println("jopencc convertor not found. View it as simplified Chinese");
                strBuf = str;
            }
            strBuf = convertor.convertToZhs(str); 
        }
        else{ //zhs
            strBuf = str;
        }
        
        //segment
        List<String> words = segmenter.segmentString(strBuf);
        String[] output = new String[words.size()];
        
        //convert back to zht or not
        if(outLang == Lang.ZHT){  //convert back to zht
            if(this.convertor == null){
                System.err.println("jopencc convertor not found. Cannot convert it back to traditional Chinese");
                return (String []) words.toArray();
            }
            return convertor.convertToZht((String[]) words.toArray()); 
        }
        else{
            return (String []) words.toArray();
        }
    }
    public static String mergeStr(String[] str, String sep){
        if(str.length > 0){
            String r = new String();
            for(int i = 0; i < str.length - 1; i++){
                r = r + str[i] + sep;
                //System.out.println(str[i]);
            }
            r = r + str[str.length - 1];
            //System.out.println(str[str.length - 1]);
            return r;
        }
        else{
            return null;
        }
    }

}



