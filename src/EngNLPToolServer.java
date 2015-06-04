import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.nio.charset.Charset;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.trees.*;

public class EngNLPToolServer {
    public static FullPOSTagger tagger;
    public static FullNNDepParser fdp;
    public static PCFGParser fpp;

    private static final String imgFolder = "/utmp/weiming/eng_deptree_img/";
    
    public static void main(String[] args) {

        //initialize the pos-tagger 
        System.out.println(">>>>>> Initializing POS-tagger ...");
        tagger = new FullPOSTagger("./stanford_postagger", Lang.ENG);

        //Initialize the English PCFG parser
        System.out.println(">>>>> Initializing PCFG Parser ...");
        fpp = new PCFGParser(Lang.ENG);

        //Initialize the server
        try{
            HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
            server.createContext("/info", new InfoHandler());
            server.createContext("/pos", new POSHandler());
            server.createContext("/pcfg", new PCFGConstParserHandler());
            server.createContext("/pcfg_dep", new PCFGDepParserHandler());
            server.createContext("/pcfg_all", new PCFGParserHandler());

            server.setExecutor(null); // creates a default executor
            server.start();
            System.out.println("The server is running");
        }
        catch(Exception e){
            e.printStackTrace(System.out);
            e.printStackTrace();
        }
    }

    // http://localhost:8000/info
    static class InfoHandler implements HttpHandler {
        public void handle(HttpExchange httpExchange) throws IOException {
            String response = "/pos?seg_s=sentence\n/pcfg_dep?seg_s=sentence";
            NLPToolServer.writeResponse(httpExchange, response.toString());
        }
    }


    // http://localhost:port/pos?s=sentence
    // http://localhost:port/pos?seg_s=sentence
    static class POSHandler implements HttpHandler {
        public void handle(HttpExchange httpExchange) {
            try {
                StringBuilder response = new StringBuilder();
                Map <String,String>parms = EngNLPToolServer.queryToMap(httpExchange.getRequestURI().getQuery());
                
                // default: segemented sentence (word delimiter is space)
                String input = parms.get("seg_s");

                List<TaggedWord> tagged = tagger.tagTokenizedSent(input, Lang.ENG, Lang.ENG);
                String output = Sentence.listToString(tagged, false);
                response.append(output);
                
                System.out.println("Reqeust:" + input);
                System.out.println("Response:" + output);
                
                NLPToolServer.writeResponse(httpExchange, response.toString());
            }
            catch(Exception e){
                e.printStackTrace(System.out);
                e.printStackTrace();
                NLPToolServer.writeFailResponse(httpExchange);
            }
        }
    }


    // Stanford PCFG Dependency Parser handler (output: stanford dependencies)
    //http://localhost:port/pcfg_dep?s=sentence?f_name=ooo?f_folder=xxx
    static class PCFGDepParserHandler implements HttpHandler {
        public void handle(HttpExchange httpExchange) {
            try{ 
                String imgPath = null;
                //retrieve sentence
                StringBuilder response = new StringBuilder();
                Map <String,String>parms = EngNLPToolServer.queryToMap(httpExchange.getRequestURI().getQuery());
                String text = parms.get("seg_s");

                //Check drawing dependency tree or not
                String drawFlag = parms.get("draw");
                if(drawFlag != null){
                    if(drawFlag.toLowerCase().compareTo("true") == 0){
                        String fileFolder = parms.get("f_folder");
                        String fileName = parms.get("f_name");
                        if(fileFolder == null || fileName == null || 
                           fileFolder.length() == 0 || fileName.length()==0){
                            fileFolder = ".";
                            fileName = text;
                        }
                        imgPath = EngNLPToolServer.imgFolder + fileFolder + "/" + fileName;
                    }
                }
                System.out.println(imgPath);
                //dependency parsing by pcfg parser
                List<TypedDependency> tdList = fpp.depParseTokenizedSent(text, " ", imgPath);
                String depStr = DepPrinter.TDsToString(tdList); //get typed dependencies
                response.append(text + "\n");
                response.append(depStr);

                //System.out.println("Reqeust:" + input.substring(0, input.length() > 10 ? 10: input.length()) + "...");
                //System.out.println("Response:" + output.substring(0, output.length() > 10 ? 10: output.length()) + "...");
                System.out.println("Reqeust:" + text);
                System.out.println("Response:" + depStr);
       
                NLPToolServer.writeResponse(httpExchange, response.toString());
            }
            catch(Exception e){
                e.printStackTrace(System.out);
                e.printStackTrace();
                NLPToolServer.writeFailResponse(httpExchange);
            }
        }
    }

	// Stanford PCFG Constituent Parser handler (output: stanford parsing tree as a list of nodes and edges)
    //http://localhost:port/pcfg?s=sentence
    static class PCFGConstParserHandler implements HttpHandler {
        public void handle(HttpExchange httpExchange){
            try{
                //retrieve sentence
                StringBuilder response = new StringBuilder();
                Map <String,String>parms = EngNLPToolServer.queryToMap(httpExchange.getRequestURI().getQuery());
                String text = parms.get("seg_s");

                //constituent parsing by pcfg parser
                Tree tree = fpp.parseTokenizedSent(text, " ");
                String treeStr = TreePrinter.treeToString(tree); //get string of tree
                response.append(text + "\n");
                response.append(treeStr);

                //System.out.println("Reqeust:" + text.substring(0, text.length() > 10 ? 10: text.length()) + "...");
                //System.out.println("Response:" + output.substring(0, output.length() > 10 ? 10: output.length()) + "...");
                System.out.println("Reqeust:" + text);
                System.out.println("Response:" + treeStr);
     
                NLPToolServer.writeResponse(httpExchange, response.toString());
            }
            catch(Exception e){
                e.printStackTrace(System.out);
                e.printStackTrace();
                NLPToolServer.writeFailResponse(httpExchange);
            }
        }
    }

    //both constituent parsing and dependency parsing
    static class PCFGParserHandler implements HttpHandler {
        public void handle(HttpExchange httpExchange){
            try{
                String imgPath = null;

                //retrieve sentence
                StringBuilder response = new StringBuilder();
                Map <String,String>parms = EngNLPToolServer.queryToMap(httpExchange.getRequestURI().getQuery());
                String text = parms.get("seg_s");

                //Check drawing dependency tree or not
                String drawFlag = parms.get("draw");
                if(drawFlag != null){
                    if(drawFlag.toLowerCase().compareTo("true") == 0){
                        String fileFolder = parms.get("f_folder");
                        String fileName = parms.get("f_name");
                        if(fileFolder == null || fileName == null || 
                           fileFolder.length() == 0 || fileName.length()==0){
                            fileFolder = ".";
                            fileName = text;
                        }
                        imgPath = EngNLPToolServer.imgFolder + fileFolder + "/" + fileName;
                    }
                }
                System.out.println(imgPath);

                //constituent parsing & dependency parsing by pcfg parser
                Object[] object = fpp.CDParseTokenizedSent(text, " ", imgPath);
                Tree tree = (Tree) object[0];
                List<TypedDependency> tdList = (List<TypedDependency>) object[1];
                String treeStr = TreePrinter.treeToString(tree); //get string of tree
                String depStr = DepPrinter.TDsToString(tdList); //get string of typed dependencies
                
                int nLine1 = TreePrinter.getExpLineNum();
                int nLine2 = DepPrinter.getExpLineNum(tdList);
                response.append(nLine1 + " " + nLine2 + '\n');
                response.append(treeStr);
                response.append(depStr);

                //System.out.println("Reqeust:" + text.substring(0, text.length() > 10 ? 10: text.length()) + "...");
                //System.out.println("Response:" + output.substring(0, output.length() > 10 ? 10: output.length()) + "...");
                System.out.println("Reqeust:" + text);
                System.out.println("Response:" + response.toString());
     
                NLPToolServer.writeResponse(httpExchange, response.toString());
            }
            catch(Exception e){
                e.printStackTrace(System.out);
                e.printStackTrace();
                NLPToolServer.writeFailResponse(httpExchange);
            }
        }
    }


    public static void writeResponse(HttpExchange httpExchange, String response) throws IOException {
        Headers header = httpExchange.getResponseHeaders();
        header.add("Content-Type", "text/plain; charset=utf-8");
        httpExchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }


    public static void writeFailResponse(HttpExchange httpExchange){
        try{
            httpExchange.sendResponseHeaders(500, 0);
            httpExchange.getResponseBody().close();
        }
        catch(Exception e){
            e.printStackTrace(System.out);
            e.printStackTrace();
        }
    }


    /**
     * returns the url parameters in a map
     * @param query
     * @return map
     */
    public static Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<String, String>();
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length>1) {
                result.put(pair[0], pair[1].replace("+", " "));
            }else{
                result.put(pair[0], "");
            }
        }
        return result;
    }

}
