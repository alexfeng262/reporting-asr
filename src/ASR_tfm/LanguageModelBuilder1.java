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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
public class LanguageModelBuilder1 {
    private final Resource_manager rm;
    private final String model_name;
    private final String corpus_path;
    
    public LanguageModelBuilder1(String name, String corpus_path){
        this.rm = new Resource_manager();
        this.model_name = name;
        this.corpus_path = corpus_path;
    }
    
    public List<String> cleanCorpus(){
        //Create ArrayList
        List<String> data = new ArrayList<>();
        List<String> all_sentences = new ArrayList<>();
        List<String> regex_sentences = new ArrayList<>();
        try {
            //Load and config CoreNLP
            Properties props = new Properties();
            props.load(IOUtils.readerFromString("StanfordCoreNLP-spanish.properties"));
            props.setProperty("annotators", "tokenize,ssplit");
            props.setProperty("tokenize.language", "Spanish");
            StanfordCoreNLP corenlp = new StanfordCoreNLP(props);
            
            //Load File
            File file = new File(corpus_path); //Select in jfile_chooser
            File filewrite = new File(rm.getCorpus_dir_path() + "\\"+model_name+".txt"); 
            File vocab_json = new File(rm.getDefault_vocab_correction_file_path());
            
            //Read Json
            JsonReader reader = Json.createReader(new FileReader(vocab_json));
            JsonObject vocabObject = reader.readObject();
            reader.close();
            
            //Read corpus
            BufferedReader br = new BufferedReader(new FileReader(file));
            String st; 
            while ((st = br.readLine()) != null){
                //text = text + " " +st;
                if(!st.isBlank())
                    data.add(st);
                //i++;
            }
            br.close();
            
            //Preprocess token
            CoreDocument document = new CoreDocument(String.join("\n", data));
            corenlp.annotate(document);
            for (CoreSentence sent : document.sentences()) {
                List<String> strList = new ArrayList<>();
                
                for(CoreLabel token : sent.tokens()){
                    String tok = token.word();
                    tok = tok.toLowerCase();
                    tok = tok.replaceAll(",+(?=\\d)",".");
                    tok = tok.replaceAll("^\\.+(?=\\d)","0.");
                    tok = tok.replaceAll("[^a-zA-ZáéíóúüñÁÉÍÓÚÑ\\.\\%0-9]|(lrb)|(rrb)", "");
                    tok = tok.replaceAll("x{2,}","");
                    tok = tok.replaceAll("(?<=[A-Za-záéíóú])\\.?(?=[0-9])|(?<=[0-9])\\.?(?=[A-Za-záéíóú])"," ");
                    tok = tok.replaceAll("(?=\\d)"," ");
                    
                    tok = tok.replaceAll("(?=\\. \\d)"," ");
                    
                    tok = tok.trim();
           
                    strList.add(tok);    
                    
                }
                
                if(!strList.isEmpty())
                    all_sentences.add(String.join(" ", strList).toLowerCase());
            }
            
            for(String sent : all_sentences){
                String sent1 = sent.replaceAll("(i\\.v\\.)|(i\\.? ?v\\.)|(i\\.v)","i.v.");
                sent1 = sent1.replaceAll("\\b(x x)\\b","");
                sent1 = sent1.replaceAll("([i|j]\\. d\\.)|(i\\.?d\\.)|(i\\.d)|(idx)|(j\\.? ?d\\.?)|([ij]\\. d)","id");
                regex_sentences.add(sent1);
            }
            
            //Write to file
            BufferedWriter writer = new BufferedWriter(new FileWriter(filewrite));
            for(String sent : regex_sentences){  
                writer.write(sent + "\n");
                //System.out.println(sent);
            }
            writer.close();
            
            
        } catch (Exception e) {
        }
        
        return regex_sentences;
    }
    
    public void buildVocab(List<String> sentences){
        SortedSet<String> vocab = new TreeSet<>();
        List<String> vocab_phoneme = new ArrayList<>();
        
        for(String sentence : sentences){
            String[] words = sentence.split(" ");
            for(String word : words){
                vocab.add(word);
            }
        }
        
        vocab.remove("");
        
 
        File filewrite = new File(rm.getLm_dir_path() + "\\"+model_name+".dict"); //set name
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(filewrite));
            for(String sent : vocab){  
            writer.write(sent + "\n");
            //System.out.println(sent);
        }
        writer.close();
        } catch (IOException ex) {
            
        }
    
    }
    
    public  void buildLm() throws Exception{
        String[] kylm_args = new String[]{
            rm.getCorpus_dir_path()+"\\"+model_name+".txt",
            "-n","3",
            "-arpa", rm.getLm_dir_path()+"\\"+model_name+".lm",
            "-mkn"};
        
            CountNgrams.main(kylm_args);
       
    }
}
