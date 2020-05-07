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
import java.io.FileNotFoundException;
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
public class LanguageModelBuilder {
    private final Resource_manager rm;
    private final String model_name;
    private final String corpus_path;
    
    public LanguageModelBuilder(String name, String corpus_path){
        this.rm = new Resource_manager();
        this.model_name = name;
        this.corpus_path = corpus_path;
    }
    
    public List<String> cleanCorpus() throws IOException{
        //Create ArrayList
        List<String> data = new ArrayList<>();
        List<String> all_sentences = new ArrayList<>();
        List<String> regex_sentences = new ArrayList<>();
        
        //Load and config CoreNLP
        Properties props = new Properties();
        props.load(IOUtils.readerFromString("StanfordCoreNLP-spanish.properties"));
        props.setProperty("annotators", "tokenize,ssplit");
        props.setProperty("tokenize.language", "Spanish");
        props.setProperty("tokenize.options", "ptb3Escaping=false");
        
        StanfordCoreNLP corenlp = new StanfordCoreNLP(props);

        //Load File
        File file = new File(corpus_path); //Select in jfile_chooser
        File filewrite = new File(rm.getCorpus_dir_path() + "\\"+model_name+".txt"); 
        File vocab_json = new File(rm.getDefault_vocab_correction_file_path());
        File abc_json = new File("etc\\word_correction\\abc.json");

        //Read Json
        JsonReader reader = Json.createReader(new FileReader(vocab_json));
        JsonObject vocabObject = reader.readObject();
        reader.close();

        JsonReader reader2 = Json.createReader(new FileReader(abc_json));
        JsonObject abcObject = reader2.readObject();
        reader2.close();

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

                if(!tok.isBlank())
                    if(!(tok.matches("^h+h$") || tok.matches("^c+c$") || tok.matches("^q+q$") || tok.matches("^v+v$") ||
                    tok.matches("^w+w$") || tok.matches("^ñ+ñ$"))){
                        for(String word:tok.split(" ")){
                            strList.add(word);
                        }
                    }
            }

            if(!strList.isEmpty())
                all_sentences.add(String.join(" ", strList));
        }

        //Preprocess sentences
        for(String sent : all_sentences){
            String sent1 = sent.replaceAll("(i\\.v\\.)|(i\\.? ?v\\.)|(i\\.v)","i.v.");
            sent1 = sent1.replaceAll("\\b(x x)\\b","");
            sent1 = sent1.replaceAll("([i|j]\\. d\\.)|(i\\.?d\\.)|(i\\.d)|(idx)|(j\\.? ?d\\.?)|([ij]\\. d)","id");
            sent1 = sent1.replaceAll("1 8 F - FDG","18F-FDG");
            sent1 = sent1.replaceAll("PET - CT","PET-CT");
            sent1 = sent1.replaceAll("9 9 mTc - HDP","99mTc-HDP");
            sent1 = sent1.replaceAll("9 9 mTc - DMSA","99mTc-DMSA");
            sent1 = sent1.replaceAll("9 9 mTc - MAG 3","99mTc-MAG3");
            sent1 = sent1.replaceAll("9 9 mTc - MAA","99mTc-MAA");
            sent1 = sent1.replaceAll("9 9 mTc - DTPA","99mTc-DTPA");
            sent1 = sent1.replaceAll("9 9 mTc - ECD","99mTc-ECD");
            sent1 = sent1.replaceAll("1 2 3 i - ioflupano","123I-Ioflupano");
            sent1 = sent1.replaceAll("1 2 3 i - MIBG","123I-MIBG");
            sent1 = sent1.replaceAll("2 2 3 - ra","223-Ra");
            sent1 = sent1.replaceAll("6 7 ga","67Ga");
            regex_sentences.add(sent1);
        }

        //Write to file
        BufferedWriter writer = new BufferedWriter(new FileWriter(filewrite));
        for(String sent : regex_sentences){  
            writer.write(sent + "\n");
            //System.out.println(sent);
        }
        writer.close();
            
            
        
        
        return regex_sentences;
    }
    
    public void buildVocab(List<String> sentences) throws FileNotFoundException, IOException{
        SortedSet<String> vocab = new TreeSet<>();
        List<String> vocab_phoneme = new ArrayList<>();
        File abc_json = new File("etc\\word_correction\\abc.json");
            
        //Read Json
        JsonReader reader = Json.createReader(new FileReader(abc_json));
        JsonObject abcObject = reader.readObject();
        reader.close();
        
        for(String sentence : sentences){
            String[] words = sentence.split(" ");
            for(String word : words){
                vocab.add(word);
            }
        }
        
        vocab.remove("");
        
        // Make phoneme
        for(String word_vocab : vocab){    
            String word = word_vocab;
            
            if(abcObject.containsKey(word))
                word = abcObject.getString(word);
            
            word = word.toLowerCase();
            word = word.replace("ce","ze");
            word = word.replace("ci","zi");
            word = word.replace("v","b");
            word = word.replace('á','a');
            word = word.replace('é','e');
            word = word.replace('í','i');
            word = word.replace('ó','o');
            word = word.replace('ú','u');
            word = word.replace('ü','u');
            word = word.replace('w','u');
            word = word.replace("qui","ki");
            word = word.replace("que","ke");
            word = word.replace('q','k');
            word = word.replace("ge","je");
            word = word.replace("gi","ji");
            word = word.replace("gui","gi");
            word = word.replace("ch","++");
            word = word.replace('c','k');
            word = word.replace("ge","je");
            word = word.replace("h","");
            word = word.replace(".","");
            word = word.replace("-","");
            word = word.replaceAll("[0-9]","");
           
            String[] split_word = word.split("");
            String new_word = String.join(" ", split_word);
            new_word = new_word.replace("l l","ll");
            new_word = new_word.replace("+ +","ch");
            new_word = new_word.replace("r r","rr");
            new_word = new_word.replace("ñ","gn");
            if(!new_word.isBlank())
                vocab_phoneme.add(word_vocab+" "+new_word);
        }
        File filewrite = new File(rm.getLm_dir_path() + "\\"+model_name+".dict"); //set name
        BufferedWriter writer;
       
        writer = new BufferedWriter(new FileWriter(filewrite));
        for(String sent : vocab_phoneme){  
        writer.write(sent + "\n");
        //System.out.println(sent);
        }
        writer.close();
       
    
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
