/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ASR;

import asr_utils.LoggerStatus;
import asr_utils.ResourceManager;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.Context;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
//import edu.cmu.sphinx.api.LiveSpeechRecognizer;



/**
 *
 * @author alexf
 */
public class AppRecognizer extends Thread{
    private volatile Configuration configuration;
    private volatile Boolean IsStart = false;
    private volatile LiveSpeechRecognizer recognizer;
    private volatile Boolean flag = false;

    private volatile Boolean StopThread = false;
    
    private final String mllr_path = "resource:/mllr_matrix/alex_mllr";
    private final String acoustic_model_path  ;
    private final String language_model_path ;
    private final String language_model_dir_path;
    private final String wav_dir_path;
    //private final String language_model_path1 = "file:C:\\Users\\alexf\\Documents\\GitHub\\clean-repo\\LM\\lm_4gram.bin";
    private final String dictionary_path ;
   
    private final String config_xml ;
    
    //public  Context context;
  
    //private App_gui Gui;


    public AppRecognizer(){
        ResourceManager rm = new ResourceManager();
        acoustic_model_path = "file:"+rm.getDefault_acoustic_model_dir_path();
        language_model_path = "file:"+rm.getDefault_language_model_file_path();
        dictionary_path = "file:"+rm.getDefault_dictionary_file_path();
        config_xml = "file:"+rm.getDefault_config_xml_file_path();
        language_model_dir_path = "file:"+rm.getLm_dir_path();
        wav_dir_path = "file:"+rm.getWav_dir_path();
        Config();  
    }
    
    public void loadConfig(Map<String, String> global_prop){
        
        try{
            configuration = new Configuration();
            if(global_prop.get("acousticModel").equals("Default"))
                configuration.setAcousticModelPath(acoustic_model_path);
            else{
                String map_adapt_path = wav_dir_path + "\\"+global_prop.get("acousticModel");
                configuration.setAcousticModelPath(map_adapt_path);
            }
           
            
            if(global_prop.get("languageModel").equals("Default")){
                configuration.setLanguageModelPath(language_model_path);
                configuration.setDictionaryPath(dictionary_path);
            }
            else{
                //String lm_name = global_prop.get("languageModel");
                String lm_path = language_model_dir_path + "\\"+global_prop.get("languageModel");
                String dict_path = language_model_dir_path + "\\"+global_prop.get("languageModel").replace(".lm", ".dict");
                configuration.setLanguageModelPath(lm_path);
                configuration.setDictionaryPath(dict_path);
            }

            configuration.setSampleRate(16000);
            configuration.setNewConfig(global_prop);
            Context context;
            context = new Context(config_xml,configuration);
            //context.setGlobalProperty("languageWeight",12);
            //@TODO:
            //create configuration manager from xml
            //set new global properties
            //export to xml with configuration manager utils and replace de old.
            
            //System.out.println("Config reloaded");
            recognizer.loadConfig(context);
            AppGui.showMessageGUI("Parámetros de configuración cargado exitósamente.", "info");
            System.out.println("Config reloaded");
            //recognizer.loadTransform("C:\\Users\\alexf\\Desktop\\ASR\\sphinx_adapt\\wav\\Alex\\mllr_matrix", 1); // Load MLLR
        }
        catch(Exception ex){
            AppGui.showMessageGUI("No se ha podido cargar configuración del reconocedor. Exception de tipo: " + ex.getMessage(), "error");
            System.out.println(ex.getMessage());
        }
        
    }
    
    private void Config(){
        LoggerStatus.Log("Loading Model",LoggerStatus.LogType.INFO);
        configuration = new Configuration();
        
        configuration.setAcousticModelPath(acoustic_model_path);
        configuration.setDictionaryPath(dictionary_path);
        configuration.setLanguageModelPath(language_model_path);
        configuration.setSampleRate(16000);
        Context context;
        
        try{
            context = new Context(config_xml,configuration);
            recognizer = new LiveSpeechRecognizer(context);
            //recognizer.loadTransform("C:\\Users\\alexf\\Desktop\\ASR\\sphinx_adapt\\wav\\Alex\\mllr_matrix", 1); // Load MLLR
            
        }
        catch(IOException ex){
            AppGui.showMessageGUI("No se ha podido cargar configuración del reconocedor.", "error");
            System.out.println(ex.getMessage());
        } catch (LineUnavailableException ex) {      
            AppGui.showMessageGUI("No line matching interface TargetDataLine supporting format "
                    + "PCM_SIGNED 16000.0 Hz, 16 bit, mono, 2 bytes/frame, little-endian is supported.", "warning");
        } catch (IllegalArgumentException ex){
            AppGui.showMessageGUI("No line matching interface TargetDataLine supporting format "
                    + "PCM_SIGNED 16000.0 Hz, 16 bit, mono, 2 bytes/frame, little-endian is supported.", "warning");
        }
        this.start();
        //app_gui.report_txt.append("Models ready....\n");
        
        LoggerStatus.Log("Preparado para escuchar. Presiona PLAY para empezar.",LoggerStatus.LogType.INFO);
        
    }
    
    private static String convertCapitalWord(String original){
        if (original == null || original.length() == 0) {
            return original;
        }
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }
    
    @Override
    public void run(){
        String previous_word = "";
        boolean capital_letter = true;
        LoggerStatus.Log("Preparado para escuchar. Presiona PLAY para empezar.",LoggerStatus.LogType.INFO);
        while(!StopThread){

            String utf = "holis";
            
            try {
                String utterance = new String(recognizer.getResult().getHypothesis());
                utf = new String(utterance.getBytes("ISO-8859-1"), "UTF-8");
                
                String[] words = utf.split(" ");
                //String phrase = "";
                
                for(String word: words){
                    int pos = AppGui.report_txt.getCaretPosition();
                    if(capital_letter){
                        word = convertCapitalWord(word);
                        capital_letter = false;
                    }
                    if(previous_word.isEmpty() || pos==0){
                        AppGui.report_txt.append(word + " ");   
                    }
                    else{
                        if(word.matches("[0-9\\.]")&&previous_word.matches("[0-9\\.]")){
                            AppGui.report_txt.replaceRange(word+" ", pos-1, pos);
                        }
                        else if(word.matches("[%]")&&previous_word.matches("[0-9]")){
                            AppGui.report_txt.replaceRange(word+" ", pos-1, pos);
                        }
                        else if(word.matches("[,\\)\\.]")){
                            AppGui.report_txt.replaceRange(word+" ", pos-1, pos);
                        }
                        else if(previous_word.matches("[\\(]")){
                            AppGui.report_txt.replaceRange(word + " ", pos-1, pos);
                        }
                        else if(word.matches("[//]")){
                            AppGui.report_txt.replaceRange(word , pos-1, pos);
                        }
                        else{
                            AppGui.report_txt.append(word + " ");
                        }
                    }
                    if(word.matches("[\\.]"))
                        capital_letter = true;
                    AppGui.report_txt.setCaretPosition(AppGui.report_txt.getDocument().getLength());
                    previous_word = word;
                }

            } catch (UnsupportedEncodingException ex) {
                AppGui.showMessageGUI("Excepción de tipo UnsupportedEncodingException.", "error");
            } catch(NullPointerException ex){
                //AppGui.showMessageGUI("Excepción de tipo NullPointerException en App_recognizer.", "error");
                StopThread = true;
            }
            
        }
        
    }


    public void stopRecognition(){
        //this.IsStart = false;
        recognizer.stopRecognition();
        //app_gui.report_txt.setEnabled(false);
        LoggerStatus.Log("Reconocedor detenido.",LoggerStatus.LogType.INFO);
    }
    
    public void closeRecognition(){
        recognizer.closeRecognition();
    }
    
    public void initRecognition(){
        
        recognizer.initRecognition();
    }
    public void startRecognition(){
      
        recognizer.startRecognition(true);
        LoggerStatus.Log("Reconociendo....",LoggerStatus.LogType.INFO);            
    }
    
    public void loadSpeakerMLLR(String name){
        ResourceManager rm = new ResourceManager();
        
        try {
            recognizer.loadTransform(rm.getWav_dir_path()+"\\"+name+"\\mllr_matrix", 1); // Load MLLR
        } catch (Exception ex) {
            AppGui.showMessageGUI("Excepción al cargar MLLR: "+ex.getMessage(), "error");
            Logger.getLogger(AppRecognizer.class.getName()).log(Level.SEVERE, null, ex);
        }
    
    }
}

