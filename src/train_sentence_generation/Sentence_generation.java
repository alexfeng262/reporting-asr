/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package train_sentence_generation;

import asr_utils.Resource_manager;
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
public class Sentence_generation {
    private String[] corpus_array;
    
    public Sentence_generation(){
        corpus_array = load_corpus();
    }
    
    private String[] load_corpus(){
        Resource_manager rm = new Resource_manager();
        
        List<String> list = new ArrayList<>();
        try {
            File file=new File(rm.getCorpus_path());
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
    
    public String generate_sentence(){
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(corpus_array.length) ;
        
        return corpus_array[randomInt];
    }
    
    
}
