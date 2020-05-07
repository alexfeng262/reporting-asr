/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ASR_tfm;

import asr_utils.Resource_manager;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author alexf
 */
public class NewMain1 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //String [] text = "a los 6 7 minutos".split(" ");
        //for(String word : text)
         //   System.out.println(word);
        cleanCorpus();
    }
    public static void cleanCorpus(){
        //Create ArrayList
        List<String> all_sentences = new ArrayList<>();
        List<String> regex_sentences = new ArrayList<>();
        Resource_manager rm = new Resource_manager();
        String model_name = "pruebas";
        try {
            //Load and config CoreNLP
            Properties props = new Properties();
            props.load(IOUtils.readerFromString("StanfordCoreNLP-spanish.properties"));
            props.setProperty("annotators", "tokenize,ssplit");
            props.setProperty("tokenize.language", "Spanish");
            props.setProperty("tokenize.options", "ptb3Escaping=false");
            StanfordCoreNLP corenlp = new StanfordCoreNLP(props);
            
            //Load File
            //File file = new File(corpus_path); //Select in jfile_chooser
            //File filewrite = new File(rm.getCorpus_dir_path() + "\\"+model_name+".txt"); 
            File vocab_json = new File(rm.getDefault_vocab_correction_file_path());
            File exceptions_json = new File("etc\\word_correction\\abc.json");
            
            //Read Json
            JsonReader reader = Json.createReader(new FileReader(vocab_json));
            JsonObject vocabObject = reader.readObject();
            reader.close();
            
            JsonReader reader2 = Json.createReader(new FileReader(exceptions_json));
            JsonObject abcObject = reader2.readObject();
            reader2.close();
            
            String data = "99mTc-HDP ...... Aumento a- de (18-7-6) la captación 18F-FDG en {ci}ntura D10 -- (h) escapular y 20/20/2020 pelviana, en probable";
           
            //Preprocess token
            CoreDocument document = new CoreDocument(data);
            corenlp.annotate(document);
            for (CoreSentence sent : document.sentences()) {
            List<String> strList = new ArrayList<>();

            for(CoreLabel token : sent.tokens()){
                String tok = token.word();
                tok = tok.toLowerCase();
                
                if(vocabObject.containsKey(tok)){
                    tok = vocabObject.getString(tok);
                }
                
                if(!abcObject.containsKey(tok)){
                    tok = tok.replaceAll(",+(?=\\d)",".");
                    tok = tok.replaceAll("^\\.+(?=\\d)","0.");
                    tok = tok.replaceAll("[^a-zA-ZáéíóúüñÁÉÍÓÚÑ0-9\\)\\(\\.\\%,\\-\\s//]", "");
                    tok = tok.replaceAll("x{2,}","");
                    tok = tok.replaceAll("\\.{1,}",".");
                    tok = tok.replaceAll("\\-{1,}","-");
                    tok = tok.replaceAll("(?<=[A-Za-záéíóú//\\-])\\.?(?=[0-9\\-\\.])|(?<=[0-9\\-\\.])\\.?(?=[A-Za-záéíóú//\\-])"," ");
                    //tok = tok.replaceAll("(?<=[A-Za-záéíóú//])\\.?(?=[\\-\\.])|(?<=[\\-\\.])\\.?(?=[A-Za-záéíóú//])"," ");
                    tok = tok.replaceAll("(?=\\d)"," ");
                    tok = tok.replaceAll("(?=\\. \\d)"," ");
                }

                //System.out.println(tok);
                tok = tok.trim();
                //System.out.println(tok);
                if(!tok.isBlank())
                    if(!(tok.matches("^h+$") || tok.matches("^c+$") || tok.matches("^q+$") || tok.matches("^v+$") ||
                    tok.matches("^w+$") || tok.matches("^ñ+$"))){
                        for(String word:tok.split(" ")){
                            strList.add(word);
                            
                        }
                    }
            }         
                if(!strList.isEmpty())
                    all_sentences.add(String.join(" ", strList));
                System.out.println(String.join(" ", strList));
            }
            
           
            
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
}
