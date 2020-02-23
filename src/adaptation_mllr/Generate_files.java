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
        String base_path = "C:\\Users\\alexf\\Desktop\\ASR\\sphinx_adapt\\Alex\\";
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
    
}
