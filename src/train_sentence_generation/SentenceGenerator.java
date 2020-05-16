/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package train_sentence_generation;

import asr_utils.ResourceManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author alexf
 */
public class SentenceGenerator {
    private String[] corpus_array;
    
    public SentenceGenerator(){
        corpus_array = loadCorpus();
    }
    
    private String[] loadCorpus(){
        ResourceManager rm = new ResourceManager();
        
        List<String> list = new ArrayList<>();
        try {
            File file=new File(rm.getDefault_corpus_file_path());
            //FileReader fr=new FileReader(file);
            
            BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
            String str;
            while((str = br.readLine()) != null){
                if(str.length() > 50)
                    list.add(str);
            }
            String[] stringArr = list.toArray(new String[0]);
            return stringArr;
            //
        
            //System.out.println(stringArr[randomInt]);
            
            
        } catch (Exception e) {
        }
        return new String[0];
    }
    
    public String generateSentences(){
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(corpus_array.length) ;
        
        return corpus_array[randomInt];
    }
    
    
}
