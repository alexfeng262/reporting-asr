/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adaptation_mllr;

import ASR_tfm.app_gui;
import asr_utils.Resource_manager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alexf
 */
public class Generate_files {
    public static void create_fileid_file(String name){
        Resource_manager rm = new Resource_manager();
        
        String speaker_dir = rm.getWav_dir_path()+"\\"+name+"\\";
        File f = new File(speaker_dir);
        File fileids = new File(speaker_dir+"test.fileids");
        
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".txt");
            }
        };
        
        String[] txt_filenames = f.list(filter);
        FileOutputStream output_stream;
        try {
            output_stream = new FileOutputStream(fileids);
            OutputStreamWriter stream_writer = new OutputStreamWriter(output_stream,"utf-8");
            BufferedWriter out = new BufferedWriter(stream_writer);
            for (String filename: txt_filenames){
                out.write(speaker_dir + filename.replace(".txt", "") + "\n");
                //System.out.println(filename.replace(".txt", ""));
            }
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(app_gui.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(app_gui.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(app_gui.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
    
    public static void create_transcription_file(String name){
        Resource_manager rm = new Resource_manager();
        String speaker_dir = rm.getWav_dir_path()+"\\"+name+"\\";
        File f = new File(speaker_dir);
        File transcription = new File(speaker_dir + "test.transcription");
        
        String line = "";
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".txt");
            }
        };
        
        FileOutputStream output_stream;
        try {
            output_stream = new FileOutputStream(transcription);
            OutputStreamWriter stream_writer = new OutputStreamWriter(output_stream,"utf-8");
            BufferedWriter out = new BufferedWriter(stream_writer);
            
            for (String filename: f.list(filter)){
            
                BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(new File(speaker_dir + filename)), "UTF8"));
                String str;
                str = br.readLine();
                line = "<s> "+str+" </s> "+"("+filename.replace(".txt", "")+")\n";
                out.write(line);
            }
            
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Generate_files.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(Generate_files.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Generate_files.class.getName()).log(Level.SEVERE, null, ex);
        }
            
    }
    
    public static void create_vocab(String name){
        Resource_manager rm = new Resource_manager();
        String speaker_dir = rm.getWav_dir_path()+"\\"+name+"\\";
        File f = new File(speaker_dir);
        File dictionary = new File(speaker_dir + "vocab.dict");
        
        String line = "";
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".txt");
            }
        };
        
        FileOutputStream output_stream;
        List<String> token_list = new ArrayList<>();
        try {
            output_stream = new FileOutputStream(dictionary);
            OutputStreamWriter stream_writer = new OutputStreamWriter(output_stream,"utf-8");
            BufferedWriter out = new BufferedWriter(stream_writer);
            
            for (String filename: f.list(filter)){
                
                BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(new File(speaker_dir + filename)), "UTF8"));
                String str;
                str = br.readLine();
                
                String[] token = str.split(" ");
                Collections.addAll(token_list, token);
            }
            
            List<String> token_no_dup = new ArrayList<>(new HashSet<>(token_list));
            Collections.sort(token_no_dup);
            
            for(String word : token_no_dup){
                String word1 = word.replace("ce", "ze");
                word1 = word1.replace("ci","zi");
                word1 = word1.replace("v","b");                
                word1 = word1.replace("á","a");
                word1 = word1.replace("é","e");
                word1 = word1.replace("í","i");
                word1 = word1.replace("ó","o");
                word1 = word1.replace("ú","u");
                word1 = word1.replace("w","u");
                word1 = word1.replace("qui","ki");
                word1 = word1.replace("que","ke");
                word1 = word1.replace("q","k");
                word1 = word1.replace("ge","je");
                word1 = word1.replace("gi","ji");
                word1 = word1.replace("gui","gi");
                word1 = word1.replace("ch","++");
                word1 = word1.replace("c","k");
                word1 = word1.replace("h","");
                
                word1 = String.join(" ", word1.split(""));
                
                word1 = word1.replace("l l","ll");
                word1 = word1.replace("+ +","ch");
                word1 = word1.replace("r r","rr");
                word1 = word1.replace("ñ","gn");
                out.write(word + " "+ word1 + "\n");
            }
            out.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(app_gui.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(app_gui.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(app_gui.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
