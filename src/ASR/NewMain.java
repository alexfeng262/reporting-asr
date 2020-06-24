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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
        ResourceManager rm = new ResourceManager();
        List<String> data = new ArrayList<>();
        File file = new File("etc\\corpus\\lm3gram1.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st; 
//        while ((st = br.readLine()) != null){
//            //text = text + " " +st;
//            if(!st.isBlank())
//                data.add(st);
//            //i++;
//        }
        br.close();
        LanguageModelBuilder lm = new LanguageModelBuilder("prueba","etc\\corpus\\lm3gram1.txt");
        data = lm.cleanCorpus();
        lm.buildVocab(data, "etc\\linguist\\vocab_phoneme1.dict");
     
        
    }

}
