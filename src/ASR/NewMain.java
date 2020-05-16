/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ASR;
import asr_utils.ResourceManager;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import kylm.main.CountNgrams;
/**
 *
 * @author alexf
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
//       LanguageModelBuilder lm = new LanguageModelBuilder("probando","C:\\Users\\alexf\\Desktop\\ASR\\joan-andreu.txt");
//       List<String> sentences = lm.cleanCorpus();
//       
//       
//       
//        try {
//            lm.buildVocab(sentences);
//            lm.buildLm();
//            //Load File
//        } catch (Exception ex) {
//            Logger.getLogger(NewMain.class.getName()).log(Level.SEVERE, null, ex);
//        }

//Load and config CoreNLP
            Properties props = new Properties();
            props.load(IOUtils.readerFromString("StanfordCoreNLP-spanish.properties"));
            props.setProperty("annotators", "tokenize,ssplit");
            props.setProperty("tokenize.language", "Spanish");
            StanfordCoreNLP corenlp = new StanfordCoreNLP(props);
            
            CoreDocument document = new CoreDocument("Adenopatías patológicas hipermetabólicas  paravertebrales bilaterales a nivel de vértebra D10 y D11");
            corenlp.annotate(document);
            for (CoreSentence sent : document.sentences()) {
                for(CoreLabel token : sent.tokens()){
                    System.out.println(token.word());
                }
            }
    }
    
}
